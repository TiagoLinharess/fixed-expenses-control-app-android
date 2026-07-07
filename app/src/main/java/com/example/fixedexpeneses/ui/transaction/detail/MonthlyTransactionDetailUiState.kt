package com.example.fixedexpeneses.ui.transaction.detail

import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.Transaction
import com.example.fixedexpeneses.ui.recurring.create.toAmountInCentsOrNull

data class MonthlyTransactionDetailUiState(
    val isLoading: Boolean = true,
    val transaction: Transaction? = null,
    val amountBehavior: AmountBehavior? = null,
    val confirmedAmount: String = "",
    val isSavingConfirmedAmount: Boolean = false,
    val errorMessage: String? = null
) {
    val canConfirmAmount: Boolean
        get() = amountBehavior == AmountBehavior.VARIABLE &&
            confirmedAmount.toAmountInCentsOrNull() != null &&
            !isLoading &&
            !isSavingConfirmedAmount
}
