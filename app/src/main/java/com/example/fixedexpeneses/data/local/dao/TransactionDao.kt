package com.example.fixedexpeneses.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fixedexpeneses.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY referenceYear DESC, referenceMonth DESC, dueDay ASC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE referenceMonth = :referenceMonth AND referenceYear = :referenceYear
        ORDER BY dueDay ASC
        """
    )
    fun observeByMonth(referenceMonth: Int, referenceYear: Int): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE referenceMonth = :referenceMonth AND referenceYear = :referenceYear
        ORDER BY dueDay ASC
        """
    )
    suspend fun getByMonth(referenceMonth: Int, referenceYear: Int): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM transactions
            WHERE recurringMonthlyTransactionId = :recurringMonthlyTransactionId
            AND referenceMonth = :referenceMonth
            AND referenceYear = :referenceYear
        )
        """
    )
    suspend fun existsForRecurringMonthlyTransaction(
        recurringMonthlyTransactionId: Long,
        referenceMonth: Int,
        referenceYear: Int
    ): Boolean

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM transactions
            WHERE installmentTransactionId = :installmentTransactionId
            AND referenceMonth = :referenceMonth
            AND referenceYear = :referenceYear
        )
        """
    )
    suspend fun existsForInstallmentTransaction(
        installmentTransactionId: Long,
        referenceMonth: Int,
        referenceYear: Int
    ): Boolean

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
