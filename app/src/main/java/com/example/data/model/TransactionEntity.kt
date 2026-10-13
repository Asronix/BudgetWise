package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String,
    val dateMillis: Long,
    val paymentMethod: String = "Card",
    val notes: String = "",
    val savingsGoalId: Long? = null
)
