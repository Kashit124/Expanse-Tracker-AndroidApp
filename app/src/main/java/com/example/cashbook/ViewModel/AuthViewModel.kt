package com.example.cashbook.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashbook.Repository.AuthRepository
import com.example.cashbook.Repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    // Read once at startup — reflects whatever was saved from a previous session
    private val _isLoggedIn = MutableStateFlow(SessionManager.isLoggedIn(application))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _resetSuccess = MutableStateFlow(false)
    val resetSuccess: StateFlow<Boolean> = _resetSuccess.asStateFlow()

    val currentUsername: String?
        get() = SessionManager.getUsername(getApplication())

    fun login(username: String, password: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.login(username, password)
                .onSuccess { response ->
                    SessionManager.saveSession(getApplication(), response.userId, response.username)
                    _isLoggedIn.value = true
                    onComplete(true)
                }
                .onFailure {
                    _errorMessage.value = mapError(it)
                    onComplete(false)
                }
            _isLoading.value = false
        }
    }

    fun register(username: String, password: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.register(username, password)
                .onSuccess { response ->
                    SessionManager.saveSession(getApplication(), response.userId, response.username)
                    _isLoggedIn.value = true
                    onComplete(true)
                }
                .onFailure {
                    _errorMessage.value = mapError(it)
                    onComplete(false)
                }
            _isLoading.value = false
        }
    }

    fun resetPassword(username: String, newPassword: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _resetSuccess.value = false
            repository.resetPassword(username, newPassword)
                .onSuccess {
                    _resetSuccess.value = true
                    onComplete(true)
                }
                .onFailure {
                    _errorMessage.value = mapError(it)
                    onComplete(false)
                }
            _isLoading.value = false
        }
    }

    fun logout() {
        SessionManager.clearSession(getApplication())
        _isLoggedIn.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearResetSuccess() {
        _resetSuccess.value = false
    }

    private fun mapError(e: Throwable): String {
        val message = e.message ?: "Something went wrong"
        return when {
            message.contains("401") -> "Incorrect username or password"
            message.contains("409") -> "That username is already taken"
            message.contains("404") -> "No account found with that username"
            else -> message
        }
    }
}