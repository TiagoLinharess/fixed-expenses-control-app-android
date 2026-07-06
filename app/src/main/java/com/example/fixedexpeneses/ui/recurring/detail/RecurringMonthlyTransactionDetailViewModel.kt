package com.example.fixedexpeneses.ui.recurring.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixedexpeneses.domain.repository.RecurringMonthlyTransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecurringMonthlyTransactionDetailViewModel(
    private val repository: RecurringMonthlyTransactionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecurringMonthlyTransactionDetailUiState())
    val uiState: StateFlow<RecurringMonthlyTransactionDetailUiState> = _uiState.asStateFlow()

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                repository.getById(id)
            }.onSuccess { transaction ->
                _uiState.update {
                    it.copy(
                        transaction = transaction,
                        isLoading = false,
                        errorMessage = if (transaction == null) {
                            "Conta fixa não encontrada."
                        } else {
                            null
                        }
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Não foi possível carregar a conta fixa."
                    )
                }
            }
        }
    }
}
