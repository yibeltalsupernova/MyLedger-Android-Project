package com.myledger.app
import com.myledger.app.data.local.*
import com.myledger.app.data.parser.*
import org.junit.Assert.*
import org.junit.Test

class ParserTest {
    private val p=ParserManager()
    @Test fun telebirrFood(){
        val x=p.parse("Telebirr","You paid 450.00 ETB to ABC Restaurant. Reference REF12345.")
        assertNotNull(x);assertEquals(450.0,x!!.amount,.01);assertEquals(PaymentSource.TELEBIRR,x.source)
        assertEquals(ExpenseCategory.FOOD,x.category)
    }
    @Test fun cbeIncome(){
        val x=p.parse("CBE","Your account was credited with ETB 5000.00. Reference ABC12345.")
        assertEquals(TransactionType.INCOME,x!!.type);assertEquals(5000.0,x.amount,.01)
    }
    @Test fun mpesaTransport(){
        val x=p.parse("M-Pesa","You paid 120 ETB to Uber.")
        assertEquals(ExpenseCategory.TRANSPORT,x!!.category)
    }
}
