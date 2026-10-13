package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.RecurringBillEntity
import com.example.data.model.SavingsContributionEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC")
    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getCount(): Int
}

@Dao
interface CategoryBudgetDao {
    @Query("SELECT * FROM category_budgets ORDER BY categoryName ASC")
    fun getAllBudgets(): Flow<List<CategoryBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(budget: CategoryBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<CategoryBudgetEntity>)

    @Query("DELETE FROM category_budgets WHERE categoryName = :categoryName")
    suspend fun delete(categoryName: String)

    @Query("DELETE FROM category_budgets")
    suspend fun deleteAll()

    @Query("SELECT * FROM category_budgets ORDER BY categoryName ASC")
    suspend fun getAllBudgetsSync(): List<CategoryBudgetEntity>

    @Query("SELECT COUNT(*) FROM category_budgets")
    suspend fun getCount(): Int
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY targetDateMillis ASC")
    fun getAllGoals(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    fun getGoalById(id: Long): Flow<SavingsGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingsGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<SavingsGoalEntity>)

    @Update
    suspend fun update(goal: SavingsGoalEntity)

    @Delete
    suspend fun delete(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM savings_goals")
    suspend fun deleteAll()

    @Query("SELECT * FROM savings_goals")
    suspend fun getAllGoalsSync(): List<SavingsGoalEntity>

    @Query("SELECT COUNT(*) FROM savings_goals")
    suspend fun getCount(): Int
}

@Dao
interface SavingsContributionDao {
    @Query("SELECT * FROM savings_contributions WHERE goalId = :goalId ORDER BY dateMillis DESC")
    fun getContributionsForGoal(goalId: Long): Flow<List<SavingsContributionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contribution: SavingsContributionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contributions: List<SavingsContributionEntity>)

    @Query("DELETE FROM savings_contributions WHERE goalId = :goalId")
    suspend fun deleteByGoalId(goalId: Long)

    @Query("DELETE FROM savings_contributions")
    suspend fun deleteAll()

    @Query("SELECT * FROM savings_contributions")
    suspend fun getAllContributionsSync(): List<SavingsContributionEntity>
}

@Dao
interface RecurringBillDao {
    @Query("SELECT * FROM recurring_bills ORDER BY dueDayOfMonth ASC")
    fun getAllBills(): Flow<List<RecurringBillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: RecurringBillEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bills: List<RecurringBillEntity>)

    @Update
    suspend fun update(bill: RecurringBillEntity)

    @Query("DELETE FROM recurring_bills WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recurring_bills")
    suspend fun deleteAll()

    @Query("SELECT * FROM recurring_bills ORDER BY dueDayOfMonth ASC")
    suspend fun getAllBillsSync(): List<RecurringBillEntity>

    @Query("SELECT COUNT(*) FROM recurring_bills")
    suspend fun getCount(): Int
}
