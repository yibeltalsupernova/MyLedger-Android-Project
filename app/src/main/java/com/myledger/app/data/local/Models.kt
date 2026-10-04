package com.myledger.app.data.local

enum class TransactionType { INCOME, EXPENSE, TRANSFER }
enum class ExpenseCategory {
    FOOD, TRANSPORT, SHOPPING, UTILITIES, RENT, AIRTIME,
    ENTERTAINMENT, HEALTH, EDUCATION, SALARY, TRANSFER, OTHER
}
enum class PaymentSource { CBE, TELEBIRR, MPESA, OTHER, MANUAL }
enum class Recurrence { NONE, DAILY, WEEKLY, MONTHLY, YEARLY }
