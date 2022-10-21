package com.katiearose.sobriety.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.katiearose.sobriety.Addiction
import com.katiearose.sobriety.AddictionCardAdapter
import com.katiearose.sobriety.R
import com.katiearose.sobriety.databinding.ActivityMainBinding
import com.katiearose.sobriety.internal.CacheHandler
import com.katiearose.sobriety.utils.showConfirmDialog
import java.io.FileNotFoundException
import java.time.Instant
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
            val addiction = Addiction(name, instant, false, 0, LinkedHashMap(), priority)
            addiction.history[instant.toEpochMilli()] = 0
            addictions.add(addiction)
            addictions.sortWith { a1, a2 ->
                a1.priority.compareTo(a2.priority)
            }
            cacheHandler.writeCache()
            adapterAddictions.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
            setOnButtonDeleteClickListener {
                val viewHolder = it.tag as RecyclerView.ViewHolder
                val pos = viewHolder.adapterPosition
                val action: () -> Unit = {
                    addictions.remove(addictions[pos])
                    updatePromptVisibility()
                    this.notifyItemRemoved(pos)
                    deleting = true
                    cacheHandler.writeCache()
                }
                showConfirmDialog(getString(R.string.delete), getString(R.string.delete_confirm, addictions[pos].name), action)
            }
            setOnButtonRelapseClickListener {
                val viewHolder = it.tag as RecyclerView.ViewHolder
                val pos = viewHolder.adapterPosition
                val action: () -> Unit = {
                    addictions[pos].relapse()
                    this.notifyItemChanged(pos)
                    cacheHandler.writeCache()
                }
                showConfirmDialog(getString(R.string.relapse), getString(R.string.relapse_confirm, addictions[pos].name), action)
            }
            setOnButtonStopClickListener {
                val viewHolder = it.tag as RecyclerView.ViewHolder
                val pos = viewHolder.adapterPosition
                if (addictions[pos].isStopped)
                    Snackbar.make(binding.root, getString(R.string.already_stopped, addictions[pos].name), BaseTransientBottomBar.LENGTH_SHORT).show()
                else {
                    val action: () -> Unit = {
                        addictions[pos].stopAbstaining()
                        this.notifyItemChanged(pos)
                        cacheHandler.writeCache()
                    }
                    showConfirmDialog(getString(R.string.stop), getString(R.string.stop_confirm, addictions[pos].name), action)
                }
            }
            setOnTimelineButtonClickListener {
                val viewHolder = it.tag as RecyclerView.ViewHolder
                val pos = viewHolder.adapterPosition
                val intent = Intent(this@Main, Timeline::class.java)
                    .putExtra(EXTRA_ADDICTION_POSITION, pos)
                startActivity(intent)
            }
            setOnPriorityTextViewClickListener {
                val viewHolder = it.tag as RecyclerView.ViewHolder
                val pos = viewHolder.adapterPosition
                var choice = addictions[pos].priority.ordinal
                MaterialAlertDialogBuilder(this@Main)
                    .setTitle(R.string.edit_priority)
                    .setSingleChoiceItems(R.array.priorities, addictions[pos].priority.ordinal) { _, which -> choice = which }
                    .setPositiveButton(R.string.edit) { _, _ ->
                        addictions[pos].priority = Addiction.Priority.values()[choice]
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
        }
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerAddictions.layoutManager = layoutManager
        binding.recyclerAddictions.adapter = adapterAddictions
        //main handler to refresh all cards in sync
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.postDelayed(object : Runnable {
            @SuppressLint("NotifyDataSetChanged")
            override fun run() {
                //Skip the refresh, when a delete was initiated < 1 second ago, to not reset delete animation
                if (!deleting) {
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
}