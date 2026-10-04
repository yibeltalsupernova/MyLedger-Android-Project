package com.myledger.app.receiver

import android.content.*
import android.provider.Telephony
import com.myledger.app.MyLedgerApplication
import com.myledger.app.data.local.TransactionEntity
import com.myledger.app.data.parser.ParserManager
import kotlinx.coroutines.*
import java.security.MessageDigest

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as MyLedgerApplication
                val dao = app.db.transactionDao()
                val parser = ParserManager()
                Telephony.Sms.Intents.getMessagesFromIntent(intent).forEach { sms ->
                    val sender=sms.displayOriginatingAddress ?: return@forEach
                    val body=sms.displayMessageBody ?: return@forEach
                    val parsed=parser.parse(sender,body) ?: return@forEach
                    val hash=sha256("$sender|$body|${sms.timestampMillis}")
                    if (dao.bySmsHash(hash)==null) {
                        dao.insert(TransactionEntity(
                            amount=parsed.amount,type=parsed.type,vendor=parsed.vendor,
                            category=parsed.category,source=parsed.source,
                            accountOrPhone=parsed.accountOrPhone,reference=parsed.reference,
                            originalSms=body,timestamp=sms.timestampMillis,smsHash=hash,
                            isAutoParsed=true
                        ))
                    }
                }
            } finally { pending.finish() }
        }
    }
    private fun sha256(s:String)=MessageDigest.getInstance("SHA-256")
        .digest(s.toByteArray()).joinToString(""){"%02x".format(it)}
}
