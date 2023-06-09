package com.katiearose.sobriety.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.katiearose.sobriety.AddictionCardAdapter
import com.katiearose.sobriety.R
import com.katiearose.sobriety.databinding.ActivityMainBinding
import com.katiearose.sobriety.databinding.DialogAddNoteAfterRelapseBinding
import com.katiearose.sobriety.databinding.DialogMiscBinding
import com.katiearose.sobriety.shared.Addiction
import com.katiearose.sobriety.shared.CacheHandler
import com.katiearose.sobriety.utils.applyThemes
import com.katiearose.sobriety.utils.getAddNoteAfterRelapsePref
import com.katiearose.sobriety.utils.getSharedPref
import com.katiearose.sobriety.utils.isInputEmpty
import com.katiearose.sobriety.utils.showConfirmDialog
import com.katiearose.sobriety.utils.write
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.FileNotFoundException
import java.time.Instant
import java.util.*

class Main : AppCompatActivity() {

    companion object {
        const val EXTRA_NAMES = "com.katiearose.sobriety.EXTRA_NAMES"
        // Pass the position of the addiction so that it can be modified
        const val EXTRA_ADDICTION_POSITION = "com.katiearose.sobriety.EXTRA_ADDICTION_POSITION"
        val addictions = ArrayList<Addiction>()
    }

    private lateinit var adapterAddictions: AddictionCardAdapter
    private lateinit var cacheHandler: CacheHandler
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("NotifyDataSetChanged")
    private val addNewAddiction =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val data = requireNotNull(requireNotNull(it.data) { "Something is wrong in the Create activity, go check the create() function" }.extras) { "Something is wrong in the Create activity, go check the create() function" }
                val name = data.getString("name")!!
                val instant = data.getLong("instant")
                val priority =
                    Addiction.Priority.values()[data.getInt("priority")]
                val addiction = Addiction.newInstance(name, instant, priority)
                if (addiction.status == Addiction.Status.Future) {
                    // Update status at that point in time
                    addTimerForFutureAddiction(addiction)
                }
                addictions.add(addiction)
                addictions.sortWith { a1, a2 -> a1.priority.compareTo(a2.priority) }
                cacheHandler.write()
                adapterAddictions.notifyDataSetChanged()
            }
        }

    private val goToSettings = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            if (it.data!!.extras!!.getBoolean("import")) {
                addictions.sortWith { a1, a2 -> a1.priority.compareTo(a2.priority) }
                cacheHandler.write()
            }
        }
        adapterAddictions.notifyDataSetChanged()
    }

    //needed for this activity to apply md3 theme after user backs away from settings
    private val materialYouSettingListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "material_you") recreate()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getString("theme", "system") == "system" &&
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
        ) {
            preferences.edit { putString("theme", "light") }
        }
        preferences.registerOnSharedPreferenceChangeListener(materialYouSettingListener)
        applyThemes()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addCardButton.setOnClickListener { newCardDialog() }
        cacheHandler = CacheHandler(this)
        if (addictions.isEmpty())
            try {
                this.openFileInput("Sobriety.cache").use {
                    addictions.addAll(cacheHandler.readCache(it))
                }
            } catch (_: FileNotFoundException) {
            }

        updatePromptVisibility()

        //Create adapter, and layout manager for recyclerview and attach them
        adapterAddictions = AddictionCardAdapter(this, deleteButtonAction =  {
            val action: () -> Unit = {
                adapterAddictions.notifyItemRemoved(addictions.indexOf(it))
                addictions.remove(it)
                updatePromptVisibility()
                cacheHandler.write()
            }
            showConfirmDialog(
                getString(R.string.delete),
                getString(R.string.delete_confirm, it.name),
                action
            )
        }, relapseButtonAction = {
            val (title, question, extra_action) = when (it.status) {
                Addiction.Status.Ongoing ->
                    Triple(
                        getString(R.string.relapse),
                        getString(R.string.relapse_confirm, it.name)
                    ) { showAddNoteAfterRelapseDialogIfEnabled(it) }

                Addiction.Status.Stopped, Addiction.Status.Future ->
                    Triple(
                        getString(R.string.track_now),
                        getString(R.string.start_tracking_now, it.name)
                    ) {}
            }
            showConfirmDialog(title, question) {
                it.relapse()
                adapterAddictions.notifyItemChanged(addictions.indexOf(it))
                cacheHandler.write()
                extra_action()
            }
        }, stopButtonAction = {
            when (it.status) {
                Addiction.Status.Ongoing ->
                    showConfirmDialog(
                        getString(R.string.stop),
                        getString(R.string.stop_confirm, it.name)
                    ) {
                        it.stopAbstaining()
                        adapterAddictions.notifyItemChanged(addictions.indexOf(it))
                        cacheHandler.write()
                        showAddNoteAfterRelapseDialogIfEnabled(it)
                    }

                Addiction.Status.Stopped ->
                    Snackbar.make(
                        binding.root,
                        getString(R.string.already_stopped, it.name),
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()

                Addiction.Status.Future ->
                    Snackbar.make(
                        binding.root,
                        getString(R.string.not_tracked_yet, it.name),
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
            }
        }, timelineButtonAction = {
            when (it.status) {
                Addiction.Status.Ongoing, Addiction.Status.Stopped ->
                    startActivity(
                        Intent(this@Main, Timeline::class.java)
                            .putExtra(EXTRA_ADDICTION_POSITION, addictions.indexOf(it))
                    )

                Addiction.Status.Future ->
                    Snackbar.make(
                        binding.root,
                        getString(R.string.not_tracked_yet, it.name),
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
            }
        }, priorityTextViewAction = {
            var choice = it.priority.ordinal
            MaterialAlertDialogBuilder(this@Main)
                .setTitle(R.string.edit_priority)
                .setSingleChoiceItems(
                    R.array.priorities,
                    it.priority.ordinal
                ) { _, which -> choice = which }
                .setPositiveButton(R.string.edit) { _, _ ->
                    it.priority = Addiction.Priority.values()[choice]
                    addictions.sortWith { a1, a2 -> a1.priority.compareTo(a2.priority) }
                    cacheHandler.write()
                    adapterAddictions.notifyDataSetChanged()
                    Snackbar.make(
                        binding.root,
                        R.string.edit_priority_success,
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }, miscButtonAction = { a ->
            val dialogViewBinding = DialogMiscBinding.inflate(layoutInflater)
            val dialog = BottomSheetDialog(this@Main)
            dialogViewBinding.dailyNotes.setOnClickListener {
                startActivity(
                    Intent(this@Main, DailyNotes::class.java)
                        .putExtra(EXTRA_ADDICTION_POSITION, addictions.indexOf(a))
                )
                dialog.dismiss()
            }
            dialogViewBinding.savings.setOnClickListener {
                startActivity(
                    Intent(this@Main, Savings::class.java)
                        .putExtra(EXTRA_ADDICTION_POSITION, addictions.indexOf(a))
                )
                dialog.dismiss()
            }
            dialogViewBinding.milestones.setOnClickListener {
                startActivity(
                    Intent(this@Main, Milestones::class.java)
                        .putExtra(EXTRA_ADDICTION_POSITION, addictions.indexOf(a))
                )
                dialog.dismiss()
            }
            dialog.setContentView(dialogViewBinding.root)
            dialog.show()
        }, cardButtonAction = {
            when(it.status) {
                Addiction.Status.Ongoing, Addiction.Status.Stopped ->
                    startActivity(
                        Intent(this@Main, Summary::class.java)
                            .putExtra(EXTRA_ADDICTION_POSITION, addictions.indexOf(it))
                    )

                Addiction.Status.Future ->
                    Snackbar.make(
                        binding.root,
                        getString(R.string.not_tracked_yet, it.name),
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
            }

        })
        binding.recyclerAddictions.layoutManager = LinearLayoutManager(this)
        binding.recyclerAddictions.adapter = adapterAddictions

        // Add a timer to start an attempt on future addictions
        for (addiction in addictions) {
            if (addiction.status == Addiction.Status.Future)
                addTimerForFutureAddiction(addiction)
        }
    }

    private fun addTimerForFutureAddiction(addiction: Addiction) {
        // Also works for negative delays
        // See https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/os/Handler.java#693
        val delay = addiction.history.keys.last() - Instant.now().toEpochMilli()
        Handler(Looper.getMainLooper()).postDelayed({
            addiction.status = Addiction.Status.Ongoing
        }, delay)
    }

    private fun showAddNoteAfterRelapseDialogIfEnabled(addiction: Addiction) {
        val pref = getSharedPref()
        if (pref.getAddNoteAfterRelapsePref()) {
            var dialog: Dialog? = null
            val dialogViewBinding = DialogAddNoteAfterRelapseBinding.inflate(layoutInflater)
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            with(dialogViewBinding) {
                noteInput.setText(addiction.dailyNotes[today]) //TextView.setText does accept a null CharSequence.
                btnSave.setOnClickListener { _ ->
                    if (noteInput.isInputEmpty()) {
                        noteInputLayout.error = getString(R.string.error_empty_note)
                    } else {
                        addiction.dailyNotes[today] = noteInput.text.toString()
                        cacheHandler.write()
                        requireNotNull(dialog).dismiss()
                    }
                }
            }
            with(BottomSheetDialog(this)) {
                dialog = this
                setContentView(dialogViewBinding.root)
                setOnDismissListener {
                    if (dialogViewBinding.dontShowAgain.isChecked) {
                        pref.edit { putBoolean("add_note_after_relapse", false) }
                    }
                }
                show()
            }
        }
    }

    private fun updatePromptVisibility() {
        binding.prompt.visibility = if (addictions.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun newCardDialog() {
        //Pass current addiction names to create activity, to prevent creation of elements with identical keys
        val intent = Intent(this, Create::class.java)
            .putStringArrayListExtra(EXTRA_NAMES, addictions.mapTo(arrayListOf()) { it.name })
        addNewAddiction.launch(intent)
    }

    /**
     * This gets called once the Create Activity is closed (Necessary to hide the prompt in case
     * a first addiction was added to the list.
     */
    override fun onResume() {
        updatePromptVisibility()
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.go_to_settings) {
            val intent = Intent(this, Settings::class.java)
            goToSettings.launch(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}