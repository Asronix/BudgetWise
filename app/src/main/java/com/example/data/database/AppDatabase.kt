package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        TransactionEntity::class,
        CategoryBudgetEntity::class,
        SavingsGoalEntity::class,
        SavingsContributionEntity::class,
        RecurringBillEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun savingsContributionDao(): SavingsContributionDao
    abstract fun recurringBillDao(): RecurringBillDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgetwise_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
