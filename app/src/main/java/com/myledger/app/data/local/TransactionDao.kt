package com.myledger.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'EXPENSE' ORDER BY timestamp DESC")
    fun observeExpenses(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'INCOME' ORDER BY timestamp DESC")
    fun observeIncome(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE vendor LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
           OR source LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun search(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE smsHash = :hash LIMIT 1")
    suspend fun findBySmsHash(hash: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'")
    fun observeTotalIncome(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'")
    fun observeTotalExpense(): Flow<Double>

    @Query("""
        SELECT category, COALESCE(SUM(amount),0) AS total
        FROM transactions
        WHERE type = 'EXPENSE'
        GROUP BY category
        ORDER BY total DESC
    """)
    fun observeExpenseByCategory(): Flow<List<CategoryTotal>>
}

data class CategoryTotal(
    val category: ExpenseCategory,
    val total: Double
)
