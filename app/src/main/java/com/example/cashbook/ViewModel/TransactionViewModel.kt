package com.example.cashbook.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashbook.Repository.TransactionRepository
import com.example.cashbook.model.TransactionRequest
import com.example.cashbook.model.TransactionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repository: TransactionRepository = TransactionRepository()
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<TransactionResponse>>(emptyList())
    val transactions: StateFlow<List<TransactionResponse>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Remembers which account is currently loaded, so add/update/delete can refresh correctly
    private var currentAccountId: Int? = null

    // Loads only this account's transactions — never everyone's
    fun loadTransactions(accountId: Int) {
        currentAccountId = accountId
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTransactionsByAccount(accountId)
                .onSuccess { _transactions.value = it }
                .onFailure { _errorMessage.value = it.message ?: "Failed to load transactions" }
            _isLoading.value = false
        }
    }

    private fun refresh() {
        currentAccountId?.let { loadTransactions(it) }
    }

    // ---- Convenience wrappers matching CreditEntryScreen / DebitEntryScreen ----

    fun addCreditEntry(
        accountId: Int,
        date: String,
        particular: String,
        amount: Double,
        jf: String? = null,
        notes: String? = null,
        onComplete: (Boolean) -> Unit = {}
    ) {
        createTransaction(
            TransactionRequest(
                accountId = accountId,
                transactionDate = date,
                transactionType = "CREDIT",
                particular = particular,
                jf = jf,
                amount = amount,
                notes = notes
            ),
            onComplete
        )
    }

    fun addDebitEntry(
        accountId: Int,
        date: String,
        particular: String,
        amount: Double,
        jf: String? = null,
        notes: String? = null,
        onComplete: (Boolean) -> Unit = {}
    ) {
        createTransaction(
            TransactionRequest(
                accountId = accountId,
                transactionDate = date,
                transactionType = "DEBIT",
                particular = particular,
                jf = jf,
                amount = amount,
                notes = notes
            ),
            onComplete
        )
    }

    fun createTransaction(request: TransactionRequest, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.createTransaction(request)
                .onSuccess {
                    refresh()
                    onComplete(true)
                }
                .onFailure {
                    _errorMessage.value = it.message ?: "Failed to save entry"
                    onComplete(false)
                }
            _isLoading.value = false
        }
    }

    fun updateTransaction(id: Int, request: TransactionRequest, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repository.updateTransaction(id, request)
                .onSuccess {
                    refresh()
                    onComplete(true)
                }
                .onFailure {
                    _errorMessage.value = it.message ?: "Failed to update entry"
                    onComplete(false)
                }
        }
    }

    fun deleteTransaction(id: Int, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
                .onSuccess {
                    refresh()
                    onComplete(true)
                }
                .onFailure {
                    _errorMessage.value = it.message ?: "Failed to delete entry"
                    onComplete(false)
                }
        }
    }

    // ---- Derived totals, useful for DashboardScreen / AccountsScreen ----

    fun totalCredit(): Double =
        _transactions.value.filter { it.transactionType == "CREDIT" }.sumOf { it.amount }

    fun totalDebit(): Double =
        _transactions.value.filter { it.transactionType == "DEBIT" }.sumOf { it.amount }

    fun balance(): Double = totalCredit() - totalDebit()

    fun transactionsForDate(date: String): List<TransactionResponse> =
        _transactions.value.filter { it.transactionDate == date }

    fun clearError() {
        _errorMessage.value = null
    }
}