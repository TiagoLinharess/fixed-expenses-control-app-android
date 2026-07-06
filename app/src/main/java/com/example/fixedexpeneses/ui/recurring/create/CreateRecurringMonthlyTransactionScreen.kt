package com.example.fixedexpeneses.ui.recurring.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.ui.rememberAppViewModelFactory
import com.example.fixedexpeneses.ui.theme.FixedExpenesesTheme

@Composable
fun CreateRecurringMonthlyTransactionRoute(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CreateRecurringMonthlyTransactionViewModel = viewModel(
        factory = rememberAppViewModelFactory()
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaved()
        }
    }

    CreateRecurringMonthlyTransactionScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTypeChange = viewModel::onTypeChange,
        onNameChange = viewModel::onNameChange,
        onAmountChange = viewModel::onAmountChange,
        onAmountBehaviorChange = viewModel::onAmountBehaviorChange,
        onDueDayChange = viewModel::onDueDayChange,
        onPaymentMethodChange = viewModel::onPaymentMethodChange,
        onSaveClick = viewModel::save
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateRecurringMonthlyTransactionScreen(
    uiState: CreateRecurringMonthlyTransactionUiState,
    onBackClick: () -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onAmountBehaviorChange: (AmountBehavior) -> Unit,
    onDueDayChange: (String) -> Unit,
    onPaymentMethodChange: (PaymentMethod?) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Nova conta fixa",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Cadastre uma entrada ou saida mensal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onBackClick) {
                Text("Voltar")
            }
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
            label = { Text("Valor em reais") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        ChipSection(title = "Comportamento do valor") {
            AmountBehavior.entries.forEach { behavior ->
                FilterChip(
                    selected = uiState.amountBehavior == behavior,
                    onClick = { onAmountBehaviorChange(behavior) },
                    label = { Text(behavior.label) }
                )
            }
        }

        OutlinedTextField(
            value = uiState.dueDay,
            onValueChange = onDueDayChange,
            label = { Text("Dia do mes") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        ChipSection(title = "Metodo de pagamento") {
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
            Text(if (uiState.isSaving) "Salvando..." else "Salvar")
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
        TransactionType.EXPENSE -> "Saida"
    }

private val AmountBehavior.label: String
    get() = when (this) {
        AmountBehavior.FIXED -> "Fixo"
        AmountBehavior.VARIABLE -> "Variavel"
    }

private val PaymentMethod.label: String
    get() = when (this) {
        PaymentMethod.PIX -> "PIX"
        PaymentMethod.CASH -> "Dinheiro"
        PaymentMethod.CREDIT -> "Credito"
        PaymentMethod.DEBIT -> "Debito"
    }

@Preview(showBackground = true)
@Composable
private fun CreateRecurringMonthlyTransactionScreenPreview() {
    FixedExpenesesTheme {
        CreateRecurringMonthlyTransactionScreen(
            uiState = CreateRecurringMonthlyTransactionUiState(),
            onBackClick = {},
            onTypeChange = {},
            onNameChange = {},
            onAmountChange = {},
            onAmountBehaviorChange = {},
            onDueDayChange = {},
            onPaymentMethodChange = {},
            onSaveClick = {}
        )
    }
}
