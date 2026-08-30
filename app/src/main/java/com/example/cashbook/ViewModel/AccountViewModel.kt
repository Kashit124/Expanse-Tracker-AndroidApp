package com.example.cashbook.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashbook.Repository.AccountRepository
import com.example.cashbook.model.AccountRequest
import com.example.cashbook.model.AccountResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountViewModel(
    private val repository: AccountRepository = AccountRepository()
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<AccountResponse>>(emptyList())
    val accounts: StateFlow<List<AccountResponse>> = _accounts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllAccounts()
                .onSuccess { _accounts.value = it }
                .onFailure { _errorMessage.value = it.message ?: "Failed to load accounts" }
            _isLoading.value = false
        }
    }

    fun createAccount(name: String, openingBalance: Double, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.createAccount(AccountRequest(name, openingBalance))
                .onSuccess {
                    loadAccounts()
                    onComplete(true)
                }
                .onFailure {
                    _errorMessage.value = it.message ?: "Failed to create account"
                    onComplete(false)
                }
            _isLoading.value = false
        }
    }

    fun updateAccount(id: Int, name: String, openingBalance: Double, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repository.updateAccount(id, AccountRequest(name, openingBalance))
                .onSuccess {
                    loadAccounts()
                    onComplete(true)
                }
                .onFailure {
                    _errorMessage.value = it.message ?: "Failed to update account"
                    onComplete(false)
                }
        }
    }

    fun deleteAccount(id: Int, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteAccount(id)
                .onSuccess {
                    loadAccounts()
                    onComplete(true)
                }
                .onFailure {
                    _errorMessage.value = it.message ?: "Failed to delete account"
                    onComplete(false)
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}