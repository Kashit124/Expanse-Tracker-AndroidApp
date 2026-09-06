package com.example.cashbook.ui.theme.screens

import android.content.Context
import android.net.Uri

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.cashbook.Repository.SessionManager
import com.example.cashbook.ui.theme.CashBookTheme
import com.example.cashbook.ui.theme.components.BottomNavigationBar
import com.example.cashbook.ViewModel.AccountViewModel
import com.example.cashbook.ViewModel.TransactionViewModel
import com.example.cashbook.model.TransactionResponse

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale


data class MonthlyRow(
    val date: String,
    val opening: String,
    val receipts: String,
    val payments: String,
    val closing: String
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(
    onHomeClick: () -> Unit,
    onCashBookClick: () -> Unit,
    onReportsClick: () -> Unit,
    onAccountsClick: () -> Unit,
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

    val openingBalanceOfAccount = accounts.find { it.accountId == accountId }?.openingBalance ?: 0.0

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val monthStart = currentMonth.atDay(1)
    val monthEnd = currentMonth.atEndOfMonth()

    val sortedTransactions = remember(transactions) {
        transactions.sortedWith(compareBy({ it.transactionDate }, { it.transactionId }))
    }

    val balanceBeforeMonth = remember(sortedTransactions, currentMonth, openingBalanceOfAccount) {
        var balance = openingBalanceOfAccount
        for (t in sortedTransactions) {
            val date = LocalDate.parse(t.transactionDate)
            if (date.isBefore(monthStart)) {
                balance += if (t.transactionType == "CREDIT") t.amount else -t.amount
            } else {
                break
            }
        }
        balance
    }

    val monthTransactions: List<TransactionResponse> = remember(sortedTransactions, currentMonth) {
        sortedTransactions.filter {
            val date = LocalDate.parse(it.transactionDate)
            !date.isBefore(monthStart) && !date.isAfter(monthEnd)
        }
    }

    val monthlyRows = remember(monthTransactions, balanceBeforeMonth) {
        val result = mutableListOf<MonthlyRow>()
        var runningBalance = balanceBeforeMonth

        monthTransactions
            .groupBy { it.transactionDate }
            .toSortedMap()
            .forEach { (date, dayTransactions) ->

                val receipts = dayTransactions.filter { it.transactionType == "CREDIT" }.sumOf { it.amount }
                val payments = dayTransactions.filter { it.transactionType == "DEBIT" }.sumOf { it.amount }
                val opening = runningBalance
                val closing = opening + receipts - payments

                result.add(
                    MonthlyRow(
                        date = date,
                        opening = String.format(Locale.US, "%,.2f", opening),
                        receipts = String.format(Locale.US, "%,.2f", receipts),
                        payments = String.format(Locale.US, "%,.2f", payments),
                        closing = String.format(Locale.US, "%,.2f", closing)
                    )
                )

                runningBalance = closing
            }

        result
    }

    val totalReceipts = monthTransactions.filter { it.transactionType == "CREDIT" }.sumOf { it.amount }
    val totalPayments = monthTransactions.filter { it.transactionType == "DEBIT" }.sumOf { it.amount }
    val closingBalance = balanceBeforeMonth + totalReceipts - totalPayments

    val fileNameSafeMonth = currentMonth.format(DateTimeFormatter.ofPattern("MMMM_yyyy"))

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            exportMonthlyReport(
                context = context,
                uri = uri,
                rows = monthlyRows,
                totalReceipts = totalReceipts,
                totalPayments = totalPayments,
                closingBalance = closingBalance
            )
        }
    }

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text("Monthly Report")
                }
            )
        },

        bottomBar = {
            BottomNavigationBar(
                selectedItem = "Reports",
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
                .padding(12.dp)
        ) {

            if (accountId == null) {
                Text(
                    text = "No account found. Please log out and log in again.",
                    color = MaterialTheme.colorScheme.error
                )
                return@Column
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = { currentMonth = currentMonth.minusMonths(1) }
                ) {
                    Text(text = "‹")
                }

                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleLarge
                )

                TextButton(
                    onClick = { currentMonth = currentMonth.plusMonths(1) }
                ) {
                    Text(text = "›")
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            if (isLoading) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            } else if (monthlyRows.isEmpty()) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions this month.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            } else {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {

                    Row(
                        modifier = Modifier
                            .width(680.dp)
                            .height(60.dp)
                    ) {

                        ReportCell(text = "Date", width = 80.dp, bold = true)
                        ReportCell(text = "Opening", width = 150.dp, bold = true)
                        ReportCell(text = "Receipts", width = 150.dp, bold = true)
                        ReportCell(text = "Payments", width = 150.dp, bold = true)
                        ReportCell(text = "Closing", width = 150.dp, bold = true)
                    }

                    monthlyRows.forEach { row ->

                        Row(
                            modifier = Modifier
                                .width(680.dp)
                                .height(55.dp)
                        ) {

                            ReportCell(text = row.date, width = 80.dp)
                            ReportCell(text = row.opening, width = 150.dp)
                            ReportCell(text = row.receipts, width = 150.dp)
                            ReportCell(text = row.payments, width = 150.dp)
                            ReportCell(text = row.closing, width = 150.dp)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .width(680.dp)
                            .height(60.dp)
                    ) {

                        ReportCell(text = "Total", width = 80.dp, bold = true)
                        ReportCell(text = "-", width = 150.dp, bold = true)
                        ReportCell(text = String.format(Locale.US, "%,.2f", totalReceipts), width = 150.dp, bold = true)
                        ReportCell(text = String.format(Locale.US, "%,.2f", totalPayments), width = 150.dp, bold = true)
                        ReportCell(text = String.format(Locale.US, "%,.2f", closingBalance), width = 150.dp, bold = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(

                onClick = {
                    csvLauncher.launch("CashBook_$fileNameSafeMonth.csv")
                },

                enabled = monthlyRows.isNotEmpty(),

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {

                Text(text = "Download Excel")
            }
        }
    }
}


@Composable
private fun ReportCell(
    text: String,
    width: Dp,
    bold: Boolean = false
) {

    Surface(

        modifier = Modifier
            .width(width)
            .fillMaxHeight(),

        border = ButtonDefaults.outlinedButtonBorder
    ) {

        Box(

            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),

            contentAlignment = Alignment.Center
        ) {

            Text(
                text = text,

                style = if (bold) {
                    MaterialTheme.typography.labelLarge
                } else {
                    MaterialTheme.typography.bodySmall
                }
            )
        }
    }
}


private fun exportMonthlyReport(
    context: Context,
    uri: Uri,
    rows: List<MonthlyRow>,
    totalReceipts: Double,
    totalPayments: Double,
    closingBalance: Double
) {

    try {

        val csv = buildString {

            appendLine("Date,Opening,Receipts,Payments,Closing")

            rows.forEach { row ->
                appendLine(
                    "${row.date},${row.opening},${row.receipts},${row.payments},${row.closing}"
                )
            }

            appendLine()
            appendLine(
                "Total,-,${String.format(Locale.US, "%.2f", totalReceipts)}," +
                        "${String.format(Locale.US, "%.2f", totalPayments)}," +
                        "${String.format(Locale.US, "%.2f", closingBalance)}"
            )
        }

        val outputStream = context.contentResolver.openOutputStream(uri)

        if (outputStream != null) {
            outputStream.write(csv.toByteArray(Charsets.UTF_8))
            outputStream.close()
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}


@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun MonthlyReportScreenPreview() {

    CashBookTheme {

        MonthlyReportScreen(
            onHomeClick = {},
            onCashBookClick = {},
            onReportsClick = {},
            onAccountsClick = {}
        )
    }
}