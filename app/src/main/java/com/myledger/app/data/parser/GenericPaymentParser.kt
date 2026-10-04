package com.myledger.app.data.parser

import com.myledger.app.data.local.*
import java.util.Locale

abstract class GenericPaymentParser(
    private val source: PaymentSource,
    private val senderKeywords: List<String>,
    private val extraKeywords: List<String> = emptyList()
) : SmsParser {

    override fun canParse(sender: String, body: String): Boolean {
        val text = "$sender $body".lowercase(Locale.ROOT)
        return senderKeywords.any { text.contains(it) } ||
                extraKeywords.any { text.contains(it) }
    }

    override fun parse(sender: String, body: String): ParsedSms? {
        val amount = extractAmount(body) ?: return null
        val lower = body.lowercase(Locale.ROOT)

        val isIncome = listOf(
            "received", "credited", "deposit", "salary", "income",
            "ተቀብለዋል", "ገቢ", "ገብቷል", "ደርሷል"
        ).any { lower.contains(it) }

        val isTransfer = listOf(
            "transfer", "transferred", "ገንዘብ ልከዋል", "ማስተላለፍ"
        ).any { lower.contains(it) }

        val isExpense = listOf(
            "sent", "paid", "payment", "debited", "purchase", "withdraw",
            "ወጪ", "ከፍለዋል", "ወጥቷል"
        ).any { lower.contains(it) }

        val type = when {
            isIncome -> TransactionType.INCOME
            isTransfer && !isExpense -> TransactionType.TRANSFER
            else -> TransactionType.EXPENSE
        }

        val vendor = extractVendor(body)
        val category = when (type) {
            TransactionType.INCOME -> ExpenseCategory.SALARY
            TransactionType.TRANSFER -> ExpenseCategory.TRANSFER
            TransactionType.EXPENSE -> CategoryEngine.categorize(vendor, body)
        }

        return ParsedSms(
            amount = amount,
            type = type,
            vendor = vendor,
            category = category,
            source = source,
            accountOrPhone = extractPhone(body),
            reference = extractReference(body),
            confidence = calculateConfidence(body, vendor)
        )
    }

    private fun extractAmount(body: String): Double? {
        val patterns = listOf(
            Regex("""(?i)(?:ETB|Birr|ብር)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)"""),
            Regex("""(?i)([0-9][0-9,]*(?:\.[0-9]{1,2})?)\s*(?:ETB|Birr|ብር)"""),
            Regex("""(?i)(?:amount|amt|amount\s+is)\D{0,25}([0-9][0-9,]*(?:\.[0-9]{1,2})?)""")
        )
        return patterns.asSequence()
            .mapNotNull { it.find(body)?.groupValues?.getOrNull(1) }
            .map { it.replace(",", "").toDoubleOrNull() }
            .firstOrNull { it != null && it > 0.0 }
    }

    private fun extractVendor(body: String): String {
        val patterns = listOf(
            Regex("""(?i)(?:to|merchant|vendor|from|at)\s*[:\-]?\s*([A-Za-z][A-Za-z0-9 .&'_-]{2,60})"""),
            Regex("""(?i)(?:paid\s+to|payment\s+to)\s+([A-Za-z][A-Za-z0-9 .&'_-]{2,60})"""),
            Regex("""(?:ለ|ከ)\s*[:\-]?\s*([^\n.]{2,60})""")
        )
        return patterns.asSequence()
            .mapNotNull { it.find(body)?.groupValues?.getOrNull(1)?.trim() }
            .firstOrNull()
            ?.trimEnd('.', ',', ';')
            ?.take(60)
            ?: "Unknown vendor"
    }

    private fun extractPhone(body: String): String? =
        Regex("""(?<!\d)(?:\+251|0)9\d{8}(?!\d)""").find(body)?.value

    private fun extractReference(body: String): String? =
        Regex(
            """(?i)(?:reference|ref|transaction\s*(?:id|no)|txn|ቁጥር)\s*[:#\-]?\s*([A-Za-z0-9-]{4,50})"""
        ).find(body)?.groupValues?.getOrNull(1)

    private fun calculateConfidence(body: String, vendor: String): Int {
        var score = 55
        if (body.contains("ETB", true) || body.contains("Birr", true) || body.contains("ብር")) score += 15
        if (!vendor.equals("Unknown vendor", true)) score += 15
        if (extractReference(body) != null) score += 10
        if (extractPhone(body) != null) score += 5
        return score.coerceAtMost(100)
    }
}

class CbeSmsParser : GenericPaymentParser(
    PaymentSource.CBE,
    listOf("cbe", "commercial bank of ethiopia"),
    listOf("commercial bank", "cbe birr")
)

class TelebirrSmsParser : GenericPaymentParser(
    PaymentSource.TELEBIRR,
    listOf("telebirr"),
    listOf("telebirr payment", "telebirr transaction")
)

class MpesaSmsParser : GenericPaymentParser(
    PaymentSource.MPESA,
    listOf("m-pesa", "m pesa", "mpesa"),
    listOf("safaricom mpesa", "safaricom")
)
