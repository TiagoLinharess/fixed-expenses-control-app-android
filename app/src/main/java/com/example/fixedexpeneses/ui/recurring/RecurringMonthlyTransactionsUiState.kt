package com.example.fixedexpeneses.ui.recurring

import com.example.fixedexpeneses.domain.model.RecurringMonthlyTransaction

data class RecurringMonthlyTransactionsUiState(
    val items: List<RecurringMonthlyTransaction> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
