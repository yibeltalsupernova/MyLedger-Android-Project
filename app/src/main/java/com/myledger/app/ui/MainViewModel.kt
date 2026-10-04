package com.myledger.app.ui

import androidx.lifecycle.*
import com.myledger.app.data.local.*
import com.myledger.app.data.repository.Repository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repo: Repository): ViewModel() {
    val transactions=repo.transactions.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val income=repo.income.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),0.0)
    val expense=repo.expense.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),0.0)
    val categoryTotals=repo.categoryTotals.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val monthlyTotals=repo.monthlyTotals.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val budgets=repo.budgets.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    private val q=MutableStateFlow("")
    val filtered=q.flatMapLatest{if(it.isBlank()) repo.transactions else repo.search(it)}.stateIn(
        viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList()
    )
    fun search(v:String){q.value=v}
    fun add(t:TransactionEntity)=viewModelScope.launch{repo.add(t)}
    fun delete(t:TransactionEntity)=viewModelScope.launch{repo.delete(t)}
    fun saveBudget(b:BudgetEntity)=viewModelScope.launch{repo.saveBudget(b)}
    fun deleteBudget(b:BudgetEntity)=viewModelScope.launch{repo.deleteBudget(b)}

    class Factory(private val repo:Repository):ViewModelProvider.Factory{
        @Suppress("UNCHECKED_CAST")
        override fun <T:ViewModel> create(c:Class<T>):T=MainViewModel(repo) as T
    }
}
