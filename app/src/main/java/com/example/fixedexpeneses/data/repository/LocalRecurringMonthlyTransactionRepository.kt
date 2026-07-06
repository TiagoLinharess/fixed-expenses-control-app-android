package com.example.fixedexpeneses.data.repository

import com.example.fixedexpeneses.data.local.dao.RecurringMonthlyTransactionDao
import com.example.fixedexpeneses.data.local.mapper.toDomain
import com.example.fixedexpeneses.data.local.mapper.toEntity
import com.example.fixedexpeneses.domain.model.RecurringMonthlyTransaction
import com.example.fixedexpeneses.domain.repository.RecurringMonthlyTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalRecurringMonthlyTransactionRepository(
    private val dao: RecurringMonthlyTransactionDao
) : RecurringMonthlyTransactionRepository {
    override fun observeAll(): Flow<List<RecurringMonthlyTransaction>> =
        dao.observeAll().map { transactions -> transactions.map { it.toDomain() } }

    override suspend fun getAll(): List<RecurringMonthlyTransaction> =
        dao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): RecurringMonthlyTransaction? =
        dao.getById(id)?.toDomain()

    override suspend fun insert(transaction: RecurringMonthlyTransaction): Long =
        dao.insert(transaction.toEntity())

    override suspend fun update(transaction: RecurringMonthlyTransaction) {
        dao.update(transaction.toEntity())
    }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}
