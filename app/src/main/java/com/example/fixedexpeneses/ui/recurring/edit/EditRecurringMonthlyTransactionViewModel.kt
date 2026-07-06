package com.example.fixedexpeneses.ui.recurring.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.RecurringMonthlyTransaction
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.domain.repository.RecurringMonthlyTransactionRepository
import com.example.fixedexpeneses.ui.recurring.create.toAmountInCentsOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditRecurringMonthlyTransactionViewModel(
    private val repository: RecurringMonthlyTransactionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditRecurringMonthlyTransactionUiState())
    val uiState: StateFlow<EditRecurringMonthlyTransactionUiState> = _uiState.asStateFlow()

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                repository.getById(id)
            }.onSuccess { transaction ->
                _uiState.value = transaction?.toEditUiState()
                    ?: EditRecurringMonthlyTransactionUiState(
                        isLoading = false,
                        errorMessage = "Conta fixa nao encontrada."
                    )
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Nao foi possivel carregar a conta fixa."
                    )
                }
            }
        }
    }

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
                repository.update(
                    RecurringMonthlyTransaction(
                        id = state.id,
                        type = state.type,
                        name = state.name.trim(),
                        amountInCents = amountInCents,
                        amountBehavior = state.amountBehavior,
                        dueDay = dueDay,
                        paymentMethod = state.paymentMethod,
                        createdAt = state.createdAt,
                        updatedAt = System.currentTimeMillis()
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
}
