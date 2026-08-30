package com.example.cashbook.Repository

import com.example.cashbook.Network.ApiService
import com.example.cashbook.Network.RetrofitClient
import com.example.cashbook.model.TransactionRequest
import com.example.cashbook.model.TransactionResponse

class TransactionRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun getAllTransactions(): Result<List<TransactionResponse>> = safeApiCall {
        apiService.getAllTransactions()
    }

    suspend fun getTransactionById(id: Int): Result<TransactionResponse> = safeApiCall {
        apiService.getTransactionById(id)
    }

    suspend fun createTransaction(request: TransactionRequest): Result<TransactionResponse> = safeApiCall {
        apiService.createTransaction(request)
    }

    suspend fun updateTransaction(id: Int, request: TransactionRequest): Result<TransactionResponse> = safeApiCall {
        apiService.updateTransaction(id, request)
    }

    suspend fun deleteTransaction(id: Int): Result<Unit> = safeApiCall {
        apiService.deleteTransaction(id)
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