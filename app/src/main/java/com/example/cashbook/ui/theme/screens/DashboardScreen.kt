package com.example.cashbook.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cashbook.ui.theme.components.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashbook.Repository.SessionManager
import com.example.cashbook.ui.theme.CashBookTheme
import com.example.cashbook.ViewModel.AccountViewModel
import com.example.cashbook.ViewModel.TransactionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DashboardScreen(
    onCreditClick: (String) -> Unit,
    onDebitClick: (String) -> Unit,
    onCashBookClick: () -> Unit,
    onReportsClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onHomeClick: () -> Unit,
    accountViewModel: AccountViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel()
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

    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    val selectedDateIso = remember(selectedDate) { selectedDate.toString() }
    val isToday = selectedDate == today
    val isFutureBlocked = selectedDate >= today

    val sortedTransactions = remember(transactions) {
        transactions.sortedWith(compareBy({ it.transactionDate }, { it.transactionId }))
    }

    val balanceAsOfSelectedDay = remember(sortedTransactions, selectedDate, openingBalance) {
        var balance = openingBalance
        for (t in sortedTransactions) {
            val date = LocalDate.parse(t.transactionDate)
            if (!date.isAfter(selectedDate)) {
                balance += if (t.transactionType == "CREDIT") t.amount else -t.amount
            } else {
                break
            }
        }
        balance
    }

    val selectedDayTransactions = transactions
        .filter { it.transactionDate == selectedDateIso }
        .map { transaction ->
            TransactionRowData(
                date = transaction.transactionDate,
                particular = transaction.particular,
                credit = if (transaction.transactionType == "CREDIT") "%,.2f".format(Locale.US, transaction.amount) else "-",
                debit = if (transaction.transactionType == "DEBIT") "%,.2f".format(Locale.US, transaction.amount) else "-"
            )
        }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Dashboard")
                }
            )
        },

        bottomBar = {
            BottomNavigationBar(
                selectedItem = "Home",
                onHomeClick = onHomeClick,
                onCashBookClick = onCashBookClick,
                onReportsClick = onReportsClick,
                onAccountsClick = onAccountsClick
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            if (accountId == null) {
                Text(
                    text = "No account found. Please log out and log in again.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                return@Column
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = { selectedDate = selectedDate.minusDays(1) }
                ) {
                    Text(text = "‹")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (isToday) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        TextButton(
                            onClick = { selectedDate = today },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "Jump to Today", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                TextButton(
                    onClick = { selectedDate = selectedDate.plusDays(1) },
                    enabled = !isFutureBlocked
                ) {
                    Text(text = "›")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                CreditButton(
                    onClick = { onCreditClick(selectedDateIso) },
                    modifier = Modifier.weight(1f)
                )

                DebitButton(
                    onClick = { onDebitClick(selectedDateIso) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (!isToday) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Credit/Debit will add a new entry dated ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = if (isToday) "Current Balance" else "Balance as of ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMM"))}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "₹" + String.format(Locale.US, "%,.2f", balanceAsOfSelectedDay),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isToday) "Today's Transactions" else "Transactions",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (selectedDayTransactions.isEmpty()) {
                Text(
                    text = "No transactions on this day.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                TransactionTable(
                    transactions = selectedDayTransactions
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    CashBookTheme {
        DashboardScreen(
            onCreditClick = {},
            onDebitClick = {},
            onCashBookClick = {},
            onReportsClick = {},
            onAccountsClick = {},
            onHomeClick = {}
        )
    }
}