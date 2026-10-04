package com.myledger.app.data.repository

import com.myledger.app.data.local.*

class Repository(
    private val tx: TransactionDao,
    private val budget: BudgetDao
) {
    val transactions = tx.observeAll()
    val income = tx.totalIncome()
    val expense = tx.totalExpense()
    val categoryTotals = tx.byCategory()
    val monthlyTotals = tx.monthlyTotals()
    val budgets = budget.observeAll()

    fun search(q: String) = tx.search(q)
    fun monthBudgets(month: String) = budget.observeMonth(month)

    suspend fun add(t: TransactionEntity) = tx.insert(t)
    suspend fun update(t: TransactionEntity) = tx.update(t)
    suspend fun delete(t: TransactionEntity) = tx.delete(t)
    suspend fun findSms(hash: String) = tx.bySmsHash(hash)

    suspend fun saveBudget(b: BudgetEntity) = budget.save(b)
    suspend fun updateBudget(b: BudgetEntity) = budget.update(b)
    suspend fun deleteBudget(b: BudgetEntity) = budget.delete(b)
}
