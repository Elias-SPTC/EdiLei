package com.example.edilei.utils

import java.text.NumberFormat
import java.util.Locale

fun Double.formatBrl(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
    return formatter.format(this)
}

fun Double.formatBrlSemSimbolo(): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return formatter.format(this)
}

fun String.parseBrlToDouble(): Double {
    if (this.isBlank()) return 0.0
    val clean = this.replace("R$", "")
        .replace(" ", "")
        .replace("\u00A0", "")
        .replace(".", "")
        .replace(",", ".")
        .trim()
    return clean.toDoubleOrNull() ?: 0.0
}
