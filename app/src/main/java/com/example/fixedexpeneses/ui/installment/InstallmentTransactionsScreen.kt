package com.example.fixedexpeneses.ui.installment

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
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.InstallmentTransaction
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.ui.rememberAppViewModelFactory
import com.example.fixedexpeneses.ui.theme.FixedExpenesesTheme

@Composable
fun InstallmentTransactionsRoute(
    onAddClick: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    viewModel: InstallmentTransactionsViewModel = viewModel(
        factory = rememberAppViewModelFactory()
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    InstallmentTransactionsScreen(
        uiState = uiState,
        onAddClick = onAddClick,
        onTransactionClick = onTransactionClick,
        onDeleteTransaction = viewModel::deleteTransaction
    )
}

@Composable
fun InstallmentTransactionsScreen(
    uiState: InstallmentTransactionsUiState,
    onAddClick: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = "Parceladas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Compras e valores com período definido.",
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
            balanceInCents = uiState.balanceInCents,
            activeItemsCount = uiState.activeItemsCount,
            currentYearMonth = uiState.currentYearMonth
        )

        when {
            uiState.isLoading -> LoadingContent(modifier = Modifier.weight(1f))
            uiState.errorMessage != null -> MessageContent(
                title = "Não foi possível carregar.",
                message = uiState.errorMessage,
                modifier = Modifier.weight(1f)
            )
            uiState.items.isEmpty() -> MessageContent(
                title = "Nenhuma parcelada ainda",
                message = "Adicione uma compra ou entrada parcelada para começar.",
                modifier = Modifier.weight(1f)
            )
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items = uiState.items, key = { it.id }) { transaction ->
                    SwipeToDeleteItem(onDelete = { onDeleteTransaction(transaction.id) }) {
                        InstallmentTransactionItem(
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

@Composable
private fun FinancialSummary(
    totalIncomeInCents: Long,
    totalExpenseInCents: Long,
    balanceInCents: Long,
    activeItemsCount: Int,
    currentYearMonth: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Resumo de ${currentYearMonth.toMonthYearLabel()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            SummaryRow("Parceladas ativas", activeItemsCount.toString())
            SummaryRow("Entradas no mês", totalIncomeInCents.toBrazilianCurrency())
            SummaryRow("Saídas no mês", totalExpenseInCents.toBrazilianCurrency())
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
    valueColor: Color = MaterialTheme.colorScheme.onSurface
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
@Suppress("DEPRECATION")
private fun SwipeToDeleteItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
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
        content = { content() }
    )
}

@Composable
private fun InstallmentTransactionItem(
    transaction: InstallmentTransaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(transaction.name) },
        supportingContent = {
            Text(
                text = "${transaction.type.label} - ${transaction.yearMonthFrom.toMonthYearLabel()} até ${transaction.yearMonthTo.toMonthYearLabel()}"
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
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MessageContent(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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

private fun Long.toBrazilianCurrency(): String {
    val reais = this / 100
    val cents = kotlin.math.abs(this % 100)
    return "R$ $reais,${cents.toString().padStart(2, '0')}"
}

@Preview(showBackground = true)
@Composable
private fun InstallmentTransactionsScreenPreview() {
    FixedExpenesesTheme {
        InstallmentTransactionsScreen(
            uiState = InstallmentTransactionsUiState.fromItems(
                items = listOf(
                    InstallmentTransaction(
                        id = 1,
                        type = TransactionType.EXPENSE,
                        name = "Notebook",
                        amountInCents = 35000,
                        amountBehavior = AmountBehavior.FIXED,
                        dueDay = 15,
                        paymentMethod = PaymentMethod.CREDIT,
                        yearMonthFrom = 202607,
                        yearMonthTo = 202612,
                        createdAt = 0,
                        updatedAt = 0
                    )
                ),
                currentYearMonth = 202607
            ),
            onAddClick = {},
            onTransactionClick = {},
            onDeleteTransaction = {}
        )
    }
}
