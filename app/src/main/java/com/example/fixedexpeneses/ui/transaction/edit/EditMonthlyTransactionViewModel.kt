package com.example.fixedexpeneses.ui.transaction.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.PaymentStatus
import com.example.fixedexpeneses.domain.model.Transaction
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.domain.repository.InstallmentTransactionRepository
import com.example.fixedexpeneses.domain.repository.RecurringMonthlyTransactionRepository
import com.example.fixedexpeneses.domain.repository.TransactionRepository
import com.example.fixedexpeneses.ui.recurring.create.toAmountInCentsOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditMonthlyTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val recurringMonthlyTransactionRepository: RecurringMonthlyTransactionRepository,
    private val installmentTransactionRepository: InstallmentTransactionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditMonthlyTransactionUiState())
    val uiState: StateFlow<EditMonthlyTransactionUiState> = _uiState.asStateFlow()

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                transactionRepository.getById(id)
            }.onSuccess { transaction ->
                if (transaction == null) {
                    _uiState.value = EditMonthlyTransactionUiState(
                        isLoading = false,
                        errorMessage = "Transação não encontrada."
                    )
                    return@onSuccess
                }

                _uiState.value = transaction.toEditUiState(
                    amountBehavior = transaction.findAmountBehavior()
                )
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Não foi possível carregar a transação."
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

    fun onDueDayChange(dueDay: String) {
        val filtered = dueDay.filter { it.isDigit() }.take(2)
        _uiState.update { it.copy(dueDay = filtered) }
    }

    fun onPaymentMethodChange(paymentMethod: PaymentMethod?) {
        _uiState.update { it.copy(paymentMethod = paymentMethod) }
    }

    fun onStatusChange(status: PaymentStatus) {
        _uiState.update { it.copy(status = status) }
    }

    fun save() {
        val state = _uiState.value
        val amountInCents = state.amount.toAmountInCentsOrNull()
        val dueDay = state.dueDay.toIntOrNull()

        if (!state.canSave || amountInCents == null || dueDay == null) {
            _uiState.update {
                it.copy(errorMessage = "Preencha os campos obrigatórios corretamente.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            runCatching {
                transactionRepository.update(
                    Transaction(
                        id = state.id,
                        recurringMonthlyTransactionId = state.recurringMonthlyTransactionId,
                        installmentTransactionId = state.installmentTransactionId,
                        type = state.type,
                        name = state.name.trim(),
                        amountInCents = amountInCents,
                        dueDay = dueDay,
                        paymentMethod = state.paymentMethod,
                        status = state.status,
                        referenceMonth = state.referenceMonth,
                        referenceYear = state.referenceYear,
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
                        errorMessage = error.message ?: "Não foi possível salvar a transação."
                    )
                }
            }
        }
    }

    private suspend fun Transaction.findAmountBehavior(): AmountBehavior? =
        recurringMonthlyTransactionId?.let { id ->
            recurringMonthlyTransactionRepository.getById(id)?.amountBehavior
        } ?: installmentTransactionId?.let { id ->
            installmentTransactionRepository.getById(id)?.amountBehavior
        }
}
