package com.myledger.app.data.parser

import com.myledger.app.data.local.ExpenseCategory

object CategoryEngine {
    private val rules = linkedMapOf(
        ExpenseCategory.FOOD to listOf("restaurant","cafe","coffee","food","pizza","burger","hotel","bakery","ምግብ","እንጀራ"),
        ExpenseCategory.TRANSPORT to listOf("uber","ride","taxi","transport","sheger","fuel","petrol","gas","ታክሲ","ሚኒባስ","ነዳጅ"),
        ExpenseCategory.SHOPPING to listOf("shop","market","mall","store","supermarket","retail","ሱቅ","ገበያ"),
        ExpenseCategory.UTILITIES to listOf("electric","ethio telecom","telecom","water","internet","utility","ቴሌኮም","ውሃ","መብራት"),
        ExpenseCategory.RENT to listOf("rent","housing","house rent","ኪራይ"),
        ExpenseCategory.AIRTIME to listOf("airtime","recharge","topup","top up","mobile credit","bundle","data package","ካርድ"),
        ExpenseCategory.ENTERTAINMENT to listOf("cinema","movie","music","game","entertainment"),
        ExpenseCategory.HEALTH to listOf("pharmacy","hospital","clinic","medical","doctor","medicine","መድሃኒት"),
        ExpenseCategory.EDUCATION to listOf("school","university","college","tuition","education","training","ትምህርት")
    )
    fun categorize(vendor: String, message: String = ""): ExpenseCategory {
        val text = "$vendor $message".lowercase()
        return rules.entries.firstOrNull { (_, keys) -> keys.any { text.contains(it.lowercase()) } }?.key
            ?: ExpenseCategory.OTHER
    }
}
