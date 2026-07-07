package com.example.fixedexpeneses.ui.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.RecurringMonthlyTransaction
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.ui.rememberAppViewModelFactory
import com.example.fixedexpeneses.ui.theme.FixedExpenesesTheme

@Composable
fun RecurringMonthlyTransactionsRoute(
    onAddClick: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    viewModel: RecurringMonthlyTransactionsViewModel = viewModel(
        factory = rememberAppViewModelFactory()
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    RecurringMonthlyTransactionsScreen(
        uiState = uiState,
        onAddClick = onAddClick,
        onTransactionClick = onTransactionClick,
        onDeleteTransaction = viewModel::deleteTransaction
    )
}

@Composable
fun RecurringMonthlyTransactionsScreen(
    uiState: RecurringMonthlyTransactionsUiState,
    onAddClick: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var transactionPendingDeletion by remember { mutableStateOf<RecurringMonthlyTransaction?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Contas fixas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Entradas e saídas que entram no mês.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(onClick = onAddClick) {
                Text("Adicionar")
            }
        }

        FinancialSummary(
            totalIncomeInCents = uiState.totalIncomeInCents,
            totalExpenseInCents = uiState.totalExpenseInCents,
            balanceInCents = uiState.balanceInCents
        )

        when {
            uiState.isLoading -> {
                LoadingContent(modifier = Modifier.weight(1f))
            }

            uiState.errorMessage != null -> {
                MessageContent(
                    title = "Não foi possível carregar.",
                    message = uiState.errorMessage,
                    modifier = Modifier.weight(1f)
                )
            }

            uiState.items.isEmpty() -> {
                MessageContent(
                    title = "Nenhuma conta fixa ainda",
                    message = "Adicione uma entrada ou saída fixa para começar.",
                    modifier = Modifier.weight(1f)
                )
            }

            else -> {
                val sections = uiState.items.toRecurringSections()
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    sections.forEach { section ->
                        item(key = "section-${section.title}") {
                            TransactionSectionHeader(
                                title = section.title,
                                totalInCents = section.totalInCents
                            )
                        }
                        items(
                            items = section.items,
                            key = { it.id }
                        ) { transaction ->
                            SwipeToDeleteItem(
                                onDelete = { transactionPendingDeletion = transaction }
                            ) {
                                RecurringMonthlyTransactionItem(
                                    transaction = transaction,
                                    onClick = { onTransactionClick(transaction.id) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    transactionPendingDeletion?.let { transaction ->
        DeleteConfirmationDialog(
            transactionName = transaction.name,
            onDismiss = { transactionPendingDeletion = null },
            onConfirm = {
                onDeleteTransaction(transaction.id)
                transactionPendingDeletion = null
            }
        )
    }
}

@Composable
private fun FinancialSummary(
    totalIncomeInCents: Long,
    totalExpenseInCents: Long,
    balanceInCents: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryRow(
                label = "Entradas",
                value = totalIncomeInCents.toBrazilianCurrency()
            )
            SummaryRow(
                label = "Saídas",
                value = totalExpenseInCents.toBrazilianCurrency()
            )
            HorizontalDivider()
            SummaryRow(
                label = "Saldo",
                value = balanceInCents.toBrazilianCurrency(),
                valueColor = if (balanceInCents < 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TransactionSectionHeader(
    title: String,
    totalInCents: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = totalInCents.toBrazilianCurrency(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
@Suppress("DEPRECATION")
private fun SwipeToDeleteItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Excluir",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        content = {
            content()
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    transactionName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir conta fixa?") },
        text = { Text("Tem certeza que deseja excluir \"$transactionName\"? Essa ação não pode ser desfeita.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Excluir", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun RecurringMonthlyTransactionItem(
    transaction: RecurringMonthlyTransaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(transaction.name) },
        supportingContent = {
            Text(
                text = "${transaction.type.label} - ${transaction.amountBehavior.label} - Dia ${transaction.dueDay}"
            )
        },
        trailingContent = {
            Text(
                text = transaction.amountInCents.toBrazilianCurrency(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MessageContent(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val TransactionType.label: String
    get() = when (this) {
        TransactionType.INCOME -> "Entrada"
        TransactionType.EXPENSE -> "Saída"
    }

private val AmountBehavior.label: String
    get() = when (this) {
        AmountBehavior.FIXED -> "Fixo"
        AmountBehavior.VARIABLE -> "Variável"
    }

private data class RecurringTransactionSection(
    val title: String,
    val items: List<RecurringMonthlyTransaction>
) {
    val totalInCents: Long = items.sumOf { it.amountInCents }
}

private fun List<RecurringMonthlyTransaction>.toRecurringSections(): List<RecurringTransactionSection> {
    val incomeItems = filter { it.type == TransactionType.INCOME }
    val expenseItems = filter { it.type == TransactionType.EXPENSE }

    return buildList {
        if (incomeItems.isNotEmpty()) {
            add(RecurringTransactionSection("Entradas", incomeItems))
        }

        PaymentMethod.entries.forEach { method ->
            val items = expenseItems.filter { it.paymentMethod == method }
            if (items.isNotEmpty()) {
                add(RecurringTransactionSection(method.sectionLabel, items))
            }
        }

        val itemsWithoutPaymentMethod = expenseItems.filter { it.paymentMethod == null }
        if (itemsWithoutPaymentMethod.isNotEmpty()) {
            add(RecurringTransactionSection("Sem método", itemsWithoutPaymentMethod))
        }
    }
}

private val PaymentMethod.sectionLabel: String
    get() = when (this) {
        PaymentMethod.PIX -> "Pix"
        PaymentMethod.CASH -> "Dinheiro"
        PaymentMethod.CREDIT -> "Crédito"
        PaymentMethod.DEBIT -> "Débito"
    }

private fun Long.toBrazilianCurrency(): String {
    val reais = this / 100
    val cents = kotlin.math.abs(this % 100)
    return "R$ $reais,${cents.toString().padStart(2, '0')}"
}

@Preview(showBackground = true)
@Composable
private fun RecurringMonthlyTransactionsScreenPreview() {
    FixedExpenesesTheme {
        RecurringMonthlyTransactionsScreen(
            uiState = RecurringMonthlyTransactionsUiState(
                isLoading = false,
                items = listOf(
                    RecurringMonthlyTransaction(
                        id = 1,
                        type = TransactionType.EXPENSE,
                        name = "Internet",
                        amountInCents = 12000,
                        amountBehavior = AmountBehavior.FIXED,
                        dueDay = 10,
                        paymentMethod = PaymentMethod.CREDIT,
                        createdAt = 0,
                        updatedAt = 0
                    )
                ),
                totalExpenseInCents = 12000,
                balanceInCents = -12000
            ),
            onAddClick = {},
            onTransactionClick = {},
            onDeleteTransaction = {}
        )
    }
}
