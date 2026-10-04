package com.myledger.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index("timestamp"),
        Index("vendor"),
        Index("category"),
        Index("source"),
        Index(value = ["smsHash"], unique = true)
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val vendor: String,
    val category: ExpenseCategory,
    val source: PaymentSource,
    val accountOrPhone: String? = null,
    val reference: String? = null,
    val note: String? = null,
    val originalSms: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val smsHash: String? = null,
    val isAutoParsed: Boolean = false,
    val isRecurring: Boolean = false,
    val recurrence: Recurrence = Recurrence.NONE,
    val budgetId: Long? = null
)
