package com.example.fixedexpeneses.domain.model

data class InstallmentTransaction(
    val id: Long = 0,
    val type: TransactionType,
    val name: String,
    val amountInCents: Long,
    val amountBehavior: AmountBehavior,
    val dueDay: Int,
    val paymentMethod: PaymentMethod?,
    val yearMonthFrom: Int,
    val yearMonthTo: Int,
    val createdAt: Long,
    val updatedAt: Long
)
