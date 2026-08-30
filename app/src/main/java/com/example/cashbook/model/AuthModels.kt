package com.example.cashbook.model

data class RegisterRequest(
    val username: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class ResetPasswordRequest(
    val username: String,
    val newPassword: String
)

data class AuthResponse(
    val userId: Int,
    val username: String,
    val createdAt: String
)