package com.myledger.app.data.parser

import com.myledger.app.data.local.*
import java.util.Locale

abstract class ProviderParser(
    private val source: PaymentSource,
    private val keywords: List<String>
) : SmsParser {
    override fun canParse(sender: String, body: String): Boolean {
        val s = "$sender $body".lowercase(Locale.ROOT)
        return keywords.any { s.contains(it) }
    }

    override fun parse(sender: String, body: String): ParsedSms? {
        val amount = Regex(
            """(?i)(?:ETB|Birr|ብር)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)|([0-9][0-9,]*(?:\.[0-9]{1,2})?)\s*(?:ETB|Birr|ብር)"""
        ).find(body)?.let { m ->
            (m.groupValues[1].ifBlank { m.groupValues[2] }).replace(",","").toDoubleOrNull()
        } ?: return null

        val lower = body.lowercase()
        val income = listOf("received","credited","deposit","salary","income","ገቢ","ገብቷል","ደርሷል")
            .any { lower.contains(it) }
        val transfer = listOf("transfer","transferred","ማስተላለፍ").any { lower.contains(it) }
        val type = when {
            income -> TransactionType.INCOME
            transfer -> TransactionType.TRANSFER
            else -> TransactionType.EXPENSE
        }

        val vendor = Regex(
            """(?i)(?:to|merchant|vendor|from|at|paid\s+to|payment\s+to)\s*[:\-]?\s*([A-Za-z][A-Za-z0-9 .&'_-]{2,60})"""
        ).find(body)?.groupValues?.get(1)?.trim()?.trimEnd('.',',',';') ?: "Unknown vendor"

        val phone = Regex("""(?<!\d)(?:\+251|0)9\d{8}(?!\d)""").find(body)?.value
        val ref = Regex(
            """(?i)(?:reference|ref|transaction\s*(?:id|no)|txn|ቁጥር)\s*[:#\-]?\s*([A-Za-z0-9-]{4,50})"""
        ).find(body)?.groupValues?.get(1)

        val category = when(type) {
            TransactionType.INCOME -> ExpenseCategory.SALARY
            TransactionType.TRANSFER -> ExpenseCategory.TRANSFER
            TransactionType.EXPENSE -> CategoryEngine.categorize(vendor, body)
        }

        return ParsedSms(amount,type,vendor,category,source,phone,ref,80)
    }
}
class CbeParser : ProviderParser(PaymentSource.CBE, listOf("cbe","commercial bank of ethiopia","cbe birr"))
class TelebirrParser : ProviderParser(PaymentSource.TELEBIRR, listOf("telebirr"))
class MpesaParser : ProviderParser(PaymentSource.MPESA, listOf("m-pesa","m pesa","mpesa","safaricom"))
class ParserManager {
    private val parsers = listOf(CbeParser(),TelebirrParser(),MpesaParser())
    fun parse(sender: String, body: String): ParsedSms? =
        parsers.firstOrNull { it.canParse(sender,body) }?.parse(sender,body)
}
