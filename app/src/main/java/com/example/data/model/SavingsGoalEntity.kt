package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDateMillis: Long,
    val categoryIcon: String = "savings",
    val colorHex: String = "#F59E0B",
    val notes: String = ""
) {
    val progressFraction: Float
        get() = if (targetAmount > 0) {
            (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f)
        } else 0f

    val isCompleted: Boolean
        get() = currentAmount >= targetAmount && targetAmount > 0
}
