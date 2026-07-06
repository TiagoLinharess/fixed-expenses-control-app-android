package com.example.fixedexpeneses.data.repository

import com.example.fixedexpeneses.data.local.dao.TransactionDao
import com.example.fixedexpeneses.data.local.mapper.toDomain
import com.example.fixedexpeneses.data.local.mapper.toEntity
import com.example.fixedexpeneses.domain.model.Transaction
import com.example.fixedexpeneses.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalTransactionRepository(
    private val dao: TransactionDao
) : TransactionRepository {
    override fun observeAll(): Flow<List<Transaction>> =
        dao.observeAll().map { transactions -> transactions.map { it.toDomain() } }

    override fun observeByMonth(referenceMonth: Int, referenceYear: Int): Flow<List<Transaction>> =
        dao.observeByMonth(referenceMonth, referenceYear)
            .map { transactions -> transactions.map { it.toDomain() } }

    override suspend fun getByMonth(referenceMonth: Int, referenceYear: Int): List<Transaction> =
        dao.getByMonth(referenceMonth, referenceYear).map { it.toDomain() }

    override suspend fun getById(id: Long): Transaction? =
        dao.getById(id)?.toDomain()

    override suspend fun existsForRecurringMonthlyTransaction(
        recurringMonthlyTransactionId: Long,
        referenceMonth: Int,
        referenceYear: Int
    ): Boolean =
        dao.existsForRecurringMonthlyTransaction(
            recurringMonthlyTransactionId = recurringMonthlyTransactionId,
            referenceMonth = referenceMonth,
            referenceYear = referenceYear
        )

    override suspend fun existsForInstallmentTransaction(
        installmentTransactionId: Long,
        referenceMonth: Int,
        referenceYear: Int
    ): Boolean =
        dao.existsForInstallmentTransaction(
            installmentTransactionId = installmentTransactionId,
            referenceMonth = referenceMonth,
            referenceYear = referenceYear
        )

    override suspend fun insert(transaction: Transaction): Long =
        dao.insert(transaction.toEntity())

    override suspend fun insertAll(transactions: List<Transaction>): List<Long> =
        dao.insertAll(transactions.map { it.toEntity() })

    override suspend fun update(transaction: Transaction) {
        dao.update(transaction.toEntity())
    }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}
