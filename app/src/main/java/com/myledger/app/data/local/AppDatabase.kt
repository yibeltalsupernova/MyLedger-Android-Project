package com.myledger.app.data.local

import android.content.Context
import androidx.room.*

@Database(
    entities = [TransactionEntity::class, BudgetEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "myledger.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}

class Converters {
    @TypeConverter fun type(v: TransactionType) = v.name
    @TypeConverter fun type(v: String) = TransactionType.valueOf(v)
    @TypeConverter fun category(v: ExpenseCategory) = v.name
    @TypeConverter fun category(v: String) = ExpenseCategory.valueOf(v)
    @TypeConverter fun source(v: PaymentSource) = v.name
    @TypeConverter fun source(v: String) = PaymentSource.valueOf(v)
    @TypeConverter fun recurrence(v: Recurrence) = v.name
    @TypeConverter fun recurrence(v: String) = Recurrence.valueOf(v)
}
