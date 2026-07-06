package com.example.fixedexpeneses.ui.installment.edit

import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.InstallmentTransaction
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.ui.installment.create.isValidYearMonth
import com.example.fixedexpeneses.ui.installment.toMonthYearDigits
import com.example.fixedexpeneses.ui.installment.toYearMonthOrNull
import com.example.fixedexpeneses.ui.recurring.create.toAmountInCentsOrNull

data class EditInstallmentTransactionUiState(
    val id: Long = 0,
    val type: TransactionType = TransactionType.EXPENSE,
    val name: String = "",
    val amount: String = "",
    val amountBehavior: AmountBehavior = AmountBehavior.FIXED,
    val dueDay: String = "",
    val paymentMethod: PaymentMethod? = null,
    val yearMonthFrom: String = "",
    val yearMonthTo: String = "",
    val createdAt: Long = 0,
    val isLoading: Boolean = true,
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
                !isLoading &&
                !isSaving
        }
}

fun InstallmentTransaction.toEditUiState(): EditInstallmentTransactionUiState =
    EditInstallmentTransactionUiState(
        id = id,
        type = type,
        name = name,
        amount = amountInCents.toDisplayAmount(),
        amountBehavior = amountBehavior,
        dueDay = dueDay.toString(),
        paymentMethod = paymentMethod,
        yearMonthFrom = yearMonthFrom.toMonthYearDigits(),
        yearMonthTo = yearMonthTo.toMonthYearDigits(),
        createdAt = createdAt,
        isLoading = false
    )

private fun Long.toDisplayAmount(): String {
    val reais = this / 100
    val cents = kotlin.math.abs(this % 100)
    return "$reais,${cents.toString().padStart(2, '0')}"
}
