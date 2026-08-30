package com.example.cashbook.model

data class TransactionResponse(
    val transactionId: Int,
    val accountId: Int,
    val transactionDate: String,   // ISO format: "2026-08-26"
    val transactionType: String,   // "CREDIT" or "DEBIT"
    val particular: String,
    val jf: String?,
    val amount: Double,
    val notes: String?,
    val createdAt: String
)

data class TransactionRequest(
    val accountId: Int,
    val transactionDate: String,
    val transactionType: String,
    val particular: String,
    val jf: String? = null,
    val amount: Double,
    val notes: String? = null
)