package com.example.cashbook.Repository

import com.example.cashbook.Network.ApiService
import com.example.cashbook.Network.RetrofitClient
import com.example.cashbook.model.AccountRequest
import com.example.cashbook.model.AccountResponse

class AccountRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun getAllAccounts(): Result<List<AccountResponse>> = safeApiCall {
        apiService.getAllAccounts()
    }

    suspend fun getAccountById(id: Int): Result<AccountResponse> = safeApiCall {
        apiService.getAccountById(id)
    }

    suspend fun createAccount(request: AccountRequest): Result<AccountResponse> = safeApiCall {
        apiService.createAccount(request)
    }

    suspend fun updateAccount(id: Int, request: AccountRequest): Result<AccountResponse> = safeApiCall {
        apiService.updateAccount(id, request)
    }

    suspend fun deleteAccount(id: Int): Result<Unit> = safeApiCall {
        apiService.deleteAccount(id)
        Unit
    }

    private suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
        return try {
            Result.success(call())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}