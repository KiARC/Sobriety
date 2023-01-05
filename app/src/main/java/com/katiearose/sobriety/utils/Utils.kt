package com.katiearose.sobriety.utils

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.katiearose.sobriety.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.Period
import java.time.Duration
import java.util.TimeZone

private const val MINUTE = 60
private const val HOUR = MINUTE * 60
private const val DAY = HOUR * 24
private const val WEEK = DAY * 7
private const val YEAR = (DAY * 365.25).toInt()
private const val MONTH = YEAR / 12


fun Context.convertSecondsToString(given: Long): String {
    if (given == -1L) return ""
    var time = given
    val s = time % MINUTE
    time -= s
    val m = (time % HOUR) / MINUTE
    time -= m * MINUTE
    val h = (time % DAY) / HOUR
    time -= h * HOUR
    val d = (time % WEEK) / DAY
    time -= d * DAY
    val w = (time % MONTH) / WEEK
    time -= w * WEEK
    val mo = (time % YEAR) / MONTH
    time -= mo * MONTH
    val y = time / YEAR
    val stringBuilder = StringBuilder()
    if (y != 0L) stringBuilder.append(getString(R.string.years, y)).append(" ")
    if (mo != 0L) stringBuilder.append(getString(R.string.months, mo)).append(" ")
    if (w != 0L) stringBuilder.append(getString(R.string.weeks, w)).append(" ")
    if (d != 0L) stringBuilder.append(getString(R.string.days, d)).append(" ")
    if (h != 0L) stringBuilder.append(getString(R.string.hours, h)).append(" ")
    if (m != 0L) stringBuilder.append(getString(R.string.minutes, m)).append(" ")
    if (!(y == 0L && mo == 0L && w == 0L && d == 0L && h == 0L && m == 0L)) stringBuilder.append(
        getString(R.string.and)
    ).append(" ")
    stringBuilder.append(getString(R.string.seconds, s))
    return stringBuilder.toString()
}

// Expects start and end timestamps in epoch milliseconds
fun Context.convertRangeToString(start: Long, end: Long = Instant.now().toEpochMilli()): String {
    if (start == -1L) return ""
    val startDate: LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(start),
        TimeZone.getDefault().toZoneId())
    val endDate: LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(end),
        TimeZone.getDefault().toZoneId())
    // Period for years, months, weeks, days
    val period: Period = Period.between(startDate.toLocalDate(), endDate.toLocalDate())
    // Duration for hours, minutes, seconds
    val duration: Duration = Duration.between(startDate, endDate)

    val y = period.years
    val mo = period.months
    val d = period.days % 7
    val w = period.days / 7
    val h = duration.toHoursPart()
    val m = duration.toMinutesPart()
    val s = duration.toSecondsPart()

    val stringBuilder = StringBuilder()
    if (y != 0) stringBuilder.append(getString(R.string.years, y)).append(" ")
    if (mo != 0) stringBuilder.append(getString(R.string.months, mo)).append(" ")
    if (w != 0) stringBuilder.append(getString(R.string.weeks, w)).append(" ")
    if (d != 0) stringBuilder.append(getString(R.string.days, d)).append(" ")
    if (h != 0) stringBuilder.append(getString(R.string.hours, h)).append(" ")
    if (m != 0) stringBuilder.append(getString(R.string.minutes, m)).append(" ")
    if (!(y == 0 && mo == 0 && w == 0 && d == 0 && h == 0 && m == 0)) stringBuilder.append(
        getString(R.string.and)
    ).append(" ")
    stringBuilder.append(getString(R.string.seconds, s))
    return stringBuilder.toString()
}

fun Context.showConfirmDialog(title: String, message: String, action: () -> Unit) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok) { _, _ -> action() }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}

fun Activity.applyThemes() {
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
    when (preferences.getString("theme", "system")) {
        "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
    if (preferences.getBoolean("material_you", false)) {
        setTheme(R.style.Theme_Sobriety_Material3)
    } else setTheme(R.style.Theme_Sobriety)
}

fun AppCompatEditText.isInputEmpty(): Boolean {
    return text == null || text.toString().isBlank()
}

fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

fun Instant.secondsFromNow(): Long = Instant.now().epochSecond - this.epochSecond

/**
 * Puts the specified value to the last key in this map.
 */
fun <K, V> LinkedHashMap<K, V>.putLast(value: V) {
    val lastKey = keys.map { it }.last()
    put(lastKey, value)
}