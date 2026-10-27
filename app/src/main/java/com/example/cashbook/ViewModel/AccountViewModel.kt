package com.example.cashbook.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashbook.Repository.AccountRepository
import com.example.cashbook.model.AccountResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {

    private val repository = AccountRepository()

    private val _accounts = MutableStateFlow<List<AccountResponse>>(emptyList())
    val accounts: StateFlow<List<AccountResponse>> = _accounts.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Backend has no GET /accounts/{id}; it returns the user's accounts list instead.
    // Name kept so the existing screens don't need changes.
    @Suppress("UNUSED_PARAMETER")
    fun loadAccountById(accountId: Int) {
        viewModelScope.launch {
            repository.getAccounts()
                .onSuccess { _accounts.value = it; _errorMessage.value = null }
                .onFailure { _errorMessage.value = "Could not load account" }
        }
    }
}
