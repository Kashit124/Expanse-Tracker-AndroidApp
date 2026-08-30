package com.example.cashbook.model

data class AccountResponse(
    val accountId: Int,
    val name: String,
    val openingBalance: Double,
    val createdAt: String
)

data class AccountRequest(
    val name: String,
    val openingBalance: Double
)