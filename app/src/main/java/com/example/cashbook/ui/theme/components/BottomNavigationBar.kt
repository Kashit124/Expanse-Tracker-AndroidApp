package com.example.cashbook.ui.theme.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.example.cashbook.R
@Composable
fun BottomNavigationBar(
    selectedItem: String,
    onHomeClick: () -> Unit,
    onCashBookClick: () -> Unit,
    onReportsClick: () -> Unit,
    onAccountsClick: () -> Unit
) {

    NavigationBar(
        modifier = Modifier.fillMaxWidth()
    ) {

        NavigationBarItem(
            selected = selectedItem == "Home",
            onClick = onHomeClick,
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.home_icon),
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text("Home")
            }
        )

        NavigationBarItem(
            selected = selectedItem == "CashBook",
            onClick = onCashBookClick,
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.cashbook_icon),
                    contentDescription = "Cash Book",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Cash Book",
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    maxLines = 1
                )
            }
        )
        NavigationBarItem(
            selected = selectedItem == "Reports",
            onClick = onReportsClick,
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.report_icon),
                    contentDescription = "Report",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text("Reports")
            }
        )

        NavigationBarItem(
            selected = selectedItem == "Accounts",
            onClick = onAccountsClick,
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.account_icon),
                    contentDescription = "Account",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text("Accounts")
            }
        )
    }
}