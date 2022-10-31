package com.katiearose.sobriety.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

fun Context.getSharedPref(): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(this)
}

fun SharedPreferences.getSortNotesPref(): String {
    return getString("sort_notes", "asc")!!
}

fun SharedPreferences.getSortMilestonesPref(): String {
    return getString("sort_milestones", "asc")!!
}

fun SharedPreferences.getHideCompletedMilestonesPref(): Boolean {
    return getBoolean("hide_completed_milestones", false)
}