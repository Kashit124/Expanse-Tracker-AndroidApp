package com.example.cashbook.Repository

import com.example.cashbook.Network.ApiService
import com.example.cashbook.Network.RetrofitClient
import com.example.cashbook.model.AuthResponse
import com.example.cashbook.model.LoginRequest
import com.example.cashbook.model.RegisterRequest
import com.example.cashbook.model.ResetPasswordRequest

class AuthRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun register(username: String, password: String): Result<AuthResponse> = safeApiCall {
        apiService.register(RegisterRequest(username, password))
    }

    suspend fun login(username: String, password: String): Result<AuthResponse> = safeApiCall {
        apiService.login(LoginRequest(username, password))
    }

    suspend fun resetPassword(username: String, newPassword: String): Result<AuthResponse> = safeApiCall {
        apiService.resetPassword(ResetPasswordRequest(username, newPassword))
    }

    private suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
        return try {
            Result.success(call())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}