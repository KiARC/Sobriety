package com.katiearose.sobriety.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.preference.PreferenceManager
import com.katiearose.sobriety.Addiction
import com.katiearose.sobriety.activities.Main
import org.json.JSONObject
import java.io.BufferedReader

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

fun Context.exportData(uri: Uri) {
    // Construct the JSON Object
    var json: JSONObject = JSONObject();
    for (addiction in Main.addictions) {
        json.put(addiction.name, addiction.toJSON())
    }

    // Write to file
    json.toString(4).byteInputStream().use { input ->
        contentResolver.openOutputStream(uri)?.use { output ->
            input.copyTo(output)
        }
    }
}


fun Context.importData(uri: Uri) {
    // Read file
    val json: JSONObject;
    BufferedReader(contentResolver.openInputStream(uri)?.reader()).use { reader ->
        json = JSONObject(reader.readText())
    }

    // Construct Addictions
    for (name in json.keys()) {
        // Don't clear existing addictions
        Main.addictions.add(Addiction.fromJSON(json.getJSONObject(name)))
    }

}