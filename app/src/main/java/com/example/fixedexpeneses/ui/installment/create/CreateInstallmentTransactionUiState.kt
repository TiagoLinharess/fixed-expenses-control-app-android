package com.example.fixedexpeneses.ui.installment.create

import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.ui.installment.toYearMonthOrNull
import com.example.fixedexpeneses.ui.recurring.create.toAmountInCentsOrNull

data class CreateInstallmentTransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val name: String = "",
    val amount: String = "",
    val amountBehavior: AmountBehavior = AmountBehavior.FIXED,
    val dueDay: String = "",
    val paymentMethod: PaymentMethod? = null,
    val yearMonthFrom: String = "",
    val yearMonthTo: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val canSave: Boolean
        get() {
            val from = yearMonthFrom.toYearMonthOrNull()
            val to = yearMonthTo.toYearMonthOrNull()
            return name.isNotBlank() &&
                amount.toAmountInCentsOrNull() != null &&
                (dueDay.toIntOrNull() ?: 0) in 1..31 &&
                from.isValidYearMonth() &&
                to.isValidYearMonth() &&
                from != null &&
                to != null &&
                from <= to &&
                !isSaving
        }
}

fun Int?.isValidYearMonth(): Boolean {
    if (this == null) return false
    val month = this % 100
    val year = this / 100
    return year > 0 && month in 1..12
}
