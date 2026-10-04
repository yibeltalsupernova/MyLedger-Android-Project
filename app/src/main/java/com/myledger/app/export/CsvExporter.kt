package com.myledger.app.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.myledger.app.data.local.TransactionEntity
import java.io.File

object CsvExporter {
    fun share(context: Context, transactions: List<TransactionEntity>) {
        val file=File(context.cacheDir,"myledger-transactions.csv")
        file.printWriter().use { out ->
            out.println("Date,Type,Amount,Vendor,Category,Source,Reference,Note")
            transactions.forEach {
                fun q(s:String?)="\""+(s ?: "").replace("\"","\"\"")+"\""
                out.println(
                    "${it.timestamp},${it.type},${it.amount},${q(it.vendor)},${it.category},${it.source},${q(it.reference)},${q(it.note)}"
                )
            }
        }
        val uri=FileProvider.getUriForFile(context,context.packageName+".fileprovider",file)
        context.startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type="text/csv"; putExtra(Intent.EXTRA_STREAM,uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },"Export MyLedger"
        ))
    }
}
