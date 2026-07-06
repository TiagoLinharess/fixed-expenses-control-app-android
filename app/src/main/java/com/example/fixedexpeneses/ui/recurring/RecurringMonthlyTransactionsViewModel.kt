package com.example.fixedexpeneses.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixedexpeneses.domain.repository.RecurringMonthlyTransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecurringMonthlyTransactionsViewModel(
    private val repository: RecurringMonthlyTransactionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecurringMonthlyTransactionsUiState())
    val uiState: StateFlow<RecurringMonthlyTransactionsUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            runCatching {
                repository.deleteById(id)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Nao foi possivel excluir a conta fixa.")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            repository.observeAll()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Nao foi possivel carregar as contas fixas."
                        )
                    }
                }
                .collect { transactions ->
                    _uiState.value = RecurringMonthlyTransactionsUiState.fromItems(
                        items = transactions
                    )
                }
        }
    }
}
