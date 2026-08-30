package com.example.cashbook.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashbook.ui.theme.CashBookTheme
import com.example.cashbook.ui.theme.components.BottomNavigationBar
import com.example.cashbook.ui.theme.components.CreditButton
import com.example.cashbook.ui.theme.components.DebitButton
import com.example.cashbook.ui.theme.components.TransactionRowData
import com.example.cashbook.ui.theme.components.TransactionTable
import com.example.cashbook.ViewModel.TransactionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashBookScreen(
    onCreditClick: () -> Unit,
    onDebitClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCashBookClick: () -> Unit,
    onReportsClick: () -> Unit,
    onAccountsClick: () -> Unit,
    viewModel: TransactionViewModel = viewModel()
) {

    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val today = remember { LocalDate.now() }
    val todayIso = remember { today.toString() } // yyyy-MM-dd, matches backend format
    val todayDisplay = remember {
        today.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
    }

    // Refresh whenever this screen is shown
    LaunchedEffect(Unit) {
        viewModel.loadTransactions()
    }

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text("Cash Book")
                }
            )
        },

        bottomBar = {
            BottomNavigationBar(
                selectedItem = "CashBook",
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
                .padding(16.dp)
        ) {

            Text(
                text = todayDisplay,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                CreditButton(
                    onClick = onCreditClick,
                    modifier = Modifier.weight(1f)
                )

                DebitButton(
                    onClick = onDebitClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Today's Entries",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {

                val todaysTransactions = transactions
                    .filter { it.transactionDate == todayIso }
                    .map { transaction ->
                        TransactionRowData(
                            date = transaction.transactionDate,
                            particular = transaction.particular,
                            credit = if (transaction.transactionType == "CREDIT") "%.2f".format(transaction.amount) else "-",
                            debit = if (transaction.transactionType == "DEBIT") "%.2f".format(transaction.amount) else "-"
                        )
                    }

                if (todaysTransactions.isEmpty()) {
                    Text(
                        text = "No entries yet today. Tap Credit or Debit to add one.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    TransactionTable(
                        transactions = todaysTransactions
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CashBookScreenPreview() {
    CashBookTheme {
        CashBookScreen(
            onCreditClick = {},
            onDebitClick = {},
            onHomeClick = {},
            onCashBookClick = {},
            onReportsClick = {},
            onAccountsClick = {}
        )
    }
}