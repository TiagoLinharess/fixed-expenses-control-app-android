package com.example.fixedexpeneses.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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

            item {
                FinancialSuggestionsSection(
                    totalIncomeInCents = uiState.totalIncomeInCents,
                    totalExpenseInCents = uiState.totalExpenseInCents
                )
            }

            item {
                MonthlyPaymentStatusSection(
                    transactions = uiState.transactions
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

            item {
                MonthlyTransactionsSection(
                    transactions = uiState.transactions,
                    onDeleteTransaction = { transactionPendingDeletion = it }
                )
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
private fun MonthlyPaymentStatusSection(
    transactions: List<HomeTransactionItem>,
    modifier: Modifier = Modifier
) {
    val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
    val paidTotalInCents = expenseTransactions
        .filter { it.status == PaymentStatus.PAID }
        .sumOf { it.amountInCents }
    val pendingTransactions = expenseTransactions
        .filter { it.status == PaymentStatus.PENDING }
        .sortedBy { it.dueDay }
    val pendingTotalInCents = pendingTransactions.sumOf { it.amountInCents }
    val dueGroups = pendingTransactions
        .groupBy { it.dueDay }
        .toSortedMap()
        .map { (dueDay, items) ->
            DueDayGroup(
                dueDay = dueDay,
                transactionNames = items.map { it.name },
                totalInCents = items.sumOf { it.amountInCents }
            )
        }
    val visibleDueGroups = dueGroups.take(MAX_VISIBLE_DUE_GROUPS)
    val hiddenDueGroupsCount = (dueGroups.size - visibleDueGroups.size).coerceAtLeast(0)

    HomeSectionContainer(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Status do mês",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaymentStatusAmount(
                    title = "Pagas",
                    amountInCents = paidTotalInCents,
                    modifier = Modifier.weight(1f)
                )
                PaymentStatusAmount(
                    title = "Pendentes",
                    amountInCents = pendingTotalInCents,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()

            Text(
                text = "Próximos vencimentos",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (visibleDueGroups.isEmpty()) {
                Text(
                    text = "Nenhuma conta pendente neste mês.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                visibleDueGroups.forEach { dueGroup ->
                    DueDayGroupItem(dueGroup)
                }

                if (hiddenDueGroupsCount > 0) {
                    Text(
                        text = "+ $hiddenDueGroupsCount dias com contas pendentes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusAmount(
    title: String,
    amountInCents: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = amountInCents.toBrazilianCurrency(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DueDayGroupItem(
    dueGroup: DueDayGroup,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "Dia ${dueGroup.dueDay}: ${dueGroup.transactionNames.toReadableList()}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Total: ${dueGroup.totalInCents.toBrazilianCurrency()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class DueDayGroup(
    val dueDay: Int,
    val transactionNames: List<String>,
    val totalInCents: Long
)

private fun List<String>.toReadableList(): String =
    when (size) {
        0 -> ""
        1 -> first()
        2 -> "${this[0]} e ${this[1]}"
        else -> "${dropLast(1).joinToString(", ")} e ${last()}"
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

    HomeSectionContainer(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
}

@Composable
private fun IncomeCommitmentAlert(
    level: IncomeCommitmentAlertLevel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(level.containerColor())
            .padding(12.dp)
    ) {
        Text(
            text = level.message,
            style = MaterialTheme.typography.bodySmall,
            color = level.contentColor(),
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
private fun FinancialSuggestionsSection(
    totalIncomeInCents: Long,
    totalExpenseInCents: Long,
    modifier: Modifier = Modifier
) {
    val suggestions = remember(totalIncomeInCents, totalExpenseInCents) {
        buildFinancialSuggestions(
            totalIncomeInCents = totalIncomeInCents,
            totalExpenseInCents = totalExpenseInCents
        ).shuffled()
    }
    val suggestionPages = suggestions.chunked(SUGGESTIONS_PER_PAGE)
    val listState = rememberLazyListState()

    HomeSectionContainer(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Sugestões financeiras",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            if (totalIncomeInCents <= 0L) {
                Text(
                    text = "Cadastre uma entrada fixa para receber sugestões financeiras.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(suggestionPages) { _, page ->
                        Column(
                            modifier = Modifier.fillParentMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            page.forEach { suggestion ->
                                FinancialSuggestionItem(suggestion)
                            }
                        }
                    }
                }

                if (suggestionPages.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        suggestionPages.indices.forEach { index ->
                            val isSelected = index == listState.firstVisibleItemIndex
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(if (isSelected) 8.dp else 6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialSuggestionItem(
    suggestion: FinancialSuggestion,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = suggestion.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = suggestion.highlightedValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun buildFinancialSuggestions(
    totalIncomeInCents: Long,
    totalExpenseInCents: Long
): List<FinancialSuggestion> {
    if (totalIncomeInCents <= 0L) return emptyList()

    val monthlyReserveInCents = totalIncomeInCents.percent(10)
    val emergencyReserveTargetInCents = totalIncomeInCents * 6
    val flexibleLimitByIncomeInCents = totalIncomeInCents.percent(30)
    val flexibleLimitByAvailableIncomeInCents = (
        totalIncomeInCents - totalExpenseInCents - monthlyReserveInCents
    ).coerceAtLeast(0)
    val flexibleLimitInCents = minOf(
        flexibleLimitByIncomeInCents,
        flexibleLimitByAvailableIncomeInCents
    )
    val estimatedLeftoverInCents = totalIncomeInCents - totalExpenseInCents - monthlyReserveInCents
    val fiftyPercentIncomeInCents = totalIncomeInCents.percent(50)
    val roomBeforeFiftyPercentInCents = fiftyPercentIncomeInCents - totalExpenseInCents

    return buildList {
        add(
            FinancialSuggestion(
                title = "Reserva mensal sugerida",
                highlightedValue = monthlyReserveInCents.toBrazilianCurrency(),
                description = "para construir sua reserva de emergência este mês."
            )
        )
        add(
            FinancialSuggestion(
                title = "Meta de reserva de emergência",
                highlightedValue = emergencyReserveTargetInCents.toBrazilianCurrency(),
                description = "equivalente a 6 meses da sua entrada fixa."
            )
        )
        add(
            FinancialSuggestion(
                title = "Gastos flexíveis",
                highlightedValue = flexibleLimitInCents.toBrazilianCurrency(),
                description = "para dia a dia e lazer, considerando renda, gastos fixos e reserva."
            )
        )
        add(
            FinancialSuggestion(
                title = "Sobra estimada",
                highlightedValue = estimatedLeftoverInCents.toBrazilianCurrency(),
                description = "após gastos fixos e reserva sugerida para organizar o restante do mês."
            )
        )
        add(
            if (roomBeforeFiftyPercentInCents > 0) {
                FinancialSuggestion(
                    title = "Espaço para novas contas",
                    highlightedValue = roomBeforeFiftyPercentInCents.toBrazilianCurrency(),
                    description = "antes de chegar a 50% de comprometimento fixo."
                )
            } else {
                FinancialSuggestion(
                    title = "Novas contas",
                    highlightedValue = "R$ 0,00",
                    description = "recomendado para novas contas; seu comprometimento já passou de 50%."
                )
            }
        )
        add(
            FinancialSuggestion(
                title = "Reserva mínima para contas fixas",
                highlightedValue = (totalExpenseInCents * 6).toBrazilianCurrency(),
                description = "para cobrir 6 meses dos seus gastos fixos atuais."
            )
        )

        if (roomBeforeFiftyPercentInCents < 0) {
            add(
                FinancialSuggestion(
                    title = "Meta de redução",
                    highlightedValue = (-roomBeforeFiftyPercentInCents).toBrazilianCurrency(),
                    description = "em gastos fixos para voltar a 50% de comprometimento."
                )
            )
        }

        if (flexibleLimitInCents <= totalIncomeInCents.percent(10)) {
            add(
                FinancialSuggestion(
                    title = "Margem apertada",
                    highlightedValue = flexibleLimitInCents.toBrazilianCurrency(),
                    description = "sobram para dia a dia e lazer depois dos fixos e da reserva."
                )
            )
        }
    }
}

private data class FinancialSuggestion(
    val title: String,
    val highlightedValue: String,
    val description: String
)

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

@Composable
private fun MonthlyTransactionsSection(
    transactions: List<HomeTransactionItem>,
    onDeleteTransaction: (HomeTransactionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    HomeSectionContainer(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Transações do mês",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            if (transactions.isEmpty()) {
                Text(
                    text = "Nenhuma transação gerada para este mês.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                transactions.forEachIndexed { index, transaction ->
                    SwipeToDeleteItem(onDelete = { onDeleteTransaction(transaction) }) {
                        MonthlyTransactionItem(transaction)
                        if (index < transactions.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSectionContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(14.dp)
    ) {
        content()
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
    val absoluteValue = kotlin.math.abs(this)
    val signal = if (this < 0) "-" else ""
    val reais = absoluteValue / 100
    val cents = absoluteValue % 100
    return "${signal}R$ $reais,${cents.toString().padStart(2, '0')}"
}

private fun Long.percent(percent: Int): Long =
    this * percent / 100

private const val SUGGESTIONS_PER_PAGE = 3
private const val MAX_VISIBLE_DUE_GROUPS = 3

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
                    ),
                    HomeTransactionItem(
                        id = 3,
                        type = TransactionType.EXPENSE,
                        name = "Academia",
                        amountInCents = 9000,
                        dueDay = 5,
                        paymentMethod = PaymentMethod.PIX,
                        status = PaymentStatus.PAID,
                        sourceDescription = "Fixa"
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
