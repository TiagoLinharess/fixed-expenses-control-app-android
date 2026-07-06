package com.example.fixedexpeneses.ui.installment

fun Int.toMonthYearLabel(): String {
    val year = this / 100
    val month = this % 100

    return if (year > 0 && month in 1..12) {
        "${month.toString().padStart(2, '0')}/$year"
    } else {
        toString()
    }
}

fun Int.toMonthYearDigits(): String {
    val year = this / 100
    val month = this % 100

    return if (year > 0 && month in 1..12) {
        "${month.toString().padStart(2, '0')}$year"
    } else {
        ""
    }
}

fun String.toYearMonthOrNull(): Int? {
    val digits = filter { it.isDigit() }
    if (digits.length != 6) return null

    val monthFirst = digits.take(2).toIntOrNull()
    val yearLast = digits.drop(2).toIntOrNull()
    if (monthFirst != null && yearLast != null && monthFirst in 1..12 && yearLast > 0) {
        return yearLast * 100 + monthFirst
    }

    val yearFirst = digits.take(4).toIntOrNull()
    val monthLast = digits.drop(4).toIntOrNull()
    if (yearFirst != null && monthLast != null && yearFirst > 0 && monthLast in 1..12) {
        return yearFirst * 100 + monthLast
    }

    return null
}
