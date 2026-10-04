package com.myledger.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myledger.app.data.local.TransactionEntity
import com.myledger.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: TransactionRepository) : ViewModel() {
    val transactions = repository.transactions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val totalIncome = repository.totalIncome.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0
    )
    val totalExpense = repository.totalExpense.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0
    )
    val categoryTotals = repository.categoryTotals.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val query = MutableStateFlow("")
    val filteredTransactions = query
        .flatMapLatest { q ->
            if (q.isBlank()) repository.transactions else repository.search(q.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearch(value: String) { query.value = value }

    fun delete(transaction: TransactionEntity) {
        viewModelScope.launch { repository.delete(transaction) }
    }

    fun add(transaction: TransactionEntity) {
        viewModelScope.launch { repository.insert(transaction) }
    }

    class Factory(private val repository: TransactionRepository) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainViewModel(repository) as T
    }
}
