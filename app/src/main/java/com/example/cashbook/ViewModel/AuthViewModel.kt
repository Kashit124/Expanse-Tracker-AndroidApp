package com.example.cashbook.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashbook.Repository.AccountRepository
import com.example.cashbook.Repository.AuthRepository
import com.example.cashbook.Repository.SessionManager
import com.example.cashbook.model.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val authRepository = AuthRepository()
    private val accountRepository = AccountRepository()

    private val _isLoggedIn = MutableStateFlow(SessionManager.isLoggedIn(app))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val currentUsername: String?
        get() = SessionManager.getUsername(getApplication())

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        authenticate(email, onResult, "Login failed") { authRepository.login(email, password) }
    }

    fun register(username: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        authenticate(email, onResult, "Registration failed") {
            authRepository.register(username, email, password)
        }
    }

    fun logout() {
        SessionManager.clearSession(getApplication())
        _isLoggedIn.value = false
    }

    private fun authenticate(
        email: String,
        onResult: (Boolean) -> Unit,
        failureText: String,
        call: suspend () -> Result<AuthResponse>
    ) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            _isLoading.value = true
            _errorMessage.value = null

            val auth = call().getOrElse {
                fail(it.toMessage(failureText), onResult)
                return@launch
            }

            // Save session first: the interceptor needs the email for the next calls
            SessionManager.saveSession(
                ctx,
                userId = auth.id?.toInt() ?: 0,
                username = auth.username ?: email.substringBefore("@"),
                email = auth.email ?: email
            )

            val account = accountRepository.ensureAccount().getOrElse {
                SessionManager.clearSession(ctx)
                fail(it.toMessage("Could not load your account"), onResult)
                return@launch
            }
            SessionManager.saveAccountId(ctx, account.accountId)

            _isLoggedIn.value = true
            _isLoading.value = false
            onResult(true)
        }
    }

    private fun fail(message: String, onResult: (Boolean) -> Unit) {
        _errorMessage.value = message
        _isLoading.value = false
        onResult(false)
    }

    private fun Throwable.toMessage(default: String): String = when (this) {
        is HttpException -> when (code()) {
            401, 403 -> "Invalid email or password"
            409 -> "This email is already registered"
            else -> "$default (server error ${code()})"
        }
        is IOException -> "Can't reach the server. Check your connection."
        else -> default
    }
}
