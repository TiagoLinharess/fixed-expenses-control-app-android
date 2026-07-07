package com.example.fixedexpeneses.ui.transaction.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.PaymentStatus
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.ui.rememberAppViewModelFactory
import com.example.fixedexpeneses.ui.theme.FixedExpenesesTheme

@Composable
fun EditMonthlyTransactionRoute(
    transactionId: Long,
    onBackClick: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: EditMonthlyTransactionViewModel = viewModel(
        factory = rememberAppViewModelFactory()
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaved(uiState.id)
        }
    }

    EditMonthlyTransactionScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTypeChange = viewModel::onTypeChange,
        onNameChange = viewModel::onNameChange,
        onAmountChange = viewModel::onAmountChange,
        onDueDayChange = viewModel::onDueDayChange,
        onPaymentMethodChange = viewModel::onPaymentMethodChange,
        onStatusChange = viewModel::onStatusChange,
        onSaveClick = viewModel::save
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditMonthlyTransactionScreen(
    uiState: EditMonthlyTransactionUiState,
    onBackClick: () -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDueDayChange: (String) -> Unit,
    onPaymentMethodChange: (PaymentMethod?) -> Unit,
    onStatusChange: (PaymentStatus) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
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
                    text = "Editar transação",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Altere apenas esta transação do mês.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onBackClick) {
                Text("Voltar")
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            return@Column
        }

        ChipSection(title = "Tipo") {
            TransactionType.entries.forEach { type ->
                FilterChip(
                    selected = uiState.type == type,
                    onClick = { onTypeChange(type) },
                    label = { Text(type.label) }
                )
            }
        }

        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text("Nome") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.amount,
            onValueChange = onAmountChange,
            label = {
                Text(
                    if (uiState.amountBehavior == AmountBehavior.VARIABLE) {
                        "Confirmar valor em reais"
                    } else {
                        "Valor em reais"
                    }
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.dueDay,
            onValueChange = onDueDayChange,
            label = { Text("Dia do mês") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        ChipSection(title = "Método de pagamento") {
            FilterChip(
                selected = uiState.paymentMethod == null,
                onClick = { onPaymentMethodChange(null) },
                label = { Text("Nenhum") }
            )
            PaymentMethod.entries.forEach { method ->
                FilterChip(
                    selected = uiState.paymentMethod == method,
                    onClick = { onPaymentMethodChange(method) },
                    label = { Text(method.label) }
                )
            }
        }

        ChipSection(title = "Status") {
            PaymentStatus.entries.forEach { status ->
                FilterChip(
                    selected = uiState.status == status,
                    onClick = { onStatusChange(status) },
                    label = { Text(status.label) }
                )
            }
        }

        uiState.errorMessage?.let { errorMessage ->
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = onSaveClick,
            enabled = uiState.canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.isSaving) "Salvando..." else "Salvar alterações")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

private val TransactionType.label: String
    get() = when (this) {
        TransactionType.INCOME -> "Entrada"
        TransactionType.EXPENSE -> "Saída"
    }

private val PaymentMethod.label: String
    get() = when (this) {
        PaymentMethod.PIX -> "PIX"
        PaymentMethod.CASH -> "Dinheiro"
        PaymentMethod.CREDIT -> "Crédito"
        PaymentMethod.DEBIT -> "Débito"
    }

private val PaymentStatus.label: String
    get() = when (this) {
        PaymentStatus.PAID -> "Pago"
        PaymentStatus.PENDING -> "Pendente"
    }

@Preview(showBackground = true)
@Composable
private fun EditMonthlyTransactionScreenPreview() {
    FixedExpenesesTheme {
        EditMonthlyTransactionScreen(
            uiState = EditMonthlyTransactionUiState(
                id = 1,
                name = "Energia",
                amount = "180,00",
                amountBehavior = AmountBehavior.VARIABLE,
                dueDay = "10",
                paymentMethod = PaymentMethod.PIX,
                status = PaymentStatus.PENDING,
                isLoading = false
            ),
            onBackClick = {},
            onTypeChange = {},
            onNameChange = {},
            onAmountChange = {},
            onDueDayChange = {},
            onPaymentMethodChange = {},
            onStatusChange = {},
            onSaveClick = {}
        )
    }
}
