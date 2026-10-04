package com.myledger.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.myledger.app.data.local.*
import com.myledger.app.data.parser.CategoryEngine
import com.myledger.app.ui.MainViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels {
        MainViewModel.Factory((application as MyLedgerApplication).repository)
    }

    private val smsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestSmsPermissionIfNeeded()
        setContent {
            MaterialTheme {
                MyLedgerScreen(vm)
            }
        }
    }

    private fun requestSmsPermissionIfNeeded() {
        val permissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (permissions.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }) {
            smsPermissionLauncher.launch(permissions)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLedgerScreen(vm: MainViewModel) {
    val transactions by vm.filteredTransactions.collectAsState()
    val income by vm.totalIncome.collectAsState()
    val expense by vm.totalExpense.collectAsState()
    val categories by vm.categoryTotals.collectAsState()

    var showAdd by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var darkMode by remember { mutableStateOf(false) }

    val content: @Composable () -> Unit = {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("MyLedger", fontWeight = FontWeight.Bold)
                            Text("ማህረቤ • Personal Finance", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSettings = true }) { Text("⚙") }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAdd = true },
                    text = { Text("Add") },
                    icon = { Text("+") }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = search,
                        onValueChange = {
                            search = it
                            vm.setSearch(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Search transactions") },
                        leadingIcon = { Text("⌕") }
                    )
                }

                item {
                    BalanceCard(income, expense, income - expense)
                }

                item {
                    Text("Spending by category", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    if (categories.isEmpty()) {
                        Text("No expense data yet.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        categories.take(5).forEach { item ->
                            CategoryBar(item.category.name, item.total, expense)
                        }
                    }
                }

                item {
                    Text("Transactions", style = MaterialTheme.typography.titleLarge)
                }

                if (transactions.isEmpty()) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(
                                Modifier.fillMaxWidth().padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No transactions yet", fontWeight = FontWeight.Bold)
                                Text("Add one manually or let MyLedger detect a supported SMS.")
                            }
                        }
                    }
                }

                items(transactions, key = { it.id }) { transaction ->
                    TransactionRow(transaction, onDelete = { vm.delete(transaction) })
                }
            }
        }
    }

    if (darkMode) {
        MaterialTheme(colorScheme = darkColorScheme()) { content() }
    } else {
        MaterialTheme { content() }
    }

    if (showAdd) {
        AddTransactionDialog(
            onDismiss = { showAdd = false },
            onAdd = {
                vm.add(it)
                showAdd = false
            }
        )
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("SMS monitoring is enabled when Android grants SMS permissions.")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Dark mode", Modifier.weight(1f))
                        Switch(darkMode, { darkMode = it })
                    }
                    Text("Version 1.0.0", style = MaterialTheme.typography.labelSmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun BalanceCard(income: Double, expense: Double, balance: Double) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Current balance", style = MaterialTheme.typography.labelLarge)
            Text(money(balance), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Metric("Income", income)
                Metric("Expenses", expense)
            }
        }
    }
}

@Composable
private fun Metric(label: String, amount: Double) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(money(amount), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CategoryBar(name: String, total: Double, allExpenses: Double) {
    val fraction = if (allExpenses > 0) (total / allExpenses).toFloat().coerceIn(0f, 1f) else 0f
    Column(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() })
            Text(money(total))
        }
        LinearProgressIndicator(fraction, Modifier.fillMaxWidth())
    }
}

@Composable
private fun TransactionRow(transaction: TransactionEntity, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(transaction.vendor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${transaction.category.name.replace('_', ' ')} • ${transaction.source.name}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                        .format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.labelSmall
                )
                if (transaction.isAutoParsed) {
                    Text("Auto detected from SMS", style = MaterialTheme.typography.labelSmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val prefix = when (transaction.type) {
                    TransactionType.INCOME -> "+ "
                    TransactionType.EXPENSE -> "- "
                    TransactionType.TRANSFER -> "↔ "
                }
                Text(prefix + money(transaction.amount), fontWeight = FontWeight.Bold)
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onAdd: (TransactionEntity) -> Unit
) {
    var vendor by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expense by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    vendor, { vendor = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Vendor / description") },
                    singleLine = true
                )
                OutlinedTextField(
                    amount, { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount (ETB)") },
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(expense, { expense = true }, label = { Text("Expense") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(!expense, { expense = false }, label = { Text("Income") })
                }
                Text(
                    "Category: " +
                        CategoryEngine.categorize(vendor).name.replace('_', ' '),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value = amount.replace(",", "").toDoubleOrNull()
                if (value != null && value > 0) {
                    onAdd(
                        TransactionEntity(
                            amount = value,
                            type = if (expense) TransactionType.EXPENSE else TransactionType.INCOME,
                            vendor = vendor.ifBlank { "Manual transaction" },
                            category = if (expense)
                                CategoryEngine.categorize(vendor)
                            else ExpenseCategory.SALARY,
                            source = PaymentSource.MANUAL
                        )
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun money(value: Double): String =
    NumberFormat.getNumberInstance(Locale.US).format(value) + " ETB"
