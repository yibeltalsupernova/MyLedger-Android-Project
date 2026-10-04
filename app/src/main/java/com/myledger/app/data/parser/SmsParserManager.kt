package com.myledger.app.data.parser

object SmsParserManager {
    private val parsers: List<SmsParser> = listOf(
        CbeSmsParser(),
        TelebirrSmsParser(),
        MpesaSmsParser()
    )

    fun parse(sender: String, body: String): ParsedSms? {
        val parser = parsers.firstOrNull { it.canParse(sender, body) } ?: return null
        return parser.parse(sender, body)
    }
}
