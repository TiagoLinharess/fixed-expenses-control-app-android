package com.example.fixedexpeneses.ui.installment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixedexpeneses.domain.repository.InstallmentTransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class InstallmentTransactionsViewModel(
    private val repository: InstallmentTransactionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(InstallmentTransactionsUiState())
    val uiState: StateFlow<InstallmentTransactionsUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            runCatching {
                repository.deleteById(id)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Não foi possível excluir a parcelada.")
                }
            }
        }
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            repository.observeAll()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Não foi possível carregar as parceladas."
                        )
                    }
                }
                .collect { transactions ->
                    _uiState.value = InstallmentTransactionsUiState.fromItems(
                        items = transactions,
                        currentYearMonth = currentYearMonth()
                    )
                }
        }
    }

    private fun currentYearMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) * 100 + calendar.get(Calendar.MONTH) + 1
    }
}
