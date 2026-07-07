package com.example.fixedexpeneses.ui.transaction.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
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
import com.example.fixedexpeneses.domain.model.Transaction
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.ui.rememberAppViewModelFactory
import com.example.fixedexpeneses.ui.theme.FixedExpenesesTheme

@Composable
fun MonthlyTransactionDetailRoute(
    transactionId: Long,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: MonthlyTransactionDetailViewModel = viewModel(
        factory = rememberAppViewModelFactory()
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    MonthlyTransactionDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        onConfirmedAmountChange = viewModel::onConfirmedAmountChange,
        onConfirmAmountClick = viewModel::confirmAmount
    )
}

@Composable
fun MonthlyTransactionDetailScreen(
    uiState: MonthlyTransactionDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onConfirmedAmountChange: (String) -> Unit,
    onConfirmAmountClick: () -> Unit,
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
            TextButton(onClick = onBackClick) {
                Text("Voltar")
            }

            uiState.transaction?.let { transaction ->
                Button(onClick = { onEditClick(transaction.id) }) {
                    Text("Editar")
                }
            }
        }

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            uiState.transaction != null -> {
                TransactionDetails(
                    transaction = uiState.transaction,
                    amountBehavior = uiState.amountBehavior,
                    confirmedAmount = uiState.confirmedAmount,
                    isSavingConfirmedAmount = uiState.isSavingConfirmedAmount,
                    canConfirmAmount = uiState.canConfirmAmount,
                    onConfirmedAmountChange = onConfirmedAmountChange,
                    onConfirmAmountClick = onConfirmAmountClick
                )
            }
        }
    }
}

@Composable
private fun TransactionDetails(
    transaction: Transaction,
    amountBehavior: AmountBehavior?,
    confirmedAmount: String,
    isSavingConfirmedAmount: Boolean,
    canConfirmAmount: Boolean,
    onConfirmedAmountChange: (String) -> Unit,
    onConfirmAmountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = transaction.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = transaction.amountInCents.toBrazilianCurrency(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        HorizontalDivider()

        if (amountBehavior == AmountBehavior.VARIABLE) {
            ConfirmAmountSection(
                amount = confirmedAmount,
                isSaving = isSavingConfirmedAmount,
                canConfirm = canConfirmAmount,
                onAmountChange = onConfirmedAmountChange,
                onConfirmClick = onConfirmAmountClick
            )

            HorizontalDivider()
        }

        DetailRow(label = "Tipo", value = transaction.type.label)
        DetailRow(
            label = if (amountBehavior == AmountBehavior.VARIABLE) {
                "Valor confirmado"
            } else {
                "Valor"
            },
            value = transaction.amountInCents.toBrazilianCurrency()
        )
        DetailRow(label = "Comportamento", value = amountBehavior?.label ?: "Não informado")
        DetailRow(label = "Vencimento", value = "Dia ${transaction.dueDay}")
        DetailRow(label = "Pagamento", value = transaction.paymentMethod?.label ?: "Não informado")
        DetailRow(label = "Status", value = transaction.status.label)
        DetailRow(
            label = "Referência",
            value = "${transaction.referenceMonth.toString().padStart(2, '0')}/${transaction.referenceYear}"
        )
    }
}

@Composable
private fun ConfirmAmountSection(
    amount: String,
    isSaving: Boolean,
    canConfirm: Boolean,
    onAmountChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Confirmar valor") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onConfirmClick,
            enabled = canConfirm,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSaving) "Confirmando..." else "Confirmar valor")
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
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
            fontWeight = FontWeight.SemiBold
        )
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

private fun Long.toBrazilianCurrency(): String {
    val absoluteValue = kotlin.math.abs(this)
    val signal = if (this < 0) "-" else ""
    val reais = absoluteValue / 100
    val cents = absoluteValue % 100
    return "${signal}R$ $reais,${cents.toString().padStart(2, '0')}"
}

@Preview(showBackground = true)
@Composable
private fun MonthlyTransactionDetailScreenPreview() {
    FixedExpenesesTheme {
        MonthlyTransactionDetailScreen(
            uiState = MonthlyTransactionDetailUiState(
                isLoading = false,
                transaction = Transaction(
                    id = 1,
                    recurringMonthlyTransactionId = 1,
                    installmentTransactionId = null,
                    type = TransactionType.EXPENSE,
                    name = "Energia",
                    amountInCents = 18000,
                    dueDay = 10,
                    paymentMethod = PaymentMethod.PIX,
                    status = PaymentStatus.PENDING,
                    referenceMonth = 7,
                    referenceYear = 2026,
                    createdAt = 0,
                    updatedAt = 0
                ),
                amountBehavior = AmountBehavior.VARIABLE
            ),
            onBackClick = {},
            onEditClick = {},
            onConfirmedAmountChange = {},
            onConfirmAmountClick = {}
        )
    }
}
