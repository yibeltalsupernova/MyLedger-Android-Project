package com.myledger.app.data.parser

import com.myledger.app.data.local.ExpenseCategory

/**
 * Lightweight local-first categorization engine.
 * Vendor rules are intentionally editable in one place.
 */
object CategoryEngine {
    private val rules: LinkedHashMap<ExpenseCategory, List<String>> = linkedMapOf(
        ExpenseCategory.FOOD to listOf(
            "restaurant", "cafe", "coffee", "food", "pizza", "burger", "habesha",
            "hotel", "bakery", "ምግብ", "ምግብ ቤት", "እንጀራ"
        ),
        ExpenseCategory.TRANSPORT to listOf(
            "uber", "ride", "taxi", "transport", "feres", "sheger", "fuel",
            "gas", "petrol", "ደርሶ", "ታክሲ", "ሚኒባስ", "ነዳጅ"
        ),
        ExpenseCategory.SHOPPING to listOf(
            "shop", "market", "mall", "store", "supermarket", "retail",
            "amazon", "ሱቅ", "ገበያ"
        ),
        ExpenseCategory.UTILITIES to listOf(
            "electric", "ethio telecom", "telecom", "water", "utility",
            "internet", "ethiotelecom", "ውሃ", "መብራት", "ቴሌኮም"
        ),
        ExpenseCategory.RENT to listOf("rent", "house rent", "housing", "ኪራይ"),
        ExpenseCategory.AIRTIME to listOf(
            "airtime", "recharge", "topup", "top up", "mobile credit",
            "bundle", "data package", "ካርድ"
        ),
        ExpenseCategory.ENTERTAINMENT to listOf(
            "cinema", "movie", "music", "game", "entertainment"
        ),
        ExpenseCategory.HEALTH to listOf(
            "pharmacy", "hospital", "clinic", "medical", "doctor",
            "medicine", "መድሃኒት", "ሆስፒታል"
        ),
        ExpenseCategory.EDUCATION to listOf(
            "school", "university", "college", "tuition", "education", "training",
            "ትምህርት"
        )
    )

    fun categorize(vendor: String, message: String = ""): ExpenseCategory {
        val text = "$vendor $message".lowercase()
        return rules.entries.firstOrNull { (_, keywords) ->
            keywords.any { keyword -> text.contains(keyword.lowercase()) }
        }?.key ?: ExpenseCategory.OTHER
    }

    fun categories(): List<ExpenseCategory> = ExpenseCategory.entries
}
