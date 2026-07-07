package com.example.fixedexpeneses.ui.home

import java.util.Calendar

fun currentYearMonth(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)
}

fun Int.plusMonths(months: Int): Int {
    val year = this / 100
    val month = this % 100
    val monthIndex = year * 12 + (month - 1) + months
    val newYear = monthIndex / 12
    val newMonth = monthIndex % 12 + 1

    return newYear * 100 + newMonth
}

fun Int.referenceMonth(): Int = this % 100

fun Int.referenceYear(): Int = this / 100

fun yearMonthRange(from: Int, to: Int): List<Int> {
    if (from <= 0 || to <= 0 || from > to) return emptyList()

    return buildList {
        var current = from
        while (current <= to) {
            add(current)
            current = current.plusMonths(1)
        }
    }
}

fun Int.toMonthYearLabel(): String {
    val year = this / 100
    val month = this % 100
    val monthName = monthNames.getOrNull(month - 1)

    return if (year > 0 && monthName != null) {
        "$monthName/$year"
    } else {
        ""
    }
}

private val monthNames = listOf(
    "Janeiro",
    "Fevereiro",
    "Março",
    "Abril",
    "Maio",
    "Junho",
    "Julho",
    "Agosto",
    "Setembro",
    "Outubro",
    "Novembro",
    "Dezembro"
)
