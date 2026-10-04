package com.myledger.app

import com.myledger.app.data.local.*
import com.myledger.app.data.parser.SmsParserManager
import org.junit.Assert.*
import org.junit.Test

class SmsParserTest {
    @Test
    fun parsesTelebirrExpense() {
        val parsed = SmsParserManager.parse(
            "Telebirr",
            "You paid 450.00 ETB to ABC Restaurant. Transaction reference REF12345."
        )
        assertNotNull(parsed)
        assertEquals(450.0, parsed!!.amount, 0.01)
        assertEquals(PaymentSource.TELEBIRR, parsed.source)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals(ExpenseCategory.FOOD, parsed.category)
    }

    @Test
    fun parsesCbeIncome() {
        val parsed = SmsParserManager.parse(
            "CBE",
            "Your account has been credited with ETB 5000.00. Reference ABC12345."
        )
        assertNotNull(parsed)
        assertEquals(5000.0, parsed!!.amount, 0.01)
        assertEquals(PaymentSource.CBE, parsed.source)
        assertEquals(TransactionType.INCOME, parsed.type)
    }

    @Test
    fun categorizesTransport() {
        val parsed = SmsParserManager.parse(
            "M-Pesa",
            "You paid 120 ETB to Uber."
        )
        assertNotNull(parsed)
        assertEquals(ExpenseCategory.TRANSPORT, parsed!!.category)
    }
}
