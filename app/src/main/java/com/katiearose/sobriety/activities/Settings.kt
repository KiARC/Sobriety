package com.katiearose.sobriety.activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.katiearose.sobriety.R
import com.katiearose.sobriety.utils.applyThemes

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
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val themePref = findPreference<ListPreference>("theme")
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                themePref?.setEntries(R.array.theme_entries_p)
                themePref?.setEntryValues(R.array.theme_entry_values_p)
            }
            themePref?.setOnPreferenceChangeListener { _, _ ->
                requireActivity().recreate()
                true
            }
            val materialYouPref = findPreference<SwitchPreferenceCompat>("material_you")
            materialYouPref?.setOnPreferenceChangeListener { _, _ ->
                requireActivity().recreate()
                true
            }
        }
    }
}