package com.example.fixedexpeneses.domain.usecase

import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.InstallmentTransaction
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.PaymentStatus
import com.example.fixedexpeneses.domain.model.RecurringMonthlyTransaction
import com.example.fixedexpeneses.domain.model.Transaction
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.domain.repository.InstallmentTransactionRepository
import com.example.fixedexpeneses.domain.repository.RecurringMonthlyTransactionRepository
import com.example.fixedexpeneses.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateMonthlyTransactionsUseCaseTest {
    @Test
    fun `creates monthly transactions from recurring and installment templates`() = runBlocking {
        val transactionRepository = FakeTransactionRepository()
        val useCase = GenerateMonthlyTransactionsUseCase(
            recurringMonthlyTransactionRepository = FakeRecurringMonthlyTransactionRepository(
                transactions = listOf(sampleRecurringTransaction())
            ),
            installmentTransactionRepository = FakeInstallmentTransactionRepository(
                transactions = listOf(sampleInstallmentTransaction())
            ),
            transactionRepository = transactionRepository,
            now = { 1000L }
        )

        val insertedIds = useCase(referenceMonth = 7, referenceYear = 2026)

        assertEquals(listOf(1L, 2L), insertedIds)
        assertEquals(2, transactionRepository.transactions.size)
        assertEquals("Internet", transactionRepository.transactions[0].name)
        assertEquals("Notebook", transactionRepository.transactions[1].name)
        assertTrue(transactionRepository.transactions.all { it.status == PaymentStatus.PENDING })
    }

    @Test
    fun `does not duplicate transactions already generated for the same month`() = runBlocking {
        val recurring = sampleRecurringTransaction()
        val installment = sampleInstallmentTransaction()
        val transactionRepository = FakeTransactionRepository(
            transactions = mutableListOf(
                recurring.toExistingTransaction(referenceMonth = 7, referenceYear = 2026),
                installment.toExistingTransaction(referenceMonth = 7, referenceYear = 2026)
            )
        )
        val useCase = GenerateMonthlyTransactionsUseCase(
            recurringMonthlyTransactionRepository = FakeRecurringMonthlyTransactionRepository(listOf(recurring)),
            installmentTransactionRepository = FakeInstallmentTransactionRepository(listOf(installment)),
            transactionRepository = transactionRepository
        )

        val insertedIds = useCase(referenceMonth = 7, referenceYear = 2026)

        assertTrue(insertedIds.isEmpty())
        assertEquals(2, transactionRepository.transactions.size)
    }

    private fun sampleRecurringTransaction(): RecurringMonthlyTransaction =
        RecurringMonthlyTransaction(
            id = 10,
            type = TransactionType.EXPENSE,
            name = "Internet",
            amountInCents = 12000,
            amountBehavior = AmountBehavior.FIXED,
            dueDay = 10,
            paymentMethod = PaymentMethod.CREDIT,
            createdAt = 1,
            updatedAt = 1
        )

    private fun sampleInstallmentTransaction(): InstallmentTransaction =
        InstallmentTransaction(
            id = 20,
            type = TransactionType.EXPENSE,
            name = "Notebook",
            amountInCents = 35000,
            amountBehavior = AmountBehavior.FIXED,
            dueDay = 15,
            paymentMethod = PaymentMethod.CREDIT,
            yearMonthFrom = 202607,
            yearMonthTo = 202612,
            createdAt = 1,
            updatedAt = 1
        )

    private fun RecurringMonthlyTransaction.toExistingTransaction(
        referenceMonth: Int,
        referenceYear: Int
    ): Transaction =
        Transaction(
            id = 1,
            recurringMonthlyTransactionId = id,
            installmentTransactionId = null,
            type = type,
            name = name,
            amountInCents = amountInCents,
            dueDay = dueDay,
            paymentMethod = paymentMethod,
            status = PaymentStatus.PENDING,
            referenceMonth = referenceMonth,
            referenceYear = referenceYear,
            createdAt = 1,
            updatedAt = 1
        )

    private fun InstallmentTransaction.toExistingTransaction(
        referenceMonth: Int,
        referenceYear: Int
    ): Transaction =
        Transaction(
            id = 2,
            recurringMonthlyTransactionId = null,
            installmentTransactionId = id,
            type = type,
            name = name,
            amountInCents = amountInCents,
            dueDay = dueDay,
            paymentMethod = paymentMethod,
            status = PaymentStatus.PENDING,
            referenceMonth = referenceMonth,
            referenceYear = referenceYear,
            createdAt = 1,
            updatedAt = 1
        )

    private class FakeRecurringMonthlyTransactionRepository(
        private val transactions: List<RecurringMonthlyTransaction>
    ) : RecurringMonthlyTransactionRepository {
        override fun observeAll(): Flow<List<RecurringMonthlyTransaction>> = flowOf(transactions)
        override suspend fun getAll(): List<RecurringMonthlyTransaction> = transactions
        override suspend fun getById(id: Long): RecurringMonthlyTransaction? =
            transactions.firstOrNull { it.id == id }
        override suspend fun insert(transaction: RecurringMonthlyTransaction): Long = transaction.id
        override suspend fun update(transaction: RecurringMonthlyTransaction) = Unit
        override suspend fun deleteById(id: Long) = Unit
    }

    private class FakeInstallmentTransactionRepository(
        private val transactions: List<InstallmentTransaction>
    ) : InstallmentTransactionRepository {
        override fun observeAll(): Flow<List<InstallmentTransaction>> = flowOf(transactions)
        override fun observeByYearMonth(yearMonth: Int): Flow<List<InstallmentTransaction>> =
            flowOf(transactions.filter { yearMonth in it.yearMonthFrom..it.yearMonthTo })
        override suspend fun getByYearMonth(yearMonth: Int): List<InstallmentTransaction> =
            transactions.filter { yearMonth in it.yearMonthFrom..it.yearMonthTo }
        override suspend fun getById(id: Long): InstallmentTransaction? =
            transactions.firstOrNull { it.id == id }
        override suspend fun insert(transaction: InstallmentTransaction): Long = transaction.id
        override suspend fun update(transaction: InstallmentTransaction) = Unit
        override suspend fun deleteById(id: Long) = Unit
    }

    private class FakeTransactionRepository(
        val transactions: MutableList<Transaction> = mutableListOf()
    ) : TransactionRepository {
        override fun observeAll(): Flow<List<Transaction>> = flowOf(transactions)
        override fun observeByMonth(referenceMonth: Int, referenceYear: Int): Flow<List<Transaction>> =
            flowOf(transactions.filter { it.referenceMonth == referenceMonth && it.referenceYear == referenceYear })
        override suspend fun getByMonth(referenceMonth: Int, referenceYear: Int): List<Transaction> =
            transactions.filter { it.referenceMonth == referenceMonth && it.referenceYear == referenceYear }
        override suspend fun getById(id: Long): Transaction? = transactions.firstOrNull { it.id == id }
        override suspend fun existsForRecurringMonthlyTransaction(
            recurringMonthlyTransactionId: Long,
            referenceMonth: Int,
            referenceYear: Int
        ): Boolean =
            transactions.any {
                it.recurringMonthlyTransactionId == recurringMonthlyTransactionId &&
                    it.referenceMonth == referenceMonth &&
                    it.referenceYear == referenceYear
            }
        override suspend fun existsForInstallmentTransaction(
            installmentTransactionId: Long,
            referenceMonth: Int,
            referenceYear: Int
        ): Boolean =
            transactions.any {
                it.installmentTransactionId == installmentTransactionId &&
                    it.referenceMonth == referenceMonth &&
                    it.referenceYear == referenceYear
            }
        override suspend fun insert(transaction: Transaction): Long {
            val id = (transactions.size + 1).toLong()
            transactions.add(transaction.copy(id = id))
            return id
        }
        override suspend fun insertAll(transactions: List<Transaction>): List<Long> =
            transactions.map { insert(it) }
        override suspend fun update(transaction: Transaction) = Unit
        override suspend fun deleteById(id: Long) = Unit
    }
}
