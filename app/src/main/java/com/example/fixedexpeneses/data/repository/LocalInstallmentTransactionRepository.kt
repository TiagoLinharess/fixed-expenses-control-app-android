package com.example.fixedexpeneses.data.repository

import com.example.fixedexpeneses.data.local.dao.InstallmentTransactionDao
import com.example.fixedexpeneses.data.local.mapper.toDomain
import com.example.fixedexpeneses.data.local.mapper.toEntity
import com.example.fixedexpeneses.domain.model.InstallmentTransaction
import com.example.fixedexpeneses.domain.repository.InstallmentTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalInstallmentTransactionRepository(
    private val dao: InstallmentTransactionDao
) : InstallmentTransactionRepository {
    override fun observeAll(): Flow<List<InstallmentTransaction>> =
        dao.observeAll().map { transactions -> transactions.map { it.toDomain() } }

    override fun observeByYearMonth(yearMonth: Int): Flow<List<InstallmentTransaction>> =
        dao.observeByYearMonth(yearMonth).map { transactions -> transactions.map { it.toDomain() } }

    override suspend fun getByYearMonth(yearMonth: Int): List<InstallmentTransaction> =
        dao.getByYearMonth(yearMonth).map { it.toDomain() }

    override suspend fun getById(id: Long): InstallmentTransaction? =
        dao.getById(id)?.toDomain()

    override suspend fun insert(transaction: InstallmentTransaction): Long =
        dao.insert(transaction.toEntity())

    override suspend fun update(transaction: InstallmentTransaction) {
        dao.update(transaction.toEntity())
    }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}
