package com.example.fixedexpeneses.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fixedexpeneses.FixedExpensesApplication
import com.example.fixedexpeneses.di.AppContainer
import com.example.fixedexpeneses.ui.recurring.RecurringMonthlyTransactionsViewModel

class AppViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecurringMonthlyTransactionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecurringMonthlyTransactionsViewModel(
                appContainer.recurringMonthlyTransactionRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}. Register it in AppViewModelFactory."
        )
    }

    companion object {
        fun from(context: Context): AppViewModelFactory {
            val application = context.applicationContext as FixedExpensesApplication
            return AppViewModelFactory(application.appContainer)
        }
    }
}

@Composable
fun rememberAppViewModelFactory(): AppViewModelFactory {
    val context = LocalContext.current
    return remember(context) {
        AppViewModelFactory.from(context)
    }
}
