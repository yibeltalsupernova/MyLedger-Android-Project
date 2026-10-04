package com.myledger.app

import android.app.Application
import com.myledger.app.data.local.AppDatabase
import com.myledger.app.data.repository.Repository

class MyLedgerApplication : Application() {
    val db by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { Repository(db.transactionDao(), db.budgetDao()) }
}
