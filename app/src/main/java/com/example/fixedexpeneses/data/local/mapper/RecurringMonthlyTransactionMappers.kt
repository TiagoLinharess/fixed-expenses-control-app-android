package com.example.fixedexpeneses.data.local.mapper

import com.example.fixedexpeneses.data.local.entity.RecurringMonthlyTransactionEntity
import com.example.fixedexpeneses.domain.model.RecurringMonthlyTransaction

fun RecurringMonthlyTransactionEntity.toDomain(): RecurringMonthlyTransaction =
    RecurringMonthlyTransaction(
        id = id,
        type = type,
        name = name,
        amountInCents = amountInCents,
        amountBehavior = amountBehavior,
        dueDay = dueDay,
        paymentMethod = paymentMethod,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun RecurringMonthlyTransaction.toEntity(): RecurringMonthlyTransactionEntity =
    RecurringMonthlyTransactionEntity(
        id = id,
        type = type,
        name = name,
        amountInCents = amountInCents,
        amountBehavior = amountBehavior,
        dueDay = dueDay,
        paymentMethod = paymentMethod,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
