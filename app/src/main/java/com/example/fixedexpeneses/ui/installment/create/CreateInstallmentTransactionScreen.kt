package com.example.fixedexpeneses.ui.installment.create

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.ui.installment.MonthYearVisualTransformation
import com.example.fixedexpeneses.ui.rememberAppViewModelFactory

@Composable
fun CreateInstallmentTransactionRoute(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CreateInstallmentTransactionViewModel = viewModel(
        factory = rememberAppViewModelFactory()
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    CreateInstallmentTransactionScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTypeChange = viewModel::onTypeChange,
        onNameChange = viewModel::onNameChange,
        onAmountChange = viewModel::onAmountChange,
        onAmountBehaviorChange = viewModel::onAmountBehaviorChange,
        onDueDayChange = viewModel::onDueDayChange,
        onPaymentMethodChange = viewModel::onPaymentMethodChange,
        onYearMonthFromChange = viewModel::onYearMonthFromChange,
        onYearMonthToChange = viewModel::onYearMonthToChange,
        onSaveClick = viewModel::save
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateInstallmentTransactionScreen(
    uiState: CreateInstallmentTransactionUiState,
    onBackClick: () -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onAmountBehaviorChange: (AmountBehavior) -> Unit,
    onDueDayChange: (String) -> Unit,
    onPaymentMethodChange: (PaymentMethod?) -> Unit,
    onYearMonthFromChange: (String) -> Unit,
    onYearMonthToChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Nova parcelada",
    subtitle: String = "Cadastre uma transação com período.",
    saveButtonText: String = "Salvar"
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Header(title = title, subtitle = subtitle, onBackClick = onBackClick)
        ChipSection("Tipo") {
            TransactionType.entries.forEach { type ->
                FilterChip(selected = uiState.type == type, onClick = { onTypeChange(type) }, label = { Text(type.label) })
            }
        }
        OutlinedTextField(uiState.name, onNameChange, label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(uiState.amount, onAmountChange, label = { Text("Valor da parcela em reais") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
        ChipSection("Comportamento do valor") {
            AmountBehavior.entries.forEach { behavior ->
                FilterChip(selected = uiState.amountBehavior == behavior, onClick = { onAmountBehaviorChange(behavior) }, label = { Text(behavior.label) })
            }
        }
        OutlinedTextField(uiState.dueDay, onDueDayChange, label = { Text("Dia do mês") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(uiState.yearMonthFrom, onYearMonthFromChange, label = { Text("Início (MM/AAAA)") }, singleLine = true, visualTransformation = MonthYearVisualTransformation, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            OutlinedTextField(uiState.yearMonthTo, onYearMonthToChange, label = { Text("Fim (MM/AAAA)") }, singleLine = true, visualTransformation = MonthYearVisualTransformation, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
        }
        ChipSection("Método de pagamento") {
            FilterChip(selected = uiState.paymentMethod == null, onClick = { onPaymentMethodChange(null) }, label = { Text("Nenhum") })
            PaymentMethod.entries.forEach { method ->
                FilterChip(selected = uiState.paymentMethod == method, onClick = { onPaymentMethodChange(method) }, label = { Text(method.label) })
            }
        }
        uiState.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Button(onClick = onSaveClick, enabled = uiState.canSave, modifier = Modifier.fillMaxWidth()) {
            Text(if (uiState.isSaving) "Salvando..." else saveButtonText)
        }
    }
}

@Composable
private fun Header(title: String, subtitle: String, onBackClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onBackClick) { Text("Voltar") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

private val TransactionType.label: String
    get() = if (this == TransactionType.INCOME) "Entrada" else "Saída"

private val AmountBehavior.label: String
    get() = if (this == AmountBehavior.FIXED) "Fixo" else "Variável"

private val PaymentMethod.label: String
    get() = when (this) {
        PaymentMethod.PIX -> "PIX"
        PaymentMethod.CASH -> "Dinheiro"
        PaymentMethod.CREDIT -> "Crédito"
        PaymentMethod.DEBIT -> "Débito"
    }
