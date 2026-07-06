package com.example.fixedexpeneses.ui.installment.detail

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.InstallmentTransaction
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.ui.installment.installmentCount
import com.example.fixedexpeneses.ui.installment.toMonthYearLabel
import com.example.fixedexpeneses.ui.installment.totalAmountInCents
import com.example.fixedexpeneses.ui.rememberAppViewModelFactory

@Composable
fun InstallmentTransactionDetailRoute(
    transactionId: Long,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: InstallmentTransactionDetailViewModel = viewModel(factory = rememberAppViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(transactionId) { viewModel.loadTransaction(transactionId) }
    InstallmentTransactionDetailScreen(uiState, onBackClick, onEditClick)
}

@Composable
fun InstallmentTransactionDetailScreen(
    uiState: InstallmentTransactionDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBackClick) { Text("Voltar") }
            uiState.transaction?.let { Button(onClick = { onEditClick(it.id) }) { Text("Editar") } }
        }
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            uiState.errorMessage != null -> Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
            uiState.transaction != null -> TransactionDetails(uiState.transaction)
        }
    }
}

@Composable
private fun TransactionDetails(transaction: InstallmentTransaction) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(transaction.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(transaction.amountInCents.toBrazilianCurrency(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        HorizontalDivider()
        DetailRow("Tipo", transaction.type.label)
        DetailRow("Valor da parcela", transaction.amountInCents.toBrazilianCurrency())
        DetailRow("Comportamento", transaction.amountBehavior.label)
        DetailRow("Dia do mês", transaction.dueDay.toString())
        DetailRow("Pagamento", transaction.paymentMethod?.label ?: "Não informado")
        DetailRow("Início", transaction.yearMonthFrom.toMonthYearLabel())
        DetailRow("Fim", transaction.yearMonthTo.toMonthYearLabel())
        DetailRow("Parcelas", transaction.installmentCount().toString())
        DetailRow("Total previsto", transaction.totalAmountInCents().toBrazilianCurrency())
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
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

private fun Long.toBrazilianCurrency(): String {
    val reais = this / 100
    val cents = kotlin.math.abs(this % 100)
    return "R$ $reais,${cents.toString().padStart(2, '0')}"
}
