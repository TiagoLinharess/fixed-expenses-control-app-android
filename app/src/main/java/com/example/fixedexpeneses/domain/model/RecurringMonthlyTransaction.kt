package com.example.fixedexpeneses.domain.model

data class RecurringMonthlyTransaction(
    val id: Long = 0,
    val type: TransactionType,
    val name: String,
    val amountInCents: Long,
    val amountBehavior: AmountBehavior,
    val dueDay: Int,
    val paymentMethod: PaymentMethod?,
    val createdAt: Long,
    val updatedAt: Long
)
