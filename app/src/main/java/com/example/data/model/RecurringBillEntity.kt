package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_bills")
data class RecurringBillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String = "Utilities",
    val dueDayOfMonth: Int = 1,
    val billingCycle: String = "Monthly", // "Monthly", "Weekly", "Yearly"
    val isAutoPaid: Boolean = false,
    val isPaidThisMonth: Boolean = false
)
