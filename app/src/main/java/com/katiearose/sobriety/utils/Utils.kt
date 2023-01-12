package com.katiearose.sobriety.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import androidx.preference.PreferenceManager
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.katiearose.sobriety.R
import com.katiearose.sobriety.activities.Main
import com.katiearose.sobriety.shared.CacheHandler

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

fun Activity.checkValidIntentData() : Int {
    val index = intent.getIntExtra(Main.EXTRA_ADDICTION_POSITION, -1)
    require(index != -1) { "Invalid intent data received" }
    return index
}

fun AppCompatEditText.isInputEmpty(): Boolean = text == null || text.toString().isBlank()

fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

fun Context.toast(@StringRes textRes: Int) = Toast.makeText(this, textRes, Toast.LENGTH_SHORT).show()

inline var TextView.textResource: Int
    set(@StringRes value) = setText(value)
    @Deprecated("This property is set-only, don't bother.", level = DeprecationLevel.HIDDEN)
    get() = error("don't")

fun CacheHandler.write() = writeCache(Main.addictions)