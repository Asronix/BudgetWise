package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.RecurringBillEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.CurrencyOption
import com.example.data.preferences.UserPreferences
import com.example.data.repository.BudgetRepository
import com.example.ui.components.Formatters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthYear(
    val year: Int,
    val month: Int // 0-indexed
) {
    val monthName: String
        get() = java.text.DateFormatSymbols().months.getOrNull(month) ?: "Month"
}

data class MonthSummary(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netBalance: Double = 0.0,
    val totalBudgetCap: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val budgetUsedFraction: Float = 0f,
    val categorySpending: Map<String, Double> = emptyMap(),
    val healthScore: Int = 85
)

enum class ReportTimeFrame(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

data class ChartBucket(
    val label: String,
    val expense: Double,
    val income: Double
)

data class TimeFrameReport(
    val timeFrame: ReportTimeFrame = ReportTimeFrame.MONTH,
    val periodLabel: String = "",
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netSavings: Double = 0.0,
    val transactionCount: Int = 0,
    val dailyAverageExpense: Double = 0.0,
    val largestExpense: TransactionEntity? = null,
    val categoryBreakdown: List<Pair<String, Double>> = emptyList(),
    val chartBuckets: List<ChartBucket> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList()
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BudgetRepository
    private val userPreferences = UserPreferences(application)

    private val _themeMode = MutableStateFlow(userPreferences.getThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _selectedCurrency = MutableStateFlow(userPreferences.getCurrency())
    val selectedCurrency: StateFlow<CurrencyOption> = _selectedCurrency.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BudgetRepository(
            transactionDao = db.transactionDao(),
            categoryBudgetDao = db.categoryBudgetDao(),
            savingsGoalDao = db.savingsGoalDao(),
            savingsContributionDao = db.savingsContributionDao(),
            recurringBillDao = db.recurringBillDao()
        )
        Formatters.activeCurrencySymbol = _selectedCurrency.value.symbol
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        userPreferences.setThemeMode(mode)
    }

    fun setCurrency(currency: CurrencyOption) {
        _selectedCurrency.value = currency
        userPreferences.setCurrency(currency)
        Formatters.activeCurrencySymbol = currency.symbol
    }

    // Daily Transaction Reminder settings
    private val _morningReminderEnabled = MutableStateFlow(userPreferences.isMorningReminderEnabled())
    val morningReminderEnabled: StateFlow<Boolean> = _morningReminderEnabled.asStateFlow()

    private val _morningReminderTime = MutableStateFlow(
        Pair(userPreferences.getMorningHour(), userPreferences.getMorningMinute())
    )
    val morningReminderTime: StateFlow<Pair<Int, Int>> = _morningReminderTime.asStateFlow()

    private val _eveningReminderEnabled = MutableStateFlow(userPreferences.isEveningReminderEnabled())
    val eveningReminderEnabled: StateFlow<Boolean> = _eveningReminderEnabled.asStateFlow()

    private val _eveningReminderTime = MutableStateFlow(
        Pair(userPreferences.getEveningHour(), userPreferences.getEveningMinute())
    )
    val eveningReminderTime: StateFlow<Pair<Int, Int>> = _eveningReminderTime.asStateFlow()

    fun setMorningReminderEnabled(enabled: Boolean) {
        _morningReminderEnabled.value = enabled
        userPreferences.setMorningReminderEnabled(enabled)
        val app = getApplication<Application>()
        if (enabled) {
            com.example.notifications.TransactionReminderScheduler.scheduleStartOfDay(app)
        } else {
            com.example.notifications.TransactionReminderScheduler.cancelStartOfDay(app)
        }
    }

    fun setMorningReminderTime(hour: Int, minute: Int) {
        _morningReminderTime.value = Pair(hour, minute)
        userPreferences.setMorningTime(hour, minute)
        if (_morningReminderEnabled.value) {
            com.example.notifications.TransactionReminderScheduler.scheduleStartOfDay(getApplication())
        }
    }

    fun setEveningReminderEnabled(enabled: Boolean) {
        _eveningReminderEnabled.value = enabled
        userPreferences.setEveningReminderEnabled(enabled)
        val app = getApplication<Application>()
        if (enabled) {
            com.example.notifications.TransactionReminderScheduler.scheduleEndOfDay(app)
        } else {
            com.example.notifications.TransactionReminderScheduler.cancelEndOfDay(app)
        }
    }

    fun setEveningReminderTime(hour: Int, minute: Int) {
        _eveningReminderTime.value = Pair(hour, minute)
        userPreferences.setEveningTime(hour, minute)
        if (_eveningReminderEnabled.value) {
            com.example.notifications.TransactionReminderScheduler.scheduleEndOfDay(getApplication())
        }
    }

    fun triggerTestNotification() {
        com.example.notifications.TransactionReminderScheduler.triggerImmediateTestReminder(getApplication())
    }

    private val calendar = Calendar.getInstance()
    private val _currentMonthYear = MutableStateFlow(
        MonthYear(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH)
        )
    )
    val currentMonthYear: StateFlow<MonthYear> = _currentMonthYear.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyTransactions: StateFlow<List<TransactionEntity>> = _currentMonthYear
        .flatMapLatest { my ->
            repository.getTransactionsForMonth(my.year, my.month)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categoryBudgets: StateFlow<List<CategoryBudgetEntity>> = repository.allCategoryBudgets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.allSavingsGoals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recurringBills: StateFlow<List<RecurringBillEntity>> = repository.allRecurringBills
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val monthSummary: StateFlow<MonthSummary> = combine(
        monthlyTransactions,
        categoryBudgets,
        savingsGoals
    ) { transactions, budgets, goals ->
        var income = 0.0
        var expenses = 0.0
        val catSpend = mutableMapOf<String, Double>()

        for (t in transactions) {
            if (t.type.equals(TransactionType.INCOME.name, ignoreCase = true)) {
                income += t.amount
            } else {
                expenses += t.amount
                val current = catSpend.getOrDefault(t.category, 0.0)
                catSpend[t.category] = current + t.amount
            }
        }

        val totalBudget = budgets.sumOf { it.monthlyLimit }
        val remaining = (totalBudget - expenses).coerceAtLeast(0.0)
        val usedFraction = if (totalBudget > 0) {
            (expenses / totalBudget).toFloat().coerceIn(0f, 2f)
        } else 0f

        // Financial health score (0-100)
        var score = 50
        val savingsRate = if (income > 0) (income - expenses) / income else 0.0
        if (savingsRate > 0.20) score += 25 else if (savingsRate > 0.10) score += 15
        if (usedFraction in 0.01f..0.90f) score += 15 else if (usedFraction > 1.0f) score -= 20
        if (goals.any { it.progressFraction > 0.5f }) score += 10
        score = score.coerceIn(10, 100)

        MonthSummary(
            totalIncome = income,
            totalExpenses = expenses,
            netBalance = income - expenses,
            totalBudgetCap = totalBudget,
            remainingBudget = remaining,
            budgetUsedFraction = usedFraction,
            categorySpending = catSpend,
            healthScore = score
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthSummary()
    )

    // Transaction Search & Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilterTarget = MutableStateFlow("ALL") // "ALL", "MERCHANT", "NOTES"
    val searchFilterTarget: StateFlow<String> = _searchFilterTarget.asStateFlow()

    private val _searchScope = MutableStateFlow("THIS_MONTH") // "THIS_MONTH", "ALL_TIME"
    val searchScope: StateFlow<String> = _searchScope.asStateFlow()

    private val _typeFilter = MutableStateFlow("ALL") // "ALL", "EXPENSE", "INCOME"
    val typeFilter: StateFlow<String> = _typeFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchFilterTarget(target: String) {
        _searchFilterTarget.value = target
    }

    fun setSearchScope(scope: String) {
        _searchScope.value = scope
    }

    fun setTypeFilter(filter: String) {
        _typeFilter.value = filter
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    fun clearSearchFilters() {
        _searchQuery.value = ""
        _searchFilterTarget.value = "ALL"
        _searchScope.value = "THIS_MONTH"
        _typeFilter.value = "ALL"
        _categoryFilter.value = null
    }

    fun nextMonth() {
        val current = _currentMonthYear.value
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, current.year)
            set(Calendar.MONTH, current.month)
            add(Calendar.MONTH, 1)
        }
        _currentMonthYear.value = MonthYear(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH)
        )
    }

    fun previousMonth() {
        val current = _currentMonthYear.value
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, current.year)
            set(Calendar.MONTH, current.month)
            add(Calendar.MONTH, -1)
        }
        _currentMonthYear.value = MonthYear(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH)
        )
    }

    fun resetToCurrentMonth() {
        val cal = Calendar.getInstance()
        _currentMonthYear.value = MonthYear(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH)
        )
    }

    // --- Timeframe Reports State (Day / Week / Month / Year) ---
    private val _reportTimeFrame = MutableStateFlow(ReportTimeFrame.MONTH)
    val reportTimeFrame: StateFlow<ReportTimeFrame> = _reportTimeFrame.asStateFlow()

    private val _reportAnchorMillis = MutableStateFlow(System.currentTimeMillis())
    val reportAnchorMillis: StateFlow<Long> = _reportAnchorMillis.asStateFlow()

    fun setReportTimeFrame(timeFrame: ReportTimeFrame) {
        _reportTimeFrame.value = timeFrame
    }

    fun nextReportPeriod() {
        val cal = Calendar.getInstance().apply { timeInMillis = _reportAnchorMillis.value }
        when (_reportTimeFrame.value) {
            ReportTimeFrame.DAY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            ReportTimeFrame.WEEK -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            ReportTimeFrame.MONTH -> cal.add(Calendar.MONTH, 1)
            ReportTimeFrame.YEAR -> cal.add(Calendar.YEAR, 1)
        }
        _reportAnchorMillis.value = cal.timeInMillis
    }

    fun prevReportPeriod() {
        val cal = Calendar.getInstance().apply { timeInMillis = _reportAnchorMillis.value }
        when (_reportTimeFrame.value) {
            ReportTimeFrame.DAY -> cal.add(Calendar.DAY_OF_YEAR, -1)
            ReportTimeFrame.WEEK -> cal.add(Calendar.WEEK_OF_YEAR, -1)
            ReportTimeFrame.MONTH -> cal.add(Calendar.MONTH, -1)
            ReportTimeFrame.YEAR -> cal.add(Calendar.YEAR, -1)
        }
        _reportAnchorMillis.value = cal.timeInMillis
    }

    fun resetReportPeriodToToday() {
        _reportAnchorMillis.value = System.currentTimeMillis()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val timeframeReport: StateFlow<TimeFrameReport> = combine(
        _reportTimeFrame,
        _reportAnchorMillis,
        allTransactions
    ) { timeFrame, anchorMillis, allTxs ->
        calculateTimeFrameReport(timeFrame, anchorMillis, allTxs)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimeFrameReport()
    )

    private fun calculateTimeFrameReport(
        timeFrame: ReportTimeFrame,
        anchorMillis: Long,
        allTxs: List<TransactionEntity>
    ): TimeFrameReport {
        val cal = Calendar.getInstance().apply { timeInMillis = anchorMillis }
        val startCal = cal.clone() as Calendar
        val endCal = cal.clone() as Calendar

        var periodLabel = ""
        var numDays = 1

        when (timeFrame) {
            ReportTimeFrame.DAY -> {
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)

                endCal.set(Calendar.HOUR_OF_DAY, 23)
                endCal.set(Calendar.MINUTE, 59)
                endCal.set(Calendar.SECOND, 59)
                endCal.set(Calendar.MILLISECOND, 999)

                val sdf = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
                periodLabel = sdf.format(startCal.time)
                numDays = 1
            }
            ReportTimeFrame.WEEK -> {
                startCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)

                endCal.timeInMillis = startCal.timeInMillis
                endCal.add(Calendar.DAY_OF_YEAR, 6)
                endCal.set(Calendar.HOUR_OF_DAY, 23)
                endCal.set(Calendar.MINUTE, 59)
                endCal.set(Calendar.SECOND, 59)
                endCal.set(Calendar.MILLISECOND, 999)

                val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                val yearSdf = SimpleDateFormat("yyyy", Locale.getDefault())
                periodLabel = "${sdf.format(startCal.time)} - ${sdf.format(endCal.time)}, ${yearSdf.format(endCal.time)}"
                numDays = 7
            }
            ReportTimeFrame.MONTH -> {
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)

                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                endCal.set(Calendar.HOUR_OF_DAY, 23)
                endCal.set(Calendar.MINUTE, 59)
                endCal.set(Calendar.SECOND, 59)
                endCal.set(Calendar.MILLISECOND, 999)

                val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                periodLabel = sdf.format(startCal.time)
                numDays = endCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            }
            ReportTimeFrame.YEAR -> {
                startCal.set(Calendar.DAY_OF_YEAR, 1)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)

                endCal.set(Calendar.MONTH, Calendar.DECEMBER)
                endCal.set(Calendar.DAY_OF_MONTH, 31)
                endCal.set(Calendar.HOUR_OF_DAY, 23)
                endCal.set(Calendar.MINUTE, 59)
                endCal.set(Calendar.SECOND, 59)
                endCal.set(Calendar.MILLISECOND, 999)

                val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
                periodLabel = "Year ${sdf.format(startCal.time)}"
                numDays = 365
            }
        }

        val start = startCal.timeInMillis
        val end = endCal.timeInMillis

        val periodTxs = allTxs.filter { it.dateMillis in start..end }
        var totalIncome = 0.0
        var totalExpense = 0.0
        val catMap = mutableMapOf<String, Double>()
        var maxExpenseTx: TransactionEntity? = null

        for (tx in periodTxs) {
            if (tx.type.equals("INCOME", ignoreCase = true)) {
                totalIncome += tx.amount
            } else {
                totalExpense += tx.amount
                catMap[tx.category] = (catMap[tx.category] ?: 0.0) + tx.amount
                if (maxExpenseTx == null || tx.amount > maxExpenseTx.amount) {
                    maxExpenseTx = tx
                }
            }
        }

        val buckets = mutableListOf<ChartBucket>()
        when (timeFrame) {
            ReportTimeFrame.DAY -> {
                // 4-hour intervals
                val intervalLabels = listOf("12 AM", "4 AM", "8 AM", "12 PM", "4 PM", "8 PM")
                for (i in 0..5) {
                    val bucketStart = start + (i * 4 * 3600_000L)
                    val bucketEnd = bucketStart + (4 * 3600_000L)
                    val bucketTxs = periodTxs.filter { it.dateMillis in bucketStart until bucketEnd }
                    val exp = bucketTxs.filter { it.type.equals("EXPENSE", true) }.sumOf { it.amount }
                    val inc = bucketTxs.filter { it.type.equals("INCOME", true) }.sumOf { it.amount }
                    buckets.add(ChartBucket(intervalLabels[i], exp, inc))
                }
            }
            ReportTimeFrame.WEEK -> {
                val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                for (i in 0..6) {
                    val bucketCal = startCal.clone() as Calendar
                    bucketCal.add(Calendar.DAY_OF_YEAR, i)
                    val bStart = bucketCal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
                    val bEnd = bucketCal.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                    val bucketTxs = periodTxs.filter { it.dateMillis in bStart..bEnd }
                    val exp = bucketTxs.filter { it.type.equals("EXPENSE", true) }.sumOf { it.amount }
                    val inc = bucketTxs.filter { it.type.equals("INCOME", true) }.sumOf { it.amount }
                    buckets.add(ChartBucket(dayLabels[i % 7], exp, inc))
                }
            }
            ReportTimeFrame.MONTH -> {
                // 4 distinct weeks in month
                for (w in 1..4) {
                    val daysInQuarter = (numDays / 4).coerceAtLeast(7)
                    val bStart = start + ((w - 1) * daysInQuarter * 86_400_000L)
                    val bEnd = if (w == 4) end else bStart + (daysInQuarter * 86_400_000L)
                    val bucketTxs = periodTxs.filter { it.dateMillis in bStart..bEnd }
                    val exp = bucketTxs.filter { it.type.equals("EXPENSE", true) }.sumOf { it.amount }
                    val inc = bucketTxs.filter { it.type.equals("INCOME", true) }.sumOf { it.amount }
                    buckets.add(ChartBucket("W$w", exp, inc))
                }
            }
            ReportTimeFrame.YEAR -> {
                val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                for (m in 0..11) {
                    val mCal = startCal.clone() as Calendar
                    mCal.set(Calendar.MONTH, m)
                    mCal.set(Calendar.DAY_OF_MONTH, 1)
                    val mStart = mCal.timeInMillis
                    mCal.set(Calendar.DAY_OF_MONTH, mCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                    mCal.set(Calendar.HOUR_OF_DAY, 23); mCal.set(Calendar.MINUTE, 59); mCal.set(Calendar.SECOND, 59)
                    val mEnd = mCal.timeInMillis
                    val bucketTxs = periodTxs.filter { it.dateMillis in mStart..mEnd }
                    val exp = bucketTxs.filter { it.type.equals("EXPENSE", true) }.sumOf { it.amount }
                    val inc = bucketTxs.filter { it.type.equals("INCOME", true) }.sumOf { it.amount }
                    buckets.add(ChartBucket(monthNames[m], exp, inc))
                }
            }
        }

        return TimeFrameReport(
            timeFrame = timeFrame,
            periodLabel = periodLabel,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netSavings = totalIncome - totalExpense,
            transactionCount = periodTxs.size,
            dailyAverageExpense = if (numDays > 0) totalExpense / numDays else totalExpense,
            largestExpense = maxExpenseTx,
            categoryBreakdown = catMap.entries.map { it.key to it.value }.sortedByDescending { it.second },
            chartBuckets = buckets,
            transactions = periodTxs
        )
    }

    // CRUD operations
    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        dateMillis: Long,
        paymentMethod: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    title = title.trim(),
                    amount = amount,
                    type = type.name,
                    category = category.trim(),
                    dateMillis = dateMillis,
                    paymentMethod = paymentMethod,
                    notes = notes.trim()
                )
            )
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun updateCategoryBudget(category: String, limit: Double) {
        viewModelScope.launch {
            val existing = categoryBudgets.value.find { it.categoryName.equals(category, ignoreCase = true) }
            val updated = existing?.copy(monthlyLimit = limit)
                ?: CategoryBudgetEntity(categoryName = category, monthlyLimit = limit)
            repository.saveCategoryBudget(updated)
        }
    }

    fun addSavingsGoal(
        title: String,
        targetAmount: Double,
        initialDeposit: Double,
        targetDateMillis: Long,
        categoryIcon: String,
        colorHex: String,
        notes: String
    ) {
        viewModelScope.launch {
            val goal = SavingsGoalEntity(
                title = title.trim(),
                targetAmount = targetAmount,
                currentAmount = initialDeposit,
                targetDateMillis = targetDateMillis,
                categoryIcon = categoryIcon,
                colorHex = colorHex,
                notes = notes.trim()
            )
            val newId = repository.saveSavingsGoal(goal)
            if (initialDeposit > 0) {
                repository.addGoalDeposit(
                    goal = goal.copy(id = newId, currentAmount = 0.0),
                    amount = initialDeposit,
                    note = "Initial deposit",
                    createExpenseTransaction = false
                )
            }
        }
    }

    fun depositToSavingsGoal(
        goal: SavingsGoalEntity,
        amount: Double,
        note: String,
        deductFromMonthlyBudget: Boolean
    ) {
        viewModelScope.launch {
            repository.addGoalDeposit(
                goal = goal,
                amount = amount,
                note = note,
                createExpenseTransaction = deductFromMonthlyBudget
            )
        }
    }

    fun withdrawFromSavingsGoal(
        goal: SavingsGoalEntity,
        amount: Double,
        note: String
    ) {
        viewModelScope.launch {
            repository.withdrawFromGoal(
                goal = goal,
                amount = amount,
                note = note
            )
        }
    }

    fun deleteSavingsGoal(goalId: Long) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goalId)
        }
    }

    // Recurring Bills
    fun addRecurringBill(title: String, amount: Double, category: String, dueDay: Int, isAutoPay: Boolean) {
        viewModelScope.launch {
            repository.saveRecurringBill(
                RecurringBillEntity(
                    title = title.trim(),
                    amount = amount,
                    category = category.trim(),
                    dueDayOfMonth = dueDay,
                    isAutoPaid = isAutoPay
                )
            )
        }
    }

    fun deleteRecurringBill(id: Long) {
        viewModelScope.launch {
            repository.deleteRecurringBill(id)
        }
    }

    fun toggleBillPaid(bill: RecurringBillEntity, isPaid: Boolean, createExpense: Boolean) {
        viewModelScope.launch {
            repository.markBillPaid(bill, isPaid, createExpense)
        }
    }

    // Data Management: Clear All & Import/Export
    fun clearAllRecords() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun resetSampleData() {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    suspend fun exportDataJson(): String {
        return repository.exportDataAsJson()
    }

    suspend fun exportDataCsv(): String {
        return repository.exportDataAsCsv()
    }

    suspend fun importDataJson(jsonStr: String): Boolean {
        return repository.importDataFromJson(jsonStr)
    }

    suspend fun importDataCsv(csvStr: String): Int {
        return repository.importDataFromCsv(csvStr)
    }
}
