package com.example.fixedexpeneses.ui.installment.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.InstallmentTransaction
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.domain.repository.InstallmentTransactionRepository
import com.example.fixedexpeneses.ui.installment.toYearMonthOrNull
import com.example.fixedexpeneses.ui.recurring.create.toAmountInCentsOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateInstallmentTransactionViewModel(
    private val repository: InstallmentTransactionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateInstallmentTransactionUiState())
    val uiState: StateFlow<CreateInstallmentTransactionUiState> = _uiState.asStateFlow()

    fun onTypeChange(type: TransactionType) = _uiState.update { it.copy(type = type) }
    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }
    fun onAmountChange(amount: String) = _uiState.update { it.copy(amount = amount) }
    fun onAmountBehaviorChange(amountBehavior: AmountBehavior) =
        _uiState.update { it.copy(amountBehavior = amountBehavior) }
    fun onDueDayChange(dueDay: String) =
        _uiState.update { it.copy(dueDay = dueDay.filter { char -> char.isDigit() }.take(2)) }
    fun onPaymentMethodChange(paymentMethod: PaymentMethod?) =
        _uiState.update { it.copy(paymentMethod = paymentMethod) }
    fun onYearMonthFromChange(yearMonth: String) =
        _uiState.update { it.copy(yearMonthFrom = yearMonth.toMonthYearDigits()) }
    fun onYearMonthToChange(yearMonth: String) =
        _uiState.update { it.copy(yearMonthTo = yearMonth.toMonthYearDigits()) }

    fun save() {
        val state = _uiState.value
        val amountInCents = state.amount.toAmountInCentsOrNull()
        val dueDay = state.dueDay.toIntOrNull()
        val yearMonthFrom = state.yearMonthFrom.toYearMonthOrNull()
        val yearMonthTo = state.yearMonthTo.toYearMonthOrNull()

        if (!state.canSave || amountInCents == null || dueDay == null ||
            yearMonthFrom == null || yearMonthTo == null
        ) {
            _uiState.update { it.copy(errorMessage = "Preencha os campos obrigatórios corretamente.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                val timestamp = System.currentTimeMillis()
                repository.insert(
                    InstallmentTransaction(
                        type = state.type,
                        name = state.name.trim(),
                        amountInCents = amountInCents,
                        amountBehavior = state.amountBehavior,
                        dueDay = dueDay,
                        paymentMethod = state.paymentMethod,
                        yearMonthFrom = yearMonthFrom,
                        yearMonthTo = yearMonthTo,
                        createdAt = timestamp,
                        updatedAt = timestamp
                    )
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Não foi possível salvar a parcelada."
                    )
                }
            }
        }
    }
}

private fun String.toMonthYearDigits(): String =
    filter { it.isDigit() }.take(6)
