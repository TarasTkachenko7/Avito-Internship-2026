package com.example.avito_testing_2026_autum.core.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val baseFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

private val shortDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yy")

fun Long.toFormattedDateString(
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault()
): String {
    return Instant.ofEpochMilli(this)
        .atZone(zoneId)
        .format(baseFormatter.withLocale(locale))
}

fun Long.toShortFormattedDateString(
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault()
): String {
    return Instant.ofEpochMilli(this)
        .atZone(zoneId)
        .format(shortDateFormatter.withLocale(locale))
}