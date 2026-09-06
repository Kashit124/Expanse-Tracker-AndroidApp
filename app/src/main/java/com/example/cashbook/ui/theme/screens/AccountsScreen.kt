package com.example.cashbook.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashbook.Repository.SessionManager
import com.example.cashbook.ui.theme.CashBookTheme
import com.example.cashbook.ui.theme.components.BottomNavigationBar
import com.example.cashbook.ViewModel.AccountViewModel
import com.example.cashbook.ViewModel.AuthViewModel
import com.example.cashbook.ViewModel.TransactionViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    onHomeClick: () -> Unit,
    onCashBookClick: () -> Unit,
    onReportsClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onLogout: () -> Unit,
    accountViewModel: AccountViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {

    val context = LocalContext.current
    val accountId = remember { SessionManager.getAccountId(context) }

    val accounts by accountViewModel.accounts.collectAsState()
    val transactions by transactionViewModel.transactions.collectAsState()
    val isLoading by transactionViewModel.isLoading.collectAsState()

    LaunchedEffect(accountId) {
        accountId?.let {
            accountViewModel.loadAccountById(it)
            transactionViewModel.loadTransactions(it)
        }
    }

    val openingBalance = accounts.find { it.accountId == accountId }?.openingBalance ?: 0.0
    val totalCredit = transactions.filter { it.transactionType == "CREDIT" }.sumOf { it.amount }
    val totalDebit = transactions.filter { it.transactionType == "DEBIT" }.sumOf { it.amount }
    val cashBalance = openingBalance + totalCredit - totalDebit

    val username = authViewModel.currentUsername ?: "User"

    var showLogoutConfirm by remember { mutableStateOf(false) }

    fun rupees(value: Double) = "₹" + String.format(Locale.US, "%,.2f", value)

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text("Accounts")
                }
            )
        },

        bottomBar = {
            BottomNavigationBar(
                selectedItem = "Accounts",
                onHomeClick = onHomeClick,
                onCashBookClick = onCashBookClick,
                onReportsClick = onReportsClick,
                onAccountsClick = onAccountsClick
            )
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = "Logged in as",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = username,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Text(
                text = "Account Summary",
                style = MaterialTheme.typography.titleLarge
            )

            if (accountId == null) {
                Text(
                    text = "No account found. Please log out and log in again.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Cash Balance",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = rupees(cashBalance),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Total Credit",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = rupees(totalCredit),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Total Debit",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = rupees(totalDebit),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { showLogoutConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Log Out")
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log Out?") },
            text = { Text("You'll need to log in again to access your cash book.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        authViewModel.logout()
                        onLogout()
                    }
                ) {
                    Text("Log Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun AccountsScreenPreview() {

    CashBookTheme {

        AccountsScreen(
            onHomeClick = {},
            onCashBookClick = {},
            onReportsClick = {},
            onAccountsClick = {},
            onLogout = {}
        )
    }
}