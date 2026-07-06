package com.example.fixedexpeneses.ui.installment

import com.example.fixedexpeneses.domain.model.InstallmentTransaction
import com.example.fixedexpeneses.domain.model.TransactionType

data class InstallmentTransactionsUiState(
    val items: List<InstallmentTransaction> = emptyList(),
    val currentYearMonth: Int = 0,
    val activeItemsCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val totalIncomeInCents: Long = 0,
    val totalExpenseInCents: Long = 0,
    val balanceInCents: Long = 0
) {
    companion object {
        fun fromItems(
            items: List<InstallmentTransaction>,
            currentYearMonth: Int,
            isLoading: Boolean = false,
            errorMessage: String? = null
        ): InstallmentTransactionsUiState {
            val activeItems = items.filter { it.isActiveIn(currentYearMonth) }
            val totalIncomeInCents = activeItems
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amountInCents }
            val totalExpenseInCents = activeItems
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amountInCents }

            return InstallmentTransactionsUiState(
                items = items,
                currentYearMonth = currentYearMonth,
                activeItemsCount = activeItems.size,
                isLoading = isLoading,
                errorMessage = errorMessage,
                totalIncomeInCents = totalIncomeInCents,
                totalExpenseInCents = totalExpenseInCents,
                balanceInCents = totalIncomeInCents - totalExpenseInCents
            )
        }
    }
}
