package com.example.fixedexpeneses.ui.recurring.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun RecurringMonthlyTransactionDetailRoute(
    transactionId: Long,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: RecurringMonthlyTransactionDetailViewModel = viewModel(
        factory = rememberAppViewModelFactory()
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    RecurringMonthlyTransactionDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = onEditClick
    )
}

@Composable
fun RecurringMonthlyTransactionDetailScreen(
    uiState: RecurringMonthlyTransactionDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
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
                TransactionDetails(transaction = uiState.transaction)
            }
        }
    }
}

@Composable
private fun TransactionDetails(
    transaction: RecurringMonthlyTransaction,
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

        DetailRow(label = "Tipo", value = transaction.type.label)
        DetailRow(label = "Valor", value = transaction.amountBehavior.label)
        DetailRow(label = "Dia do mês", value = transaction.dueDay.toString())
        DetailRow(label = "Pagamento", value = transaction.paymentMethod?.label ?: "Não informado")
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

private fun Long.toBrazilianCurrency(): String {
    val reais = this / 100
    val cents = kotlin.math.abs(this % 100)
    return "R$ $reais,${cents.toString().padStart(2, '0')}"
}

@Preview(showBackground = true)
@Composable
private fun RecurringMonthlyTransactionDetailScreenPreview() {
    FixedExpenesesTheme {
        RecurringMonthlyTransactionDetailScreen(
            uiState = RecurringMonthlyTransactionDetailUiState(
                isLoading = false,
                transaction = RecurringMonthlyTransaction(
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
            onBackClick = {},
            onEditClick = {}
        )
    }
}
