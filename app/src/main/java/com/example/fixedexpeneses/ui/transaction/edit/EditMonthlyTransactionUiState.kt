package com.example.fixedexpeneses.ui.transaction.edit

import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.PaymentStatus
import com.example.fixedexpeneses.domain.model.Transaction
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.ui.recurring.create.toAmountInCentsOrNull

data class EditMonthlyTransactionUiState(
    val id: Long = 0,
    val recurringMonthlyTransactionId: Long? = null,
    val installmentTransactionId: Long? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val name: String = "",
    val amount: String = "",
    val amountBehavior: AmountBehavior? = null,
    val dueDay: String = "",
    val paymentMethod: PaymentMethod? = null,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val referenceMonth: Int = 1,
    val referenceYear: Int = 1970,
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

fun Transaction.toEditUiState(
    amountBehavior: AmountBehavior?
): EditMonthlyTransactionUiState =
    EditMonthlyTransactionUiState(
        id = id,
        recurringMonthlyTransactionId = recurringMonthlyTransactionId,
        installmentTransactionId = installmentTransactionId,
        type = type,
        name = name,
        amount = amountInCents.toDisplayAmount(),
        amountBehavior = amountBehavior,
        dueDay = dueDay.toString(),
        paymentMethod = paymentMethod,
        status = status,
        referenceMonth = referenceMonth,
        referenceYear = referenceYear,
        createdAt = createdAt,
        isLoading = false
    )

private fun Long.toDisplayAmount(): String {
    val absoluteValue = kotlin.math.abs(this)
    val signal = if (this < 0) "-" else ""
    val reais = absoluteValue / 100
    val cents = absoluteValue % 100
    return "$signal$reais,${cents.toString().padStart(2, '0')}"
}
