package com.example.fixedexpeneses.ui.installment

import com.example.fixedexpeneses.domain.model.InstallmentTransaction

fun InstallmentTransaction.isActiveIn(yearMonth: Int): Boolean =
    yearMonth in yearMonthFrom..yearMonthTo

fun InstallmentTransaction.installmentCount(): Int =
    monthsBetweenInclusive(yearMonthFrom, yearMonthTo)

fun InstallmentTransaction.installmentNumberAt(yearMonth: Int): Int? {
    if (!isActiveIn(yearMonth)) {
        return null
    }

    return monthsBetweenInclusive(yearMonthFrom, yearMonth)
}

fun InstallmentTransaction.totalAmountInCents(): Long =
    amountInCents * installmentCount()

private fun monthsBetweenInclusive(from: Int, to: Int): Int {
    val fromYear = from / 100
    val fromMonth = from % 100
    val toYear = to / 100
    val toMonth = to % 100

    if (fromMonth !in 1..12 || toMonth !in 1..12) {
        return 0
    }

    return ((toYear - fromYear) * 12) + (toMonth - fromMonth) + 1
}
