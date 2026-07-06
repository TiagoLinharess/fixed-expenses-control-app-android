package com.example.fixedexpeneses.domain.repository

import com.example.fixedexpeneses.domain.model.InstallmentTransaction
import kotlinx.coroutines.flow.Flow

interface InstallmentTransactionRepository {
    fun observeAll(): Flow<List<InstallmentTransaction>>
    fun observeByYearMonth(yearMonth: Int): Flow<List<InstallmentTransaction>>
    suspend fun getByYearMonth(yearMonth: Int): List<InstallmentTransaction>
    suspend fun getById(id: Long): InstallmentTransaction?
    suspend fun insert(transaction: InstallmentTransaction): Long
    suspend fun update(transaction: InstallmentTransaction)
    suspend fun deleteById(id: Long)
}
