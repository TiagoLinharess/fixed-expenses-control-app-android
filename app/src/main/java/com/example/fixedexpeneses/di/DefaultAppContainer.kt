package com.example.fixedexpeneses.di

import android.content.Context
import androidx.room.Room
import com.example.fixedexpeneses.data.local.AppDatabase
import com.example.fixedexpeneses.data.repository.LocalAppPreferencesRepository
import com.example.fixedexpeneses.data.repository.LocalInstallmentTransactionRepository
import com.example.fixedexpeneses.data.repository.LocalRecurringMonthlyTransactionRepository
import com.example.fixedexpeneses.data.repository.LocalTransactionRepository
import com.example.fixedexpeneses.domain.repository.AppPreferencesRepository
import com.example.fixedexpeneses.domain.repository.InstallmentTransactionRepository
import com.example.fixedexpeneses.domain.repository.RecurringMonthlyTransactionRepository
import com.example.fixedexpeneses.domain.repository.TransactionRepository
import com.example.fixedexpeneses.domain.usecase.GenerateMonthlyTransactionsUseCase

class DefaultAppContainer(context: Context) : AppContainer {
    private val applicationContext = context.applicationContext

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context = applicationContext,
            klass = AppDatabase::class.java,
            name = DATABASE_NAME
        ).build()
    }

    override val appPreferencesRepository: AppPreferencesRepository by lazy {
        LocalAppPreferencesRepository(applicationContext)
    }

    override val recurringMonthlyTransactionRepository: RecurringMonthlyTransactionRepository by lazy {
        LocalRecurringMonthlyTransactionRepository(database.recurringMonthlyTransactionDao())
    }

    override val installmentTransactionRepository: InstallmentTransactionRepository by lazy {
        LocalInstallmentTransactionRepository(database.installmentTransactionDao())
    }

    override val transactionRepository: TransactionRepository by lazy {
        LocalTransactionRepository(database.transactionDao())
    }

    override val generateMonthlyTransactionsUseCase: GenerateMonthlyTransactionsUseCase by lazy {
        GenerateMonthlyTransactionsUseCase(
            recurringMonthlyTransactionRepository = recurringMonthlyTransactionRepository,
            installmentTransactionRepository = installmentTransactionRepository,
            transactionRepository = transactionRepository,
            appPreferencesRepository = appPreferencesRepository
        )
    }

    private companion object {
        const val DATABASE_NAME = "fixed_expenses.db"
    }
}
