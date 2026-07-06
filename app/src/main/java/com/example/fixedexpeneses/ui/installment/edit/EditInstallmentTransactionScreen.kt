package com.example.fixedexpeneses.ui.installment.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fixedexpeneses.ui.installment.create.CreateInstallmentTransactionScreen
import com.example.fixedexpeneses.ui.installment.create.CreateInstallmentTransactionUiState
import com.example.fixedexpeneses.ui.rememberAppViewModelFactory

@Composable
fun EditInstallmentTransactionRoute(
    transactionId: Long,
    onBackClick: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: EditInstallmentTransactionViewModel = viewModel(factory = rememberAppViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(transactionId) { viewModel.loadTransaction(transactionId) }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved(uiState.id)
    }
    CreateInstallmentTransactionScreen(
        uiState = uiState.toCreateState(),
        onBackClick = onBackClick,
        onTypeChange = viewModel::onTypeChange,
        onNameChange = viewModel::onNameChange,
        onAmountChange = viewModel::onAmountChange,
        onAmountBehaviorChange = viewModel::onAmountBehaviorChange,
        onDueDayChange = viewModel::onDueDayChange,
        onPaymentMethodChange = viewModel::onPaymentMethodChange,
        onYearMonthFromChange = viewModel::onYearMonthFromChange,
        onYearMonthToChange = viewModel::onYearMonthToChange,
        onSaveClick = viewModel::save,
        title = "Editar parcelada",
        subtitle = "Atualize a parcela e o período.",
        saveButtonText = "Salvar alterações"
    )
}

private fun EditInstallmentTransactionUiState.toCreateState(): CreateInstallmentTransactionUiState =
    CreateInstallmentTransactionUiState(
        type = type,
        name = name,
        amount = amount,
        amountBehavior = amountBehavior,
        dueDay = dueDay,
        paymentMethod = paymentMethod,
        yearMonthFrom = yearMonthFrom,
        yearMonthTo = yearMonthTo,
        isSaving = isSaving,
        isSaved = isSaved,
        errorMessage = errorMessage
    )
