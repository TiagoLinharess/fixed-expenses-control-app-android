package com.example.fixedexpeneses.data.local.mapper

import com.example.fixedexpeneses.data.local.entity.TransactionEntity
import com.example.fixedexpeneses.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction =
    Transaction(
        id = id,
        recurringMonthlyTransactionId = recurringMonthlyTransactionId,
        installmentTransactionId = installmentTransactionId,
        type = type,
        name = name,
        amountInCents = amountInCents,
        dueDay = dueDay,
        paymentMethod = paymentMethod,
        status = status,
        referenceMonth = referenceMonth,
        referenceYear = referenceYear,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun Transaction.toEntity(): TransactionEntity =
    TransactionEntity(
        id = id,
        recurringMonthlyTransactionId = recurringMonthlyTransactionId,
        installmentTransactionId = installmentTransactionId,
        type = type,
        name = name,
        amountInCents = amountInCents,
        dueDay = dueDay,
        paymentMethod = paymentMethod,
        status = status,
        referenceMonth = referenceMonth,
        referenceYear = referenceYear,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
