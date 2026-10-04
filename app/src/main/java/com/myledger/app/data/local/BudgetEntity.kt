package com.myledger.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: ExpenseCategory,
    val monthlyLimit: Double,
    val monthKey: String,
    val createdAt: Long = System.currentTimeMillis()
)
