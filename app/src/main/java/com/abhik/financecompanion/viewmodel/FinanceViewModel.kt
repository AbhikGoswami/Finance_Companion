package com.abhik.financecompanion.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhik.financecompanion.data.Transaction
import com.abhik.financecompanion.data.TransactionRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar

class FinanceViewModel(
    private val repository: TransactionRepo,
    private val prefs: SharedPreferences
) : ViewModel() {

    val transactions: StateFlow<List<Transaction>> = repository.alltrans
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth

    val totalIncome: StateFlow<Double> = combine(transactions, _selectedMonth) { txs, month ->
        txs.filter {
            it.type == "INCOME" &&
                    YearMonth.from(Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault())) == month
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalExpense: StateFlow<Double> = combine(transactions, _selectedMonth) { txs, month ->
        txs.filter {
            it.type == "EXPENSE" &&
                    YearMonth.from(Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault())) == month
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val noSpendStreak: StateFlow<Int> = transactions.map { list ->
        calculateStreak(list)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // Read the saved budget from SharedPreferences when the app opens. Defaults to 2000.0 if empty.
    private val _monthlyBudget = MutableStateFlow(prefs.getFloat("monthly_budget", 2000f).toDouble())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget

    fun updateMonthlyBudget(newBudget: Double) {
        _monthlyBudget.value = newBudget
        // Save the new budget permanently to the device
        prefs.edit().putFloat("monthly_budget", newBudget.toFloat()).apply()
    }

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

    fun updateSelectedMonth(month: YearMonth) {
        _selectedMonth.value = month
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

class FinanceViewModelFactory(
    private val repository: TransactionRepo,
    private val prefs: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}