package com.example.cashbook.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashbook.R
import com.example.cashbook.ui.theme.CashBookTheme
import com.example.cashbook.ViewModel.TransactionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CreditEntryScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    targetDate: String? = null, // ISO "yyyy-MM-dd"; null = today
    viewModel: TransactionViewModel = viewModel()
) {

    // TODO: replace with a real account picker once multi-account support is added
    val accountId = 1

    val entryDate = remember(targetDate) {
        targetDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
    }
    val entryDateIso = remember(entryDate) { entryDate.toString() }
    val entryDateDisplay = remember(entryDate) {
        entryDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
    }

    var particular by remember { mutableStateOf("") }
    var jf by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {

            TopAppBar(
                title = {
                    Text("Credit Entry")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.leftarrow_icon),
                            contentDescription = "Back",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = entryDateDisplay,
                onValueChange = {},
                label = {
                    Text("Date")
                },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            OutlinedTextField(
                value = particular,
                onValueChange = {
                    particular = it
                },
                label = {
                    Text("Particulars")
                },
                placeholder = {
                    Text("To All Cash Receipts")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = jf,
                onValueChange = {
                    jf = it
                },
                label = {
                    Text("J.F.")
                },
                placeholder = {
                    Text("Optional")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                },
                label = {
                    Text("Amount (₹)")
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it
                },
                label = {
                    Text("Notes")
                },
                placeholder = {
                    Text("Optional")
                },
                modifier = Modifier.fillMaxWidth()
            )

            validationError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()

                    when {
                        particular.isBlank() -> {
                            validationError = "Particulars is required"
                        }
                        amountValue == null || amountValue <= 0 -> {
                            validationError = "Enter a valid amount"
                        }
                        else -> {
                            validationError = null
                            viewModel.addCreditEntry(
                                accountId = accountId,
                                date = entryDateIso,
                                particular = particular,
                                amount = amountValue,
                                jf = jf.ifBlank { null },
                                notes = notes.ifBlank { null }
                            ) { success ->
                                if (success) {
                                    onSaved()
                                } else {
                                    validationError = "Failed to save. Check your connection."
                                }
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Save Credit Entry"
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CreditEntryScreenPreview() {
    CashBookTheme() {
        CreditEntryScreen(
            onBack = {},
            onSaved = {}
        )
    }
}