package com.example.cashbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.cashbook.ui.theme.CashBookTheme
import com.example.cashbook.ui.theme.screens.*

enum class AppScreen {
    DASHBOARD,
    CASH_BOOK,
    REPORTS,
    ACCOUNTS,
    CREDIT,
    DEBIT
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CashBookTheme {
                AppNavigation()
            }
        }
    }
}