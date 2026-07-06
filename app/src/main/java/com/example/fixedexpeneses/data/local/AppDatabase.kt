package com.example.fixedexpeneses.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fixedexpeneses.data.local.dao.InstallmentTransactionDao
import com.example.fixedexpeneses.data.local.dao.RecurringMonthlyTransactionDao
import com.example.fixedexpeneses.data.local.dao.TransactionDao
import com.example.fixedexpeneses.data.local.entity.InstallmentTransactionEntity
import com.example.fixedexpeneses.data.local.entity.RecurringMonthlyTransactionEntity
import com.example.fixedexpeneses.data.local.entity.TransactionEntity

@Database(
    entities = [
        RecurringMonthlyTransactionEntity::class,
        InstallmentTransactionEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocalTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recurringMonthlyTransactionDao(): RecurringMonthlyTransactionDao
    abstract fun installmentTransactionDao(): InstallmentTransactionDao
    abstract fun transactionDao(): TransactionDao
}
