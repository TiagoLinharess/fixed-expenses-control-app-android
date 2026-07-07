package com.example.fixedexpeneses.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.PaymentStatus
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.ui.rememberAppViewModelFactory
import com.example.fixedexpeneses.ui.theme.FixedExpenesesTheme

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = viewModel(factory = rememberAppViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreen(
        uiState = uiState,
        onMonthSelected = viewModel::onMonthSelected,
        onDeleteTransaction = viewModel::deleteTransaction
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onMonthSelected: (Int) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var transactionPendingDeletion by remember { mutableStateOf<HomeTransactionItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        HomeFixedHeader(
            selectedMonthLabel = uiState.selectedMonthLabel,
            options = uiState.availableMonths,
            onMonthSelected = onMonthSelected
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            item {
                HomeProgressSection(
                    totalIncomeInCents = uiState.totalIncomeInCents,
                    totalExpenseInCents = uiState.totalExpenseInCents
                )
            }

            if (uiState.isGenerating) {
                item {
                    GeneratingMonthMessage()
                }
            }

            uiState.errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            monthlyTransactionsSection(
                transactions = uiState.transactions,
                onDeleteTransaction = { transactionPendingDeletion = it }
            )
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
private fun HomeFixedHeader(
    selectedMonthLabel: String,
    options: List<MonthSelectorOption>,
    onMonthSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeHeader()

        MonthSelector(
            selectedMonthLabel = selectedMonthLabel,
            options = options,
            onMonthSelected = onMonthSelected
        )
    }
}

@Composable
private fun HomeHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Home",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Selecione o mês para montar sua visão mensal.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MonthSelector(
    selectedMonthLabel: String,
    options: List<MonthSelectorOption>,
    onMonthSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedMonthLabel.ifBlank { "Selecionar mês" })
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onMonthSelected(option.yearMonth)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeProgressSection(
    totalIncomeInCents: Long,
    totalExpenseInCents: Long,
    modifier: Modifier = Modifier
) {
    val rawProgress = if (totalIncomeInCents > 0) {
        totalExpenseInCents.toFloat() / totalIncomeInCents.toFloat()
    } else {
        0f
    }
    val progress = rawProgress.coerceIn(0f, 1f)
    val percentage = (rawProgress * 100).toInt()
    val commitmentText = if (totalIncomeInCents > 0L) {
        "$percentage% do mês está comprometido com gastos fixos."
    } else {
        "Cadastre uma entrada para acompanhar o comprometimento fixo do mês."
    }
    val commitmentAlert = when {
        rawProgress > 1f -> IncomeCommitmentAlertLevel.EMERGENCY
        rawProgress > 0.9f -> IncomeCommitmentAlertLevel.SEVERE
        rawProgress > 0.6f -> IncomeCommitmentAlertLevel.CRITICAL
        rawProgress >= 0.5f -> IncomeCommitmentAlertLevel.WARNING
        totalIncomeInCents > 0L -> IncomeCommitmentAlertLevel.HEALTHY
        else -> null
    }
    val progressColor = if (rawProgress > 1f) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Comprometimento fixo do mês",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${totalExpenseInCents.toBrazilianCurrency()} de ${totalIncomeInCents.toBrazilianCurrency()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.titleSmall,
                color = progressColor,
                fontWeight = FontWeight.SemiBold
            )
        }

        HomeProgressBar(
            progress = progress,
            progressColor = progressColor,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = commitmentText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (commitmentAlert != null) {
            IncomeCommitmentAlert(level = commitmentAlert)
        }
    }
}

@Composable
private fun IncomeCommitmentAlert(
    level: IncomeCommitmentAlertLevel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = level.containerColor(),
            contentColor = level.contentColor()
        )
    ) {
        Text(
            text = level.message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun IncomeCommitmentAlertLevel.containerColor(): androidx.compose.ui.graphics.Color =
    when (this) {
        IncomeCommitmentAlertLevel.HEALTHY -> MaterialTheme.colorScheme.primaryContainer
        IncomeCommitmentAlertLevel.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
        IncomeCommitmentAlertLevel.CRITICAL,
        IncomeCommitmentAlertLevel.SEVERE,
        IncomeCommitmentAlertLevel.EMERGENCY -> MaterialTheme.colorScheme.errorContainer
    }

@Composable
private fun IncomeCommitmentAlertLevel.contentColor(): androidx.compose.ui.graphics.Color =
    when (this) {
        IncomeCommitmentAlertLevel.HEALTHY -> MaterialTheme.colorScheme.onPrimaryContainer
        IncomeCommitmentAlertLevel.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
        IncomeCommitmentAlertLevel.CRITICAL,
        IncomeCommitmentAlertLevel.SEVERE,
        IncomeCommitmentAlertLevel.EMERGENCY -> MaterialTheme.colorScheme.onErrorContainer
    }

private enum class IncomeCommitmentAlertLevel(val message: String) {
    HEALTHY(
        "Seu comprometimento fixo está abaixo de 50%. Esse é um ritmo saudável para manter espaço para gastos variáveis e imprevistos."
    ),
    WARNING(
        "Mais de 50% da sua renda está comprometida com gastos fixos. Isso deve ser um alerta para futuras contas que podem surgir."
    ),
    CRITICAL(
        "Mais de 60% da sua renda está comprometida com gastos fixos. Revise novas contas com cuidado antes de assumir mais compromissos."
    ),
    SEVERE(
        "Mais de 90% da sua renda está comprometida com gastos fixos. Evite assumir novas contas e revise seus gastos fixos com urgência."
    ),
    EMERGENCY(
        "Atenção: seus gastos fixos passaram de 100% da renda. O mês já está no negativo antes de considerar gastos variáveis."
    )
}

@Composable
private fun HomeProgressBar(
    progress: Float,
    progressColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(6.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(progressColor)
        )
    }
}

@Composable
private fun GeneratingMonthMessage(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
        Text(
            text = "Montando o mês...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun LazyListScope.monthlyTransactionsSection(
    transactions: List<HomeTransactionItem>,
    onDeleteTransaction: (HomeTransactionItem) -> Unit
) {
    if (transactions.isEmpty()) {
        item {
            Text(
                text = "Nenhuma transação gerada para este mês.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    item {
        Text(
            text = "Transações do mês",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }

    items(
        items = transactions,
        key = { it.id }
    ) { transaction ->
        SwipeToDeleteItem(onDelete = { onDeleteTransaction(transaction) }) {
            MonthlyTransactionItem(transaction)
            HorizontalDivider()
        }
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
        content = { content() }
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
        title = { Text("Excluir transação?") },
        text = { Text("Tem certeza que deseja excluir \"$transactionName\" deste mês? Essa ação não pode ser desfeita.") },
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
private fun MonthlyTransactionItem(transaction: HomeTransactionItem) {
    ListItem(
        headlineContent = {
            Text(transaction.name)
        },
        supportingContent = {
            Text(
                text = listOf(
                    transaction.type.label,
                    transaction.sourceDescription,
                    "Dia ${transaction.dueDay}",
                    transaction.paymentMethod?.label ?: "Sem método",
                    transaction.status.label
                ).joinToString(" - ")
            )
        },
        trailingContent = {
            Text(
                text = transaction.amountInCents.toBrazilianCurrency(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    )
}

private val TransactionType.label: String
    get() = when (this) {
        TransactionType.INCOME -> "Entrada"
        TransactionType.EXPENSE -> "Saída"
    }

private val PaymentMethod.label: String
    get() = when (this) {
        PaymentMethod.PIX -> "Pix"
        PaymentMethod.CASH -> "Dinheiro"
        PaymentMethod.CREDIT -> "Crédito"
        PaymentMethod.DEBIT -> "Débito"
    }

private val PaymentStatus.label: String
    get() = when (this) {
        PaymentStatus.PAID -> "Pago"
        PaymentStatus.PENDING -> "Pendente"
    }

private fun Long.toBrazilianCurrency(): String {
    val reais = this / 100
    val cents = kotlin.math.abs(this % 100)
    return "R$ $reais,${cents.toString().padStart(2, '0')}"
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    FixedExpenesesTheme {
        HomeScreen(
            uiState = HomeUiState(
                selectedYearMonth = 202607,
                currentYearMonth = 202607,
                firstAvailableYearMonth = 202607,
                availableMonths = yearMonthRange(202607, 202707).map {
                    MonthSelectorOption(it, it.toMonthYearLabel())
                },
                transactions = listOf(
                    HomeTransactionItem(
                        id = 1,
                        type = TransactionType.EXPENSE,
                        name = "Internet",
                        amountInCents = 12000,
                        dueDay = 10,
                        paymentMethod = PaymentMethod.CREDIT,
                        status = PaymentStatus.PENDING,
                        sourceDescription = "Fixa"
                    ),
                    HomeTransactionItem(
                        id = 2,
                        type = TransactionType.EXPENSE,
                        name = "Notebook",
                        amountInCents = 35000,
                        dueDay = 15,
                        paymentMethod = PaymentMethod.CREDIT,
                        status = PaymentStatus.PENDING,
                        sourceDescription = "Parcela 1/6"
                    )
                ),
                totalIncomeInCents = 100000,
                totalExpenseInCents = 47000,
                balanceInCents = 53000
            ),
            onMonthSelected = {},
            onDeleteTransaction = {}
        )
    }
}
