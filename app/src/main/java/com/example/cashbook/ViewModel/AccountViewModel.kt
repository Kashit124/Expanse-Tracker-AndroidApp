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

    // Loads only the one account belonging to the currently logged-in user
    fun loadAccountById(accountId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAccountById(accountId)
                .onSuccess { _accounts.value = listOf(it) }
                .onFailure { _errorMessage.value = it.message ?: "Failed to load account" }
            _isLoading.value = false
        }
    }

    fun updateAccount(id: Int, userId: Int, name: String, openingBalance: Double, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repository.updateAccount(id, AccountRequest(userId, name, openingBalance))
                .onSuccess {
                    loadAccountById(id)
                    onComplete(true)
                }
                .onFailure {
                    _errorMessage.value = it.message ?: "Failed to update account"
                    onComplete(false)
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}