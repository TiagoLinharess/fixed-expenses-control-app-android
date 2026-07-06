package com.example.fixedexpeneses.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fixedexpeneses.data.local.entity.RecurringMonthlyTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringMonthlyTransactionDao {
    @Query("SELECT * FROM recurring_monthly_transactions ORDER BY dueDay ASC")
    fun observeAll(): Flow<List<RecurringMonthlyTransactionEntity>>

    @Query("SELECT * FROM recurring_monthly_transactions ORDER BY dueDay ASC")
    suspend fun getAll(): List<RecurringMonthlyTransactionEntity>

    @Query("SELECT * FROM recurring_monthly_transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RecurringMonthlyTransactionEntity?

    @Insert
    suspend fun insert(transaction: RecurringMonthlyTransactionEntity): Long

    @Update
    suspend fun update(transaction: RecurringMonthlyTransactionEntity)

    @Query("DELETE FROM recurring_monthly_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
