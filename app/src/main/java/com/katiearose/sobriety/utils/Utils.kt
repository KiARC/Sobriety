package com.katiearose.sobriety.utils

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.katiearose.sobriety.R
import org.json.JSONArray
import org.json.JSONObject
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

/**
 * Returns JSONObject with keys and values keys as JSONArray
 * This is because JSONObjects do not have guaranteed order
 * process_key and process_value are optional functions to convert key and val to JSONObjects
 */
fun <K, V> Map<K, V>.toJSONObject(
    process_key: ((K) -> Object)? = null,
    process_value: ((V) -> Object)? = null
): JSONObject {
    var json = JSONObject()
    json.put("keys", JSONArray())
    json.put("values", JSONArray())
    for ((k, v) in this) {
        json.accumulate("keys",
            if (process_key != null) {
                process_key(k)
            } else k
        )
        json.accumulate("values",
            if (process_value != null) {
                process_value(v)
            } else v
        )
    }
    return json
}

/**
 * Returns a LinkedHashMap
 * input JSONObject is structured as 2 JSONArrays "keys" and "values"
 *  process_key and process_value are optional functions to convert Objects to keys and values
 */
fun <K, V> JSONObject.toLinkedHashMap(
    process_key: ((Object) -> K)? = null,
    process_value: ((Object) -> V)? = null
): LinkedHashMap<K,V> {
    var hash = LinkedHashMap<K, V>()

    val keys = this.getJSONArray("keys")
    val values = this.getJSONArray("values")

    for (i in 0 until keys.length()) {
        val key = if (process_key != null) {
            process_key(keys.get(i) as Object)
        } else keys.get(i) as K

        val value = if (process_value != null) {
            process_value(values.get(i) as Object)
        } else values.get(i) as V

        hash[key] = value
    }

    return hash
}
