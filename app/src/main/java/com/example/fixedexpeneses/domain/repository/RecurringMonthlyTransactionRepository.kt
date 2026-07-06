package com.example.fixedexpeneses.domain.repository

import com.example.fixedexpeneses.domain.model.RecurringMonthlyTransaction
import kotlinx.coroutines.flow.Flow

interface RecurringMonthlyTransactionRepository {
    fun observeAll(): Flow<List<RecurringMonthlyTransaction>>
    suspend fun getAll(): List<RecurringMonthlyTransaction>
    suspend fun getById(id: Long): RecurringMonthlyTransaction?
    suspend fun insert(transaction: RecurringMonthlyTransaction): Long
    suspend fun update(transaction: RecurringMonthlyTransaction)
    suspend fun deleteById(id: Long)
}
