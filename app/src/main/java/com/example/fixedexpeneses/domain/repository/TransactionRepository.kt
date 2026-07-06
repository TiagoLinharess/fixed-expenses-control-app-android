package com.example.fixedexpeneses.domain.repository

import com.example.fixedexpeneses.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    fun observeByMonth(referenceMonth: Int, referenceYear: Int): Flow<List<Transaction>>
    suspend fun getByMonth(referenceMonth: Int, referenceYear: Int): List<Transaction>
    suspend fun getById(id: Long): Transaction?
    suspend fun existsForRecurringMonthlyTransaction(
        recurringMonthlyTransactionId: Long,
        referenceMonth: Int,
        referenceYear: Int
    ): Boolean
    suspend fun existsForInstallmentTransaction(
        installmentTransactionId: Long,
        referenceMonth: Int,
        referenceYear: Int
    ): Boolean
    suspend fun insert(transaction: Transaction): Long
    suspend fun insertAll(transactions: List<Transaction>): List<Long>
    suspend fun update(transaction: Transaction)
    suspend fun deleteById(id: Long)
}
