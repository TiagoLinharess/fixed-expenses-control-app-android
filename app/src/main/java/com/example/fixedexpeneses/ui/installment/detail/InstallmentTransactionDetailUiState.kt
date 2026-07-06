package com.example.fixedexpeneses.ui.installment.detail

import com.example.fixedexpeneses.domain.model.InstallmentTransaction

data class InstallmentTransactionDetailUiState(
    val transaction: InstallmentTransaction? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
