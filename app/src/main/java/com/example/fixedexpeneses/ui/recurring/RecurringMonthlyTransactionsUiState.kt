package com.example.fixedexpeneses.ui.recurring

import com.example.fixedexpeneses.domain.model.RecurringMonthlyTransaction
import com.example.fixedexpeneses.domain.model.TransactionType

data class RecurringMonthlyTransactionsUiState(
    val items: List<RecurringMonthlyTransaction> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val totalIncomeInCents: Long = 0,
    val totalExpenseInCents: Long = 0,
    val balanceInCents: Long = 0
) {
    companion object {
        fun fromItems(
            items: List<RecurringMonthlyTransaction>,
            isLoading: Boolean = false,
            errorMessage: String? = null
        ): RecurringMonthlyTransactionsUiState {
            val totalIncomeInCents = items
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amountInCents }
            val totalExpenseInCents = items
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amountInCents }

            return RecurringMonthlyTransactionsUiState(
                items = items,
                isLoading = isLoading,
                errorMessage = errorMessage,
                totalIncomeInCents = totalIncomeInCents,
                totalExpenseInCents = totalExpenseInCents,
                balanceInCents = totalIncomeInCents - totalExpenseInCents
            )
        }
    }
}
