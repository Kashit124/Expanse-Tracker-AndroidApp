package com.example.cashbook.Network

import com.example.cashbook.model.AccountRequest
import com.example.cashbook.model.AccountResponse
import com.example.cashbook.model.AuthResponse
import com.example.cashbook.model.LoginRequest
import com.example.cashbook.model.RegisterRequest
import com.example.cashbook.model.ResetPasswordRequest
import com.example.cashbook.model.TransactionRequest
import com.example.cashbook.model.TransactionResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ---- Auth ----

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): AuthResponse

    // ---- Accounts ----

    @GET("api/accounts")
    suspend fun getAllAccounts(): List<AccountResponse>

    @GET("api/accounts/{id}")
    suspend fun getAccountById(@Path("id") id: Int): AccountResponse

    @POST("api/accounts")
    suspend fun createAccount(@Body request: AccountRequest): AccountResponse

    @PUT("api/accounts/{id}")
    suspend fun updateAccount(@Path("id") id: Int, @Body request: AccountRequest): AccountResponse

    @DELETE("api/accounts/{id}")
    suspend fun deleteAccount(@Path("id") id: Int): Response<Unit>

    // ---- Transactions ----

    @GET("api/transactions")
    suspend fun getAllTransactions(): List<TransactionResponse>

    @GET("api/transactions/{id}")
    suspend fun getTransactionById(@Path("id") id: Int): TransactionResponse

    @POST("api/transactions")
    suspend fun createTransaction(@Body request: TransactionRequest): TransactionResponse

    @PUT("api/transactions/{id}")
    suspend fun updateTransaction(@Path("id") id: Int, @Body request: TransactionRequest): TransactionResponse

    @DELETE("api/transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: Int): Response<Unit>
}