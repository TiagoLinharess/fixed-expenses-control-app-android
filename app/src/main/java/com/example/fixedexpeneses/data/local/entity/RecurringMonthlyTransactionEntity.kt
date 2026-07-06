package com.example.fixedexpeneses.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.TransactionType

@Entity(tableName = "recurring_monthly_transactions")
data class RecurringMonthlyTransactionEntity(
    @PrimaryKey(autoGenerate = true)
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
