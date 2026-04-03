package com.abhik.financecompanion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhik.financecompanion.data.Transaction
import com.abhik.financecompanion.data.TransactionRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class FinanceViewModel(private val repository: TransactionRepo) : ViewModel() {

    val transactions: StateFlow<List<Transaction>> = repository.alltrans
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalIncome: StateFlow<Double> = repository.getTotalAmtByType("INCOME")
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalExpense: StateFlow<Double> = repository.getTotalAmtByType("EXPENSE")
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val noSpendStreak: StateFlow<Int> = transactions.map { list ->
        calculateStreak(list)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun addTransaction(amount: Double, type: String, category: String, dateMillis: Long, note: String) {
        viewModelScope.launch {
            val newTransaction = Transaction(
                amount = amount,
                type = type,
                category = category,
                timestamp = dateMillis,
                note = note
            )
            repository.insertTrans(newTransaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTrans(transaction)
        }
    }

    private fun calculateStreak(transactions: List<Transaction>): Int {

        val expenses = transactions.filter { it.type == "EXPENSE" }.sortedByDescending { it.timestamp }
        if (expenses.isEmpty()) return 0

        val lastExpenseDate = expenses.first().timestamp
        val today = Calendar.getInstance().timeInMillis

        val diffMillis = today - lastExpenseDate
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}

class FinanceViewModelFactory(private val repository: TransactionRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}