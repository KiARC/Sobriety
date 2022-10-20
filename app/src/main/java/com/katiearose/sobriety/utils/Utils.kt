package com.katiearose.sobriety.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.katiearose.sobriety.R
import java.time.Instant

private const val MINUTE = 60
private const val HOUR = MINUTE * 60
private const val DAY = HOUR * 24
private const val WEEK = DAY * 7
private const val MONTH = DAY * 31
private const val YEAR = MONTH * 12

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

fun Context.showConfirmDialog(title: String, message: String, action: () -> Unit) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok) { _, _ -> action() }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}

fun Instant.secondsFromNow(): Long = Instant.now().epochSecond - this.epochSecond

/**
 * Puts the specified value to the last key in this map.
 */
fun <K, V> LinkedHashMap<K, V>.putLast(value: V) {
    val lastKey = keys.map { it }.last()
    put(lastKey, value)
}

fun <K, V> LinkedHashMap<K, V>.getKeyValuePairAtIndex(index: Int): Pair<K, V> {
    val key = keys.map { it }[index]
    val value = values.map { it }[index]
    return Pair(key, value)
}