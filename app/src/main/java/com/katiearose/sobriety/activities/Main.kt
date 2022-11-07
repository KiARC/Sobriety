package com.katiearose.sobriety.activities

import android.annotation.SuppressLint
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
import com.katiearose.sobriety.Addiction
import com.katiearose.sobriety.AddictionCardAdapter
import com.katiearose.sobriety.R
import com.katiearose.sobriety.databinding.ActivityMainBinding
import com.katiearose.sobriety.databinding.DialogMiscBinding
import com.katiearose.sobriety.internal.CacheHandler
import com.katiearose.sobriety.utils.applyThemes
import com.katiearose.sobriety.utils.showConfirmDialog
import java.io.FileNotFoundException
import java.time.Instant
import java.time.LocalTime
import java.util.*

class Main : AppCompatActivity() {

    companion object {
        const val EXTRA_NAMES = "com.katiearose.sobriety.EXTRA_NAMES"
        const val EXTRA_ADDICTION_POSITION = "com.katiearose.sobriety.EXTRA_ADDICTION_POSITION"
        val addictions = ArrayList<Addiction>()
        var deleting = false
    }

    private lateinit var adapterAddictions: AddictionCardAdapter
    private lateinit var cacheHandler: CacheHandler
    private lateinit var binding: ActivityMainBinding
    @Suppress("DEPRECATION") //google, why did you deprecate a function that's literally the only way on android 12 and lower
    @SuppressLint("NotifyDataSetChanged")
    private val addNewAddiction = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val name = it.data?.extras?.getString("name") as String
            val instant =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    it.data?.extras?.getSerializable("instant", Instant::class.java) as Instant
                else it.data?.extras?.getSerializable("instant") as Instant
            val priority =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    it.data?.extras?.getSerializable("priority", Addiction.Priority::class.java) as Addiction.Priority
                else it.data?.extras?.getSerializable("priority") as Addiction.Priority
            val addiction = Addiction(name, instant, false, 0, LinkedHashMap(), priority, LinkedHashMap(),
            LocalTime.of(0, 0), LinkedHashMap(), LinkedHashSet()
            )
            if (!addiction.isFuture())
                addiction.history[instant.toEpochMilli()] = 0
            addictions.add(addiction)
            addictions.sortWith { a1, a2 ->
                a1.priority.compareTo(a2.priority)
            }
            cacheHandler.writeCache()
            adapterAddictions.notifyDataSetChanged()
        }
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
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            preferences.edit(commit = true) { putString("theme", "light") }
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
        } catch (e: FileNotFoundException) {
        }

        updatePromptVisibility()

        //Create adapter, and layout manager for recyclerview and attach them
        adapterAddictions = AddictionCardAdapter(this)
        adapterAddictions.apply {
            setDeleteButtonAction {
                val selectedAddiction = addictions[it]
                val action: () -> Unit = {
                    addictions.remove(selectedAddiction)
                    updatePromptVisibility()
                    this.notifyItemRemoved(it)
                    deleting = true
                    cacheHandler.writeCache()
                }
                showConfirmDialog(getString(R.string.delete), getString(R.string.delete_confirm, selectedAddiction.name), action)
            }
            setRelapseButtonAction {
                val selectedAddiction = addictions[it]
                if (!selectedAddiction.isFuture()) {
                    val action: () -> Unit = {
                        selectedAddiction.relapse()
                        this.notifyItemChanged(it)
                        cacheHandler.writeCache()
                    }
                    showConfirmDialog(getString(R.string.relapse), getString(R.string.relapse_confirm, selectedAddiction.name), action)
                } else {
                    val action: () -> Unit = {
                        selectedAddiction.lastRelapse = Instant.now()
                        selectedAddiction.history[System.currentTimeMillis()] = 0
                        this.notifyItemChanged(it)
                        cacheHandler.writeCache()
                    }
                    showConfirmDialog(getString(R.string.track_now), getString(R.string.start_tracking_now, selectedAddiction.name), action)
                }
            }
            setStopButtonAction {
                val selectedAddiction = addictions[it]
                if (!selectedAddiction.isFuture()) {
                    if (selectedAddiction.isStopped)
                        Snackbar.make(binding.root, getString(R.string.already_stopped, selectedAddiction.name), BaseTransientBottomBar.LENGTH_SHORT).show()
                    else {
                        val action: () -> Unit = {
                            selectedAddiction.stopAbstaining()
                            this.notifyItemChanged(it)
                            cacheHandler.writeCache()
                        }
                        showConfirmDialog(getString(R.string.stop), getString(R.string.stop_confirm, selectedAddiction.name), action)
                    }
                } else Snackbar.make(binding.root, getString(R.string.not_tracked_yet, selectedAddiction.name), BaseTransientBottomBar.LENGTH_SHORT).show()
            }
            setTimelineButtonAction {
                val selectedAddiction = addictions[it]
                if (!selectedAddiction.isFuture()) {
                    val intent = Intent(this@Main, Timeline::class.java)
                        .putExtra(EXTRA_ADDICTION_POSITION, it)
                    startActivity(intent)
                } else Snackbar.make(binding.root, getString(R.string.not_tracked_yet, selectedAddiction.name), BaseTransientBottomBar.LENGTH_SHORT).show()
            }
            setPriorityTextViewAction {
                val selectedAddiction = addictions[it]
                var choice = selectedAddiction.priority.ordinal
                MaterialAlertDialogBuilder(this@Main)
                    .setTitle(R.string.edit_priority)
                    .setSingleChoiceItems(R.array.priorities, selectedAddiction.priority.ordinal) { _, which -> choice = which }
                    .setPositiveButton(R.string.edit) { _, _ ->
                        selectedAddiction.priority = Addiction.Priority.values()[choice]
                        addictions.sortWith { a1, a2 ->
                            a1.priority.compareTo(a2.priority)
                        }
                        cacheHandler.writeCache()
                        adapterAddictions.notifyDataSetChanged()
                        Snackbar.make(binding.root, R.string.edit_priority_success, BaseTransientBottomBar.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
            setMiscButtonAction { int ->
                var dialogViewBinding: DialogMiscBinding? = DialogMiscBinding.inflate(layoutInflater)
                val dialog = BottomSheetDialog(this@Main)
                dialogViewBinding!!.dailyNotes.setOnClickListener {
                    startActivity(Intent(this@Main, DailyNotes::class.java)
                        .putExtra(EXTRA_ADDICTION_POSITION, int)
                    )
                    dialog.dismiss()
                }
                dialogViewBinding.savings.setOnClickListener {
                    startActivity(Intent(this@Main, Savings::class.java)
                        .putExtra(EXTRA_ADDICTION_POSITION, int)
                    )
                    dialog.dismiss()
                }
                dialogViewBinding.milestones.setOnClickListener {
                    startActivity(Intent(this@Main, Milestones::class.java)
                        .putExtra(EXTRA_ADDICTION_POSITION, int)
                    )
                    dialog.dismiss()
                }
                dialog.setContentView(dialogViewBinding.root)
                dialog.setOnDismissListener { dialogViewBinding = null }
                dialog.show()
            }
        }
        binding.recyclerAddictions.layoutManager = LinearLayoutManager(this)
        binding.recyclerAddictions.adapter = adapterAddictions
        //main handler to refresh all cards in sync
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.postDelayed(object : Runnable {
            @SuppressLint("NotifyDataSetChanged")
            override fun run() {
                //Skip the refresh, when a delete was initiated < 1 second ago, to not reset delete animation
                if (!deleting) {
                    //if the future time has elapsed, insert that time into history.
                    //Note that to handle cases where the time elapses while the app is open,
                    //i have to do this catch-all check. it's ugly, but it works.
                    for (addiction in addictions) {
                        if (addiction.history.isEmpty() && addiction.lastRelapse.epochSecond < Instant.now().epochSecond) {
                            addiction.history[addiction.lastRelapse.toEpochMilli()] = 0
                        }
                    }
                    adapterAddictions.notifyDataSetChanged()
                } else {
                    cacheHandler.writeCache()
                    deleting = false
                }
                mainHandler.postDelayed(this, 1000L)
            }
        }, 1000L)
    }

    private fun updatePromptVisibility() {
        binding.prompt.visibility = if (addictions.size == 0) View.VISIBLE else View.GONE
    }

    private fun newCardDialog() {
        //Pass current addiction names to create activity, to prevent creation of elements with identical keys
        val addictionNames = arrayListOf<String>()
        addictions.forEach { addictionNames.add(it.name) }
        val intent = Intent(this, Create::class.java)
            .putStringArrayListExtra(EXTRA_NAMES, addictionNames)
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
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}