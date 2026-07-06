package com.example.fixedexpeneses.di

import com.example.fixedexpeneses.domain.repository.InstallmentTransactionRepository
import com.example.fixedexpeneses.domain.repository.RecurringMonthlyTransactionRepository
import com.example.fixedexpeneses.domain.repository.TransactionRepository
import com.example.fixedexpeneses.domain.usecase.GenerateMonthlyTransactionsUseCase

interface AppContainer {
    val recurringMonthlyTransactionRepository: RecurringMonthlyTransactionRepository
    val installmentTransactionRepository: InstallmentTransactionRepository
    val transactionRepository: TransactionRepository
    val generateMonthlyTransactionsUseCase: GenerateMonthlyTransactionsUseCase
}
