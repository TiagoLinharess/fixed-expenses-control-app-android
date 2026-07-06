package com.example.fixedexpeneses.domain.usecase

import com.example.fixedexpeneses.domain.model.InstallmentTransaction
import com.example.fixedexpeneses.domain.model.PaymentStatus
import com.example.fixedexpeneses.domain.model.RecurringMonthlyTransaction
import com.example.fixedexpeneses.domain.model.Transaction
import com.example.fixedexpeneses.domain.repository.InstallmentTransactionRepository
import com.example.fixedexpeneses.domain.repository.RecurringMonthlyTransactionRepository
import com.example.fixedexpeneses.domain.repository.TransactionRepository

class GenerateMonthlyTransactionsUseCase(
    private val recurringMonthlyTransactionRepository: RecurringMonthlyTransactionRepository,
    private val installmentTransactionRepository: InstallmentTransactionRepository,
    private val transactionRepository: TransactionRepository,
    private val now: () -> Long = { System.currentTimeMillis() }
) {
    suspend operator fun invoke(referenceMonth: Int, referenceYear: Int): List<Long> {
        require(referenceMonth in 1..12) { "referenceMonth must be between 1 and 12." }
        require(referenceYear > 0) { "referenceYear must be greater than 0." }

        val yearMonth = referenceYear * 100 + referenceMonth
        val createdTransactions = buildList {
            addAll(createRecurringTransactions(referenceMonth, referenceYear))
            addAll(createInstallmentTransactions(yearMonth, referenceMonth, referenceYear))
        }

        if (createdTransactions.isEmpty()) {
            return emptyList()
        }

        return transactionRepository.insertAll(createdTransactions)
    }

    private suspend fun createRecurringTransactions(
        referenceMonth: Int,
        referenceYear: Int
    ): List<Transaction> =
        recurringMonthlyTransactionRepository.getAll()
            .filterNot { recurringTransaction ->
                transactionRepository.existsForRecurringMonthlyTransaction(
                    recurringMonthlyTransactionId = recurringTransaction.id,
                    referenceMonth = referenceMonth,
                    referenceYear = referenceYear
                )
            }
            .map { recurringTransaction ->
                recurringTransaction.toMonthlyTransaction(
                    referenceMonth = referenceMonth,
                    referenceYear = referenceYear,
                    timestamp = now()
                )
            }

    private suspend fun createInstallmentTransactions(
        yearMonth: Int,
        referenceMonth: Int,
        referenceYear: Int
    ): List<Transaction> =
        installmentTransactionRepository.getByYearMonth(yearMonth)
            .filterNot { installmentTransaction ->
                transactionRepository.existsForInstallmentTransaction(
                    installmentTransactionId = installmentTransaction.id,
                    referenceMonth = referenceMonth,
                    referenceYear = referenceYear
                )
            }
            .map { installmentTransaction ->
                installmentTransaction.toMonthlyTransaction(
                    referenceMonth = referenceMonth,
                    referenceYear = referenceYear,
                    timestamp = now()
                )
            }

    private fun RecurringMonthlyTransaction.toMonthlyTransaction(
        referenceMonth: Int,
        referenceYear: Int,
        timestamp: Long
    ): Transaction =
        Transaction(
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
            createdAt = timestamp,
            updatedAt = timestamp
        )

    private fun InstallmentTransaction.toMonthlyTransaction(
        referenceMonth: Int,
        referenceYear: Int,
        timestamp: Long
    ): Transaction =
        Transaction(
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
            createdAt = timestamp,
            updatedAt = timestamp
        )
}
