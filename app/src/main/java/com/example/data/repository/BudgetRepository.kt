package com.example.data.repository

import com.example.data.dao.CategoryBudgetDao
import com.example.data.dao.RecurringBillDao
import com.example.data.dao.SavingsContributionDao
import com.example.data.dao.SavingsGoalDao
import com.example.data.dao.TransactionDao
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.RecurringBillEntity
import com.example.data.model.SavingsContributionEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BudgetRepository(
    private val transactionDao: TransactionDao,
    private val categoryBudgetDao: CategoryBudgetDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val savingsContributionDao: SavingsContributionDao,
    private val recurringBillDao: RecurringBillDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allCategoryBudgets: Flow<List<CategoryBudgetEntity>> = categoryBudgetDao.getAllBudgets()
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals()
    val allRecurringBills: Flow<List<RecurringBillEntity>> = recurringBillDao.getAllBills()

    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsBetween(startMillis, endMillis)
    }

    fun getTransactionsForMonth(year: Int, monthZeroIndexed: Int): Flow<List<TransactionEntity>> {
        val cal = Calendar.getInstance()
        cal.set(year, monthZeroIndexed, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startMillis = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endMillis = cal.timeInMillis

        return transactionDao.getTransactionsBetween(startMillis, endMillis)
    }

    fun getContributionsForGoal(goalId: Long): Flow<List<SavingsContributionEntity>> {
        return savingsContributionDao.getContributionsForGoal(goalId)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insert(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.update(transaction)
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteById(id)
    }

    suspend fun saveCategoryBudget(budget: CategoryBudgetEntity) {
        categoryBudgetDao.insertOrUpdate(budget)
    }

    suspend fun deleteCategoryBudget(categoryName: String) {
        categoryBudgetDao.delete(categoryName)
    }

    suspend fun saveSavingsGoal(goal: SavingsGoalEntity): Long {
        return if (goal.id == 0L) {
            savingsGoalDao.insert(goal)
        } else {
            savingsGoalDao.update(goal)
            goal.id
        }
    }

    suspend fun deleteSavingsGoal(goalId: Long) {
        savingsGoalDao.deleteById(goalId)
        savingsContributionDao.deleteByGoalId(goalId)
    }

    suspend fun saveRecurringBill(bill: RecurringBillEntity): Long {
        return if (bill.id == 0L) {
            recurringBillDao.insert(bill)
        } else {
            recurringBillDao.update(bill)
            bill.id
        }
    }

    suspend fun deleteRecurringBill(id: Long) {
        recurringBillDao.deleteById(id)
    }

    suspend fun markBillPaid(bill: RecurringBillEntity, isPaid: Boolean, createExpense: Boolean) {
        recurringBillDao.update(bill.copy(isPaidThisMonth = isPaid))
        if (isPaid && createExpense) {
            transactionDao.insert(
                TransactionEntity(
                    title = "Bill: ${bill.title}",
                    amount = bill.amount,
                    type = TransactionType.EXPENSE.name,
                    category = bill.category,
                    dateMillis = System.currentTimeMillis(),
                    paymentMethod = if (bill.isAutoPaid) "Auto Pay" else "Online",
                    notes = "Monthly bill payment"
                )
            )
        }
    }

    suspend fun addGoalDeposit(
        goal: SavingsGoalEntity,
        amount: Double,
        note: String,
        createExpenseTransaction: Boolean
    ) {
        val newBalance = (goal.currentAmount + amount).coerceAtLeast(0.0)
        savingsGoalDao.update(goal.copy(currentAmount = newBalance))

        val now = System.currentTimeMillis()
        savingsContributionDao.insert(
            SavingsContributionEntity(
                goalId = goal.id,
                amount = amount,
                dateMillis = now,
                note = note.ifBlank { "Deposit to ${goal.title}" }
            )
        )

        if (createExpenseTransaction) {
            transactionDao.insert(
                TransactionEntity(
                    title = "Savings: ${goal.title}",
                    amount = amount,
                    type = TransactionType.EXPENSE.name,
                    category = "Savings & Investments",
                    dateMillis = now,
                    paymentMethod = "Transfer",
                    notes = note,
                    savingsGoalId = goal.id
                )
            )
        }
    }

    suspend fun withdrawFromGoal(
        goal: SavingsGoalEntity,
        amount: Double,
        note: String
    ) {
        val newBalance = (goal.currentAmount - amount).coerceAtLeast(0.0)
        savingsGoalDao.update(goal.copy(currentAmount = newBalance))

        val now = System.currentTimeMillis()
        savingsContributionDao.insert(
            SavingsContributionEntity(
                goalId = goal.id,
                amount = -amount,
                dateMillis = now,
                note = note.ifBlank { "Withdrawal from ${goal.title}" }
            )
        )
    }

    suspend fun clearAllData() {
        transactionDao.deleteAll()
        categoryBudgetDao.deleteAll()
        savingsGoalDao.deleteAll()
        savingsContributionDao.deleteAll()
        recurringBillDao.deleteAll()
    }

    suspend fun exportDataAsJson(): String {
        val root = JSONObject()
        val txs = transactionDao.getAllTransactionsSync()
        val budgets = categoryBudgetDao.getAllBudgetsSync()
        val goals = savingsGoalDao.getAllGoalsSync()
        val bills = recurringBillDao.getAllBillsSync()

        val txArray = JSONArray()
        txs.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("amount", t.amount)
                put("type", t.type)
                put("category", t.category)
                put("dateMillis", t.dateMillis)
                put("paymentMethod", t.paymentMethod)
                put("notes", t.notes)
            }
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val budgetArray = JSONArray()
        budgets.forEach { b ->
            val obj = JSONObject().apply {
                put("categoryName", b.categoryName)
                put("monthlyLimit", b.monthlyLimit)
                put("iconName", b.iconName)
                put("colorHex", b.colorHex)
            }
            budgetArray.put(obj)
        }
        root.put("budgets", budgetArray)

        val goalArray = JSONArray()
        goals.forEach { g ->
            val obj = JSONObject().apply {
                put("id", g.id)
                put("title", g.title)
                put("targetAmount", g.targetAmount)
                put("currentAmount", g.currentAmount)
                put("targetDateMillis", g.targetDateMillis)
                put("categoryIcon", g.categoryIcon)
                put("colorHex", g.colorHex)
                put("notes", g.notes)
            }
            goalArray.put(obj)
        }
        root.put("savingsGoals", goalArray)

        val billArray = JSONArray()
        bills.forEach { b ->
            val obj = JSONObject().apply {
                put("id", b.id)
                put("title", b.title)
                put("amount", b.amount)
                put("category", b.category)
                put("dueDayOfMonth", b.dueDayOfMonth)
                put("billingCycle", b.billingCycle)
                put("isAutoPaid", b.isAutoPaid)
            }
            billArray.put(obj)
        }
        root.put("recurringBills", billArray)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("version", "1.0")

        return root.toString(2)
    }

    suspend fun exportDataAsCsv(): String {
        val txs = transactionDao.getAllTransactionsSync()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val sb = StringBuilder()
        sb.append("ID,Date,Type,Title,Amount,Category,PaymentMethod,Notes\n")
        txs.forEach { t ->
            val dateStr = sdf.format(Date(t.dateMillis))
            val cleanTitle = t.title.replace(",", ";").replace("\"", "")
            val cleanNotes = t.notes.replace(",", ";").replace("\"", "")
            sb.append("${t.id},\"$dateStr\",${t.type},\"$cleanTitle\",${t.amount},\"${t.category}\",\"${t.paymentMethod}\",\"$cleanNotes\"\n")
        }
        return sb.toString()
    }

    suspend fun importDataFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)

            if (root.has("transactions")) {
                val txArray = root.getJSONArray("transactions")
                val list = mutableListOf<TransactionEntity>()
                for (i in 0 until txArray.length()) {
                    val obj = txArray.getJSONObject(i)
                    list.add(
                        TransactionEntity(
                            id = if (obj.has("id")) obj.getLong("id") else 0L,
                            title = obj.getString("title"),
                            amount = obj.getDouble("amount"),
                            type = obj.getString("type"),
                            category = obj.getString("category"),
                            dateMillis = obj.getLong("dateMillis"),
                            paymentMethod = if (obj.has("paymentMethod")) obj.getString("paymentMethod") else "Card",
                            notes = if (obj.has("notes")) obj.getString("notes") else ""
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    transactionDao.insertAll(list)
                }
            }

            if (root.has("budgets")) {
                val budgetArray = root.getJSONArray("budgets")
                val list = mutableListOf<CategoryBudgetEntity>()
                for (i in 0 until budgetArray.length()) {
                    val obj = budgetArray.getJSONObject(i)
                    list.add(
                        CategoryBudgetEntity(
                            categoryName = obj.getString("categoryName"),
                            monthlyLimit = obj.getDouble("monthlyLimit"),
                            iconName = if (obj.has("iconName")) obj.getString("iconName") else "category",
                            colorHex = if (obj.has("colorHex")) obj.getString("colorHex") else "#0F766E"
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    categoryBudgetDao.insertAll(list)
                }
            }

            if (root.has("savingsGoals")) {
                val goalArray = root.getJSONArray("savingsGoals")
                val list = mutableListOf<SavingsGoalEntity>()
                for (i in 0 until goalArray.length()) {
                    val obj = goalArray.getJSONObject(i)
                    list.add(
                        SavingsGoalEntity(
                            id = if (obj.has("id")) obj.getLong("id") else 0L,
                            title = obj.getString("title"),
                            targetAmount = obj.getDouble("targetAmount"),
                            currentAmount = if (obj.has("currentAmount")) obj.getDouble("currentAmount") else 0.0,
                            targetDateMillis = obj.getLong("targetDateMillis"),
                            categoryIcon = if (obj.has("categoryIcon")) obj.getString("categoryIcon") else "savings",
                            colorHex = if (obj.has("colorHex")) obj.getString("colorHex") else "#F59E0B",
                            notes = if (obj.has("notes")) obj.getString("notes") else ""
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    savingsGoalDao.insertAll(list)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importDataFromCsv(csvStr: String): Int {
        var count = 0
        try {
            val lines = csvStr.lines()
            val list = mutableListOf<TransactionEntity>()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val fallbackSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            lines.drop(1).forEach { line ->
                val parts = line.split(",").map { it.trim().trim('"') }
                if (parts.size >= 5) {
                    val dateStr = parts[1]
                    val dateMillis = try {
                        sdf.parse(dateStr)?.time ?: fallbackSdf.parse(dateStr)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                    val type = if (parts[2].equals("INCOME", ignoreCase = true)) "INCOME" else "EXPENSE"
                    val title = parts[3].ifBlank { "Imported Transaction" }
                    val amount = parts[4].toDoubleOrNull() ?: 0.0
                    val category = if (parts.size > 5) parts[5] else "General"
                    val paymentMethod = if (parts.size > 6) parts[6] else "Card"
                    val notes = if (parts.size > 7) parts[7] else ""

                    if (amount > 0) {
                        list.add(
                            TransactionEntity(
                                title = title,
                                amount = amount,
                                type = type,
                                category = category,
                                dateMillis = dateMillis,
                                paymentMethod = paymentMethod,
                                notes = notes
                            )
                        )
                        count++
                    }
                }
            }
            if (list.isNotEmpty()) {
                transactionDao.insertAll(list)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return count
    }

    suspend fun checkAndSeedInitialData() {
        if (categoryBudgetDao.getCount() == 0) {
            val defaultBudgets = listOf(
                CategoryBudgetEntity("Housing", 1200.0, "home", "#0F766E"),
                CategoryBudgetEntity("Food & Dining", 450.0, "restaurant", "#F59E0B"),
                CategoryBudgetEntity("Groceries", 400.0, "shopping_cart", "#10B981"),
                CategoryBudgetEntity("Transportation", 200.0, "directions_car", "#3B82F6"),
                CategoryBudgetEntity("Utilities", 180.0, "bolt", "#8B5CF6"),
                CategoryBudgetEntity("Entertainment", 150.0, "movie", "#EC4899"),
                CategoryBudgetEntity("Shopping", 200.0, "shopping_bag", "#F97316"),
                CategoryBudgetEntity("Healthcare", 120.0, "medical_services", "#EF4444"),
                CategoryBudgetEntity("Savings & Investments", 500.0, "savings", "#14B8A6")
            )
            categoryBudgetDao.insertAll(defaultBudgets)
        }

        if (savingsGoalDao.getCount() == 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, 6)
            val emergencyDate = cal.timeInMillis

            cal.add(Calendar.MONTH, 3)
            val japanDate = cal.timeInMillis

            cal.add(Calendar.MONTH, -5)
            val laptopDate = cal.timeInMillis

            val defaultGoals = listOf(
                SavingsGoalEntity(
                    id = 1,
                    title = "Emergency Fund",
                    targetAmount = 5000.0,
                    currentAmount = 3200.0,
                    targetDateMillis = emergencyDate,
                    categoryIcon = "shield",
                    colorHex = "#0F766E",
                    notes = "6 months of essential living expenses"
                ),
                SavingsGoalEntity(
                    id = 2,
                    title = "Tokyo Vacation",
                    targetAmount = 3500.0,
                    currentAmount = 1850.0,
                    targetDateMillis = japanDate,
                    categoryIcon = "flight",
                    colorHex = "#F59E0B",
                    notes = "Flights, hotels, and travel experiences"
                ),
                SavingsGoalEntity(
                    id = 3,
                    title = "New Workstation",
                    targetAmount = 1800.0,
                    currentAmount = 1350.0,
                    targetDateMillis = laptopDate,
                    categoryIcon = "laptop",
                    colorHex = "#3B82F6",
                    notes = "Upgrading laptop and 4K display"
                )
            )
            savingsGoalDao.insertAll(defaultGoals)
        }

        if (recurringBillDao.getCount() == 0) {
            val defaultBills = listOf(
                RecurringBillEntity(
                    title = "Apartment Rent",
                    amount = 1200.0,
                    category = "Housing",
                    dueDayOfMonth = 1,
                    billingCycle = "Monthly",
                    isAutoPaid = true,
                    isPaidThisMonth = true
                ),
                RecurringBillEntity(
                    title = "Fiber Internet",
                    amount = 65.0,
                    category = "Utilities",
                    dueDayOfMonth = 15,
                    billingCycle = "Monthly",
                    isAutoPaid = true,
                    isPaidThisMonth = false
                ),
                RecurringBillEntity(
                    title = "Netflix Premium",
                    amount = 19.99,
                    category = "Entertainment",
                    dueDayOfMonth = 18,
                    billingCycle = "Monthly",
                    isAutoPaid = true,
                    isPaidThisMonth = false
                ),
                RecurringBillEntity(
                    title = "Gym Membership",
                    amount = 45.0,
                    category = "Healthcare",
                    dueDayOfMonth = 25,
                    billingCycle = "Monthly",
                    isAutoPaid = false,
                    isPaidThisMonth = false
                )
            )
            recurringBillDao.insertAll(defaultBills)
        }

        if (transactionDao.getCount() == 0) {
            val now = Calendar.getInstance()
            val baseTime = now.timeInMillis
            val oneDay = 86_400_000L

            val sampleTransactions = listOf(
                TransactionEntity(
                    title = "Monthly Paycheck",
                    amount = 4500.0,
                    type = TransactionType.INCOME.name,
                    category = "Income",
                    dateMillis = baseTime - (12 * oneDay),
                    paymentMethod = "Direct Deposit",
                    notes = "Primary salary deposit"
                ),
                TransactionEntity(
                    title = "Apartment Rent",
                    amount = 1200.0,
                    type = TransactionType.EXPENSE.name,
                    category = "Housing",
                    dateMillis = baseTime - (10 * oneDay),
                    paymentMethod = "Bank Transfer",
                    notes = "October rent"
                ),
                TransactionEntity(
                    title = "Fresh Market Groceries",
                    amount = 92.40,
                    type = TransactionType.EXPENSE.name,
                    category = "Groceries",
                    dateMillis = baseTime - (7 * oneDay),
                    paymentMethod = "Debit Card",
                    notes = "Weekly grocery run"
                ),
                TransactionEntity(
                    title = "Electric Utility",
                    amount = 76.80,
                    type = TransactionType.EXPENSE.name,
                    category = "Utilities",
                    dateMillis = baseTime - (5 * oneDay),
                    paymentMethod = "Auto Pay",
                    notes = "Power bill"
                ),
                TransactionEntity(
                    title = "Artisan Bistro Dinner",
                    amount = 64.50,
                    type = TransactionType.EXPENSE.name,
                    category = "Food & Dining",
                    dateMillis = baseTime - (3 * oneDay),
                    paymentMethod = "Credit Card",
                    notes = "Dinner with friends"
                ),
                TransactionEntity(
                    title = "Subway Commute Pass",
                    amount = 45.00,
                    type = TransactionType.EXPENSE.name,
                    category = "Transportation",
                    dateMillis = baseTime - (2 * oneDay),
                    paymentMethod = "Credit Card",
                    notes = "Monthly commute pass"
                ),
                TransactionEntity(
                    title = "Freelance Consulting",
                    amount = 620.0,
                    type = TransactionType.INCOME.name,
                    category = "Income",
                    dateMillis = baseTime - (2 * oneDay),
                    paymentMethod = "Bank Transfer",
                    notes = "Web app design project"
                ),
                TransactionEntity(
                    title = "Movie Night Tickets",
                    amount = 32.00,
                    type = TransactionType.EXPENSE.name,
                    category = "Entertainment",
                    dateMillis = baseTime - (1 * oneDay),
                    paymentMethod = "Credit Card",
                    notes = "Cinema tickets & popcorn"
                ),
                TransactionEntity(
                    title = "Local Coffee Roasters",
                    amount = 8.50,
                    type = TransactionType.EXPENSE.name,
                    category = "Food & Dining",
                    dateMillis = baseTime,
                    paymentMethod = "Debit Card",
                    notes = "Morning flat white"
                )
            )
            transactionDao.insertAll(sampleTransactions)
        }
    }
}
