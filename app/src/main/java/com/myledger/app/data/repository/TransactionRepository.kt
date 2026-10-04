package com.myledger.app.data.repository

import com.myledger.app.data.local.TransactionDao
import com.myledger.app.data.local.TransactionEntity

class TransactionRepository(private val dao: TransactionDao) {
    val transactions = dao.observeAll()
    val expenses = dao.observeExpenses()
    val income = dao.observeIncome()
    val totalIncome = dao.observeTotalIncome()
    val totalExpense = dao.observeTotalExpense()
    val categoryTotals = dao.observeExpenseByCategory()

    fun search(query: String) = dao.search(query)

    suspend fun insert(transaction: TransactionEntity) = dao.insert(transaction)
    suspend fun update(transaction: TransactionEntity) = dao.update(transaction)
    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)
    suspend fun findByHash(hash: String) = dao.findBySmsHash(hash)
}
