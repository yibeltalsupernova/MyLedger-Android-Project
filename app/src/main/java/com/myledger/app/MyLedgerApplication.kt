package com.myledger.app

import android.app.Application
import com.myledger.app.data.local.AppDatabase
import com.myledger.app.data.repository.TransactionRepository

class MyLedgerApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }
}
