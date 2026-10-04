package com.myledger.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE vendor LIKE '%' || :query || '%'
        OR category LIKE '%' || :query || '%'
        OR source LIKE '%' || :query || '%'
        OR note LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun search(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id=:id LIMIT 1")
    suspend fun get(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE smsHash=:hash LIMIT 1")
    suspend fun bySmsHash(hash: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: TransactionEntity): Long

    @Update suspend fun update(item: TransactionEntity)
    @Delete suspend fun delete(item: TransactionEntity)
    @Query("DELETE FROM transactions") suspend fun deleteAll()

    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type='INCOME'")
    fun totalIncome(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type='EXPENSE'")
    fun totalExpense(): Flow<Double>

    @Query("""
        SELECT category, COALESCE(SUM(amount),0) total
        FROM transactions WHERE type='EXPENSE'
        GROUP BY category ORDER BY total DESC
    """)
    fun byCategory(): Flow<List<CategoryTotal>>

    @Query("""
        SELECT strftime('%Y-%m', timestamp/1000, 'unixepoch') month,
               type, COALESCE(SUM(amount),0) total
        FROM transactions
        GROUP BY month, type ORDER BY month DESC
    """)
    fun monthlyTotals(): Flow<List<MonthlyTotal>>
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY category")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE monthKey=:monthKey")
    fun observeMonth(monthKey: String): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(item: BudgetEntity): Long

    @Update suspend fun update(item: BudgetEntity)
    @Delete suspend fun delete(item: BudgetEntity)
}

data class CategoryTotal(val category: ExpenseCategory, val total: Double)
data class MonthlyTotal(val month: String, val type: TransactionType, val total: Double)
