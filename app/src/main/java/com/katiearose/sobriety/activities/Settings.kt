package com.katiearose.sobriety.activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.katiearose.sobriety.R
import com.katiearose.sobriety.utils.applyThemes
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemes()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        companion object {
            private val sampleDate = LocalDate.of(2023, Month.JANUARY, 1)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val themePref = requireNotNull(findPreference<ListPreference>("theme")) { "Wrong key passed for theme preference" }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                themePref.setEntries(R.array.theme_entries_p)
                themePref.setEntryValues(R.array.theme_entry_values_p)
            }
            themePref.setOnPreferenceChangeListener { _, _ ->
                requireActivity().recreate()
                true
            }
            val materialYouPref = requireNotNull(findPreference<SwitchPreferenceCompat>("material_you")) { "Wrong key passed for M3 preference" }
            materialYouPref.setOnPreferenceChangeListener { _, _ ->
                requireActivity().recreate()
                true
            }
            val dateFormatPref = requireNotNull(findPreference<ListPreference>("date_format")) { "Wrong key passed for date format preference" }
            dateFormatPref.entries = dateFormatPref.entryValues.map {
                DateTimeFormatter.ofPattern(it.toString()).format(sampleDate)
            }.toTypedArray()
        }
    }
}