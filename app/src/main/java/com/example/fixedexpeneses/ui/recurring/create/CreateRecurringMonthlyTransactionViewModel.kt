package com.example.fixedexpeneses.ui.recurring.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.RecurringMonthlyTransaction
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.domain.repository.RecurringMonthlyTransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateRecurringMonthlyTransactionViewModel(
    private val repository: RecurringMonthlyTransactionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateRecurringMonthlyTransactionUiState())
    val uiState: StateFlow<CreateRecurringMonthlyTransactionUiState> = _uiState.asStateFlow()

    fun onTypeChange(type: TransactionType) {
        _uiState.update { it.copy(type = type) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onAmountBehaviorChange(amountBehavior: AmountBehavior) {
        _uiState.update { it.copy(amountBehavior = amountBehavior) }
    }

    fun onDueDayChange(dueDay: String) {
        val filtered = dueDay.filter { it.isDigit() }.take(2)
        _uiState.update { it.copy(dueDay = filtered) }
    }

    fun onPaymentMethodChange(paymentMethod: PaymentMethod?) {
        _uiState.update { it.copy(paymentMethod = paymentMethod) }
    }

    fun save() {
        val state = _uiState.value
        val amountInCents = state.amount.toAmountInCentsOrNull()
        val dueDay = state.dueDay.toIntOrNull()

        if (!state.canSave || amountInCents == null || dueDay == null) {
            _uiState.update {
                it.copy(errorMessage = "Preencha os campos obrigatorios corretamente.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            runCatching {
                val timestamp = System.currentTimeMillis()
                repository.insert(
                    RecurringMonthlyTransaction(
                        type = state.type,
                        name = state.name.trim(),
                        amountInCents = amountInCents,
                        amountBehavior = state.amountBehavior,
                        dueDay = dueDay,
                        paymentMethod = state.paymentMethod,
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
                        errorMessage = error.message ?: "Nao foi possivel salvar a conta fixa."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
