package com.example.fixedexpeneses.navigation

import androidx.annotation.DrawableRes
import com.example.fixedexpeneses.R

enum class TopLevelDestination(
    val label: String,
    @param:DrawableRes val icon: Int,
    val route: String
) {
    HOME(
        label = "Resumo",
        icon = R.drawable.ic_home,
        route = AppRoute.Home.route
    ),
    RECURRING_MONTHLY_TRANSACTIONS(
        label = "Fixas",
        icon = R.drawable.ic_receipt_long,
        route = AppRoute.RecurringMonthlyTransactions.route
    ),
    INSTALLMENT_TRANSACTIONS(
        label = "Parceladas",
        icon = R.drawable.ic_credit_card,
        route = AppRoute.InstallmentTransactions.route
    )
}
