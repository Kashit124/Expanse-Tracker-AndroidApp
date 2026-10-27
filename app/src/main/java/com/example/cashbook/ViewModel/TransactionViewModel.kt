package com.example.cashbook.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashbook.Repository.TransactionRepository
import com.example.cashbook.model.TransactionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionViewModel : ViewModel() {

    private val repository = TransactionRepository()

    private val _transactions = MutableStateFlow<List<TransactionResponse>>(emptyList())
    val transactions: StateFlow<List<TransactionResponse>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadTransactions(accountId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTransactionsByAccount(accountId)
                .onSuccess { _transactions.value = it; _errorMessage.value = null }
                .onFailure { _errorMessage.value = "Could not load transactions" }
            _isLoading.value = false
        }
    }

    fun addCreditEntry(
        accountId: Int, date: String, particular: String, amount: Double,
        jf: String?, notes: String?, onResult: (Boolean) -> Unit
    ) = addEntry(accountId, date, true, particular, amount, jf, onResult)

    fun addDebitEntry(
        accountId: Int, date: String, particular: String, amount: Double,
        jf: String?, notes: String?, onResult: (Boolean) -> Unit
    ) = addEntry(accountId, date, false, particular, amount, jf, onResult)

    private fun addEntry(
        accountId: Int, date: String, isCredit: Boolean, particular: String,
        amount: Double, jf: String?, onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.createTransaction(accountId, date, isCredit, particular, amount, jf)
            _isLoading.value = false
            result.onSuccess { _transactions.value = _transactions.value + it }
            onResult(result.isSuccess)
        }
    }

    fun deleteTransaction(id: Int, accountId: Int) {
        viewModelScope.launch {
            repository.deleteTransaction(id).onSuccess { loadTransactions(accountId) }
        }
    }
}
