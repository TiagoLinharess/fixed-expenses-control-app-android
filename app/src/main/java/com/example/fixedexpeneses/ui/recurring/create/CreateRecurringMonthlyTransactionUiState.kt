package com.example.fixedexpeneses.ui.recurring.create

import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.TransactionType

data class CreateRecurringMonthlyTransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val name: String = "",
    val amount: String = "",
    val amountBehavior: AmountBehavior = AmountBehavior.FIXED,
    val dueDay: String = "",
    val paymentMethod: PaymentMethod? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val canSave: Boolean
        get() = name.isNotBlank() &&
            amount.toAmountInCentsOrNull() != null &&
            (dueDay.toIntOrNull() ?: 0) in 1..31 &&
            !isSaving
}

fun String.toAmountInCentsOrNull(): Long? {
    val normalized = trim()
        .replace("R$", "")
        .replace(".", "")
        .replace(",", ".")
        .trim()

    if (normalized.isBlank()) {
        return null
    }

    val value = normalized.toBigDecimalOrNull() ?: return null
    val cents = value.movePointRight(2).toLong()
    return cents.takeIf { it > 0 }
}
