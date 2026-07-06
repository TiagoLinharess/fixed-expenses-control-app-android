package com.example.fixedexpeneses.ui.installment.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixedexpeneses.domain.repository.InstallmentTransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InstallmentTransactionDetailViewModel(
    private val repository: InstallmentTransactionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(InstallmentTransactionDetailUiState())
    val uiState: StateFlow<InstallmentTransactionDetailUiState> = _uiState.asStateFlow()

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getById(id) }
                .onSuccess { transaction ->
                    _uiState.update {
                        it.copy(
                            transaction = transaction,
                            isLoading = false,
                            errorMessage = if (transaction == null) "Parcelada não encontrada." else null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Não foi possível carregar a parcelada."
                        )
                    }
                }
        }
    }
}
