package com.myledger.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["vendor"]),
        Index(value = ["category"]),
        Index(value = ["source"]),
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
    val isAutoParsed: Boolean = false
)

enum class TransactionType { INCOME, EXPENSE, TRANSFER }

enum class ExpenseCategory {
    FOOD, TRANSPORT, SHOPPING, UTILITIES, RENT, AIRTIME,
    ENTERTAINMENT, HEALTH, EDUCATION, TRANSFER, SALARY, OTHER
}

enum class PaymentSource { CBE, TELEBIRR, MPESA, OTHER, MANUAL }
