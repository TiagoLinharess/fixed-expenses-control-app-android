package com.example.fixedexpeneses.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fixedexpeneses.data.local.entity.InstallmentTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentTransactionDao {
    @Query("SELECT * FROM installment_transactions ORDER BY yearMonthFrom ASC, dueDay ASC")
    fun observeAll(): Flow<List<InstallmentTransactionEntity>>

    @Query(
        """
        SELECT * FROM installment_transactions
        WHERE :yearMonth BETWEEN yearMonthFrom AND yearMonthTo
        ORDER BY dueDay ASC
        """
    )
    fun observeByYearMonth(yearMonth: Int): Flow<List<InstallmentTransactionEntity>>

    @Query(
        """
        SELECT * FROM installment_transactions
        WHERE :yearMonth BETWEEN yearMonthFrom AND yearMonthTo
        ORDER BY dueDay ASC
        """
    )
    suspend fun getByYearMonth(yearMonth: Int): List<InstallmentTransactionEntity>

    @Query("SELECT * FROM installment_transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): InstallmentTransactionEntity?

    @Insert
    suspend fun insert(transaction: InstallmentTransactionEntity): Long

    @Update
    suspend fun update(transaction: InstallmentTransactionEntity)

    @Query("DELETE FROM installment_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
