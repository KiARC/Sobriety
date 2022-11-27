package com.katiearose.sobriety.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

fun Context.getSharedPref(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

fun SharedPreferences.getSortNotesPref(): String = getString("sort_notes", "asc")!!

fun SharedPreferences.getSortMilestonesPref(): String = getString("sort_milestones", "asc")!!

fun SharedPreferences.getHideCompletedMilestonesPref(): Boolean =
    getBoolean("hide_completed_milestones", false)
