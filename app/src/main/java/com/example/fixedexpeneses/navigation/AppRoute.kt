package com.example.fixedexpeneses.navigation

sealed interface AppRoute {
    val route: String

    data object Home : AppRoute {
        override val route: String = "home"
    }

    data object RecurringMonthlyTransactions : AppRoute {
        override val route: String = "recurring-monthly-transactions"
    }

    data object CreateRecurringMonthlyTransaction : AppRoute {
        override val route: String = "recurring-monthly-transactions/create"
    }

    data object RecurringMonthlyTransactionDetail : AppRoute {
        override val route: String = "recurring-monthly-transactions/{transactionId}"
        const val TRANSACTION_ID_ARGUMENT = "transactionId"

        fun createRoute(transactionId: Long): String =
            "recurring-monthly-transactions/$transactionId"
    }

    data object EditRecurringMonthlyTransaction : AppRoute {
        override val route: String = "recurring-monthly-transactions/{transactionId}/edit"
        const val TRANSACTION_ID_ARGUMENT = "transactionId"

        fun createRoute(transactionId: Long): String =
            "recurring-monthly-transactions/$transactionId/edit"
    }

    data object InstallmentTransactions : AppRoute {
        override val route: String = "installment-transactions"
    }
}
