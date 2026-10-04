package com.myledger.app.data.parser

import com.myledger.app.data.local.*

data class ParsedSms(
    val amount: Double,
    val type: TransactionType,
    val vendor: String,
    val category: ExpenseCategory,
    val source: PaymentSource,
    val accountOrPhone: String? = null,
    val reference: String? = null,
    val confidence: Int = 70
)

interface SmsParser {
    fun canParse(sender: String, body: String): Boolean
    fun parse(sender: String, body: String): ParsedSms?
}
