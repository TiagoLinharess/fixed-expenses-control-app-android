package com.example.fixedexpeneses.domain.repository

interface AppPreferencesRepository {
    fun getOrCreateFirstAvailableYearMonth(currentYearMonth: Int): Int
    fun isGeneratedTransactionDeleted(
        recurringMonthlyTransactionId: Long?,
        installmentTransactionId: Long?,
        referenceMonth: Int,
        referenceYear: Int
    ): Boolean
    fun markGeneratedTransactionDeleted(
        recurringMonthlyTransactionId: Long?,
        installmentTransactionId: Long?,
        referenceMonth: Int,
        referenceYear: Int
    )
}
