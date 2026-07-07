package com.example.fixedexpeneses.ui.home

import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.PaymentStatus
import com.example.fixedexpeneses.domain.model.TransactionType

data class HomeUiState(
    val selectedYearMonth: Int = 0,
    val currentYearMonth: Int = 0,
    val firstAvailableYearMonth: Int = 0,
    val availableMonths: List<MonthSelectorOption> = emptyList(),
    val transactions: List<HomeTransactionItem> = emptyList(),
    val totalIncomeInCents: Long = 0,
    val totalExpenseInCents: Long = 0,
    val balanceInCents: Long = 0,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedMonthLabel: String
        get() = selectedYearMonth.toMonthYearLabel()
}

data class MonthSelectorOption(
    val yearMonth: Int,
    val label: String
)

data class HomeTransactionItem(
    val id: Long,
    val type: TransactionType,
    val name: String,
    val amountInCents: Long,
    val dueDay: Int,
    val paymentMethod: PaymentMethod?,
    val status: PaymentStatus,
    val sourceDescription: String
)
