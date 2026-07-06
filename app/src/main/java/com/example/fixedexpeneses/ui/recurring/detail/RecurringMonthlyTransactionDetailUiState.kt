package com.example.fixedexpeneses.ui.recurring.detail

import com.example.fixedexpeneses.domain.model.RecurringMonthlyTransaction

data class RecurringMonthlyTransactionDetailUiState(
    val transaction: RecurringMonthlyTransaction? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
