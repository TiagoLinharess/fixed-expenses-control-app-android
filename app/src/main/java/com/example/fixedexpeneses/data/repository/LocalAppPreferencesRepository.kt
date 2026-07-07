package com.example.fixedexpeneses.data.repository

import android.content.Context
import com.example.fixedexpeneses.domain.repository.AppPreferencesRepository

class LocalAppPreferencesRepository(
    context: Context
) : AppPreferencesRepository {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override fun getOrCreateFirstAvailableYearMonth(currentYearMonth: Int): Int {
        val savedYearMonth = preferences.getInt(FIRST_AVAILABLE_YEAR_MONTH_KEY, 0)
        if (savedYearMonth != 0) return savedYearMonth

        preferences.edit()
            .putInt(FIRST_AVAILABLE_YEAR_MONTH_KEY, currentYearMonth)
            .apply()

        return currentYearMonth
    }

    override fun isGeneratedTransactionDeleted(
        recurringMonthlyTransactionId: Long?,
        installmentTransactionId: Long?,
        referenceMonth: Int,
        referenceYear: Int
    ): Boolean {
        val key = deletedTransactionKey(
            recurringMonthlyTransactionId = recurringMonthlyTransactionId,
            installmentTransactionId = installmentTransactionId,
            referenceMonth = referenceMonth,
            referenceYear = referenceYear
        ) ?: return false

        return deletedTransactionKeys().contains(key)
    }

    override fun markGeneratedTransactionDeleted(
        recurringMonthlyTransactionId: Long?,
        installmentTransactionId: Long?,
        referenceMonth: Int,
        referenceYear: Int
    ) {
        val key = deletedTransactionKey(
            recurringMonthlyTransactionId = recurringMonthlyTransactionId,
            installmentTransactionId = installmentTransactionId,
            referenceMonth = referenceMonth,
            referenceYear = referenceYear
        ) ?: return

        preferences.edit()
            .putStringSet(
                DELETED_GENERATED_TRANSACTION_KEYS,
                deletedTransactionKeys() + key
            )
            .apply()
    }

    private fun deletedTransactionKeys(): Set<String> =
        preferences.getStringSet(DELETED_GENERATED_TRANSACTION_KEYS, emptySet()).orEmpty()

    private fun deletedTransactionKey(
        recurringMonthlyTransactionId: Long?,
        installmentTransactionId: Long?,
        referenceMonth: Int,
        referenceYear: Int
    ): String? =
        when {
            recurringMonthlyTransactionId != null ->
                "recurring:$recurringMonthlyTransactionId:$referenceYear:$referenceMonth"
            installmentTransactionId != null ->
                "installment:$installmentTransactionId:$referenceYear:$referenceMonth"
            else -> null
        }

    private companion object {
        const val PREFERENCES_NAME = "fixed_expenses_preferences"
        const val FIRST_AVAILABLE_YEAR_MONTH_KEY = "first_available_year_month"
        const val DELETED_GENERATED_TRANSACTION_KEYS = "deleted_generated_transaction_keys"
    }
}
