package com.example.cashbook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cashbook.ui.theme.screens.*
import com.example.cashbook.ViewModel.AuthViewModel
import java.time.LocalDate

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "dashboard" else "login"
    ) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signup")
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgotPassword")
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("forgotPassword") {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onResetSuccess = {
                    // Send them back to Login to sign in with the new password
                    navController.popBackStack()
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                onCreditClick = { date -> navController.navigate("creditEntry/$date") },
                onDebitClick = { date -> navController.navigate("debitEntry/$date") },
                onHomeClick = { /* already here */ },
                onCashBookClick = { navController.navigate("cashbook") },
                onReportsClick = { navController.navigate("reports") },
                onAccountsClick = { navController.navigate("accounts") }
            )
        }

        composable("cashbook") {
            CashBookScreen(
                onCreditClick = { navController.navigate("creditEntry/${LocalDate.now()}") },
                onDebitClick = { navController.navigate("debitEntry/${LocalDate.now()}") },
                onHomeClick = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } } },
                onCashBookClick = { /* already here */ },
                onReportsClick = { navController.navigate("reports") },
                onAccountsClick = { navController.navigate("accounts") }
            )
        }

        composable("reports") {
            MonthlyReportScreen(
                onHomeClick = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } } },
                onCashBookClick = { navController.navigate("cashbook") },
                onReportsClick = { /* already here */ },
                onAccountsClick = { navController.navigate("accounts") }
            )
        }

        composable("accounts") {
            AccountsScreen(
                onHomeClick = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } } },
                onCashBookClick = { navController.navigate("cashbook") },
                onReportsClick = { navController.navigate("reports") },
                onAccountsClick = { /* already here */ },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true } // wipe entire back stack
                    }
                }
            )
        }

        composable(
            route = "creditEntry/{date}",
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            CreditEntryScreen(
                targetDate = date,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = "debitEntry/{date}",
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            DebitEntryScreen(
                targetDate = date,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}