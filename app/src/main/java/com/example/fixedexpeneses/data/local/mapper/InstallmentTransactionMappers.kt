package com.example.fixedexpeneses.data.local.mapper

import com.example.fixedexpeneses.data.local.entity.InstallmentTransactionEntity
import com.example.fixedexpeneses.domain.model.InstallmentTransaction

fun InstallmentTransactionEntity.toDomain(): InstallmentTransaction =
    InstallmentTransaction(
        id = id,
        type = type,
        name = name,
        amountInCents = amountInCents,
        amountBehavior = amountBehavior,
        dueDay = dueDay,
        paymentMethod = paymentMethod,
        yearMonthFrom = yearMonthFrom,
        yearMonthTo = yearMonthTo,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun InstallmentTransaction.toEntity(): InstallmentTransactionEntity =
    InstallmentTransactionEntity(
        id = id,
        type = type,
        name = name,
        amountInCents = amountInCents,
        amountBehavior = amountBehavior,
        dueDay = dueDay,
        paymentMethod = paymentMethod,
        yearMonthFrom = yearMonthFrom,
        yearMonthTo = yearMonthTo,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
