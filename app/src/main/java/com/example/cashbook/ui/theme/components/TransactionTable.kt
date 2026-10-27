package com.example.cashbook.ui.theme.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class TransactionRowData(
    val date: String,
    val particular: String,
    val credit: String,
    val debit: String
)

@Composable
fun TransactionTable(
    transactions: List<TransactionRowData>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {

        Row(
            modifier = Modifier
                .width(600.dp)
                .height(48.dp)
        ) {

            TableCell(
                text = "Date",
                width = 90.dp,
                bold = true
            )

            TableCell(
                text = "Particulars",
                width = 240.dp,
                bold = true
            )

            TableCell(
                text = "Credit (₹)",
                width = 130.dp,
                bold = true
            )

            TableCell(
                text = "Debit (₹)",
                width = 130.dp,
                bold = true
            )
        }

        transactions.forEach { transaction ->

            Row(
                modifier = Modifier
                    .width(600.dp)
                    .height(55.dp)
            ) {

                TableCell(
                    text = transaction.date,
                    width = 90.dp
                )

                TableCell(
                    text = transaction.particular,
                    width = 240.dp
                )

                TableCell(
                    text = transaction.credit,
                    width = 130.dp
                )

                TableCell(
                    text = transaction.debit,
                    width = 130.dp
                )
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    bold: Boolean = false
) {

    Surface(
        modifier = Modifier
            .width(width)
            .fillMaxHeight(),
        border = ButtonDefaults.outlinedButtonBorder
    ) {

        Box(
            modifier = Modifier.padding(8.dp),
            contentAlignment = androidx.compose.ui.Alignment.CenterStart
        ) {

                Text(
                text = text,
                style = if (bold) {
                    MaterialTheme.typography.labelLarge
                } else {
                    MaterialTheme.typography.bodyMedium
                }
            )
        }
    }
}