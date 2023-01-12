package com.katiearose.sobriety.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.katiearose.sobriety.R
import com.katiearose.sobriety.utils.applyThemes
import com.katiearose.sobriety.utils.exportData
import com.katiearose.sobriety.utils.importData

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

            val getExport = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
                if (uri != null) {
                    context?.exportData(uri)
                }
            }
            findPreference<Preference>("data_export")?.setOnPreferenceClickListener {
                getExport.launch("sobriety_data.json")
                true
            }

            val getImport = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                if (uri != null) {
                    context?.importData(uri)
                    val intent = Intent()
                        .putExtra("import", true)
                    requireActivity().setResult(Activity.RESULT_OK, intent)
                    requireActivity().finish()
                }
            }
            findPreference<Preference>("data_import")?.setOnPreferenceClickListener {
                getImport.launch(arrayOf<String>("application/json"))
                true
            }
        }
    }
}
