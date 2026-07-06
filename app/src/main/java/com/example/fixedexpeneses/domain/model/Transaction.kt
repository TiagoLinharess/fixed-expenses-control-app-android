package com.example.fixedexpeneses.domain.model

data class Transaction(
    val id: Long = 0,
    val recurringMonthlyTransactionId: Long?,
    val installmentTransactionId: Long?,
    val type: TransactionType,
    val name: String,
    val amountInCents: Long,
    val dueDay: Int,
    val paymentMethod: PaymentMethod?,
    val status: PaymentStatus,
    val referenceMonth: Int,
    val referenceYear: Int,
    val createdAt: Long,
    val updatedAt: Long
)
