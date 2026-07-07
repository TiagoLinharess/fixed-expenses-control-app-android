package com.example.fixedexpeneses.ui.transaction.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.Transaction
import com.example.fixedexpeneses.domain.repository.InstallmentTransactionRepository
import com.example.fixedexpeneses.domain.repository.RecurringMonthlyTransactionRepository
import com.example.fixedexpeneses.domain.repository.TransactionRepository
import com.example.fixedexpeneses.ui.recurring.create.toAmountInCentsOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MonthlyTransactionDetailViewModel(
    private val transactionRepository: TransactionRepository,
    private val recurringMonthlyTransactionRepository: RecurringMonthlyTransactionRepository,
    private val installmentTransactionRepository: InstallmentTransactionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MonthlyTransactionDetailUiState())
    val uiState: StateFlow<MonthlyTransactionDetailUiState> = _uiState.asStateFlow()

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                transactionRepository.getById(id)
            }.onSuccess { transaction ->
                if (transaction == null) {
                    _uiState.value = MonthlyTransactionDetailUiState(
                        isLoading = false,
                        errorMessage = "Transação não encontrada."
                    )
                    return@onSuccess
                }

                _uiState.value = MonthlyTransactionDetailUiState(
                    isLoading = false,
                    transaction = transaction,
                    amountBehavior = transaction.findAmountBehavior(),
                    confirmedAmount = transaction.amountInCents.toDisplayAmount()
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

    fun onConfirmedAmountChange(amount: String) {
        _uiState.update { it.copy(confirmedAmount = amount) }
    }

    fun confirmAmount() {
        val state = _uiState.value
        val transaction = state.transaction
        val amountInCents = state.confirmedAmount.toAmountInCentsOrNull()

        if (transaction == null || amountInCents == null || state.amountBehavior != AmountBehavior.VARIABLE) {
            _uiState.update {
                it.copy(errorMessage = "Informe um valor válido para confirmar.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingConfirmedAmount = true, errorMessage = null) }

            runCatching {
                transaction.copy(
                    amountInCents = amountInCents,
                    updatedAt = System.currentTimeMillis()
                ).also { updatedTransaction ->
                    transactionRepository.update(updatedTransaction)
                }
            }.onSuccess { updatedTransaction ->
                _uiState.update {
                    it.copy(
                        transaction = updatedTransaction,
                        confirmedAmount = updatedTransaction.amountInCents.toDisplayAmount(),
                        isSavingConfirmedAmount = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSavingConfirmedAmount = false,
                        errorMessage = error.message ?: "Não foi possível confirmar o valor."
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

    private fun Long.toDisplayAmount(): String {
        val absoluteValue = kotlin.math.abs(this)
        val signal = if (this < 0) "-" else ""
        val reais = absoluteValue / 100
        val cents = absoluteValue % 100
        return "$signal$reais,${cents.toString().padStart(2, '0')}"
    }
}
