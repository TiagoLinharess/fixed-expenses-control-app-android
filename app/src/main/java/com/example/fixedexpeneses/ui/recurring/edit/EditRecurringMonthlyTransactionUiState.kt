package com.example.fixedexpeneses.ui.recurring.edit

import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.RecurringMonthlyTransaction
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.ui.recurring.create.toAmountInCentsOrNull

data class EditRecurringMonthlyTransactionUiState(
    val id: Long = 0,
    val type: TransactionType = TransactionType.EXPENSE,
    val name: String = "",
    val amount: String = "",
    val amountBehavior: AmountBehavior = AmountBehavior.FIXED,
    val dueDay: String = "",
    val paymentMethod: PaymentMethod? = null,
    val createdAt: Long = 0,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val canSave: Boolean
        get() = name.isNotBlank() &&
            amount.toAmountInCentsOrNull() != null &&
            (dueDay.toIntOrNull() ?: 0) in 1..31 &&
            !isLoading &&
            !isSaving
}

fun RecurringMonthlyTransaction.toEditUiState(): EditRecurringMonthlyTransactionUiState =
    EditRecurringMonthlyTransactionUiState(
        id = id,
        type = type,
        name = name,
        amount = amountInCents.toDisplayAmount(),
        amountBehavior = amountBehavior,
        dueDay = dueDay.toString(),
        paymentMethod = paymentMethod,
        createdAt = createdAt,
        isLoading = false
    )

private fun Long.toDisplayAmount(): String {
    val reais = this / 100
    val cents = kotlin.math.abs(this % 100)
    return "$reais,${cents.toString().padStart(2, '0')}"
}
