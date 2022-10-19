package com.sixtyninefourtwenty.imdefinitelysober.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.sixtyninefourtwenty.imdefinitelysober.Addiction
import com.sixtyninefourtwenty.imdefinitelysober.AddictionCardAdapter
import com.sixtyninefourtwenty.imdefinitelysober.R
import com.sixtyninefourtwenty.imdefinitelysober.databinding.ActivityMainBinding
import com.sixtyninefourtwenty.imdefinitelysober.internal.CacheHandler
import com.sixtyninefourtwenty.imdefinitelysober.utils.showConfirmDialog
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.time.Instant
import java.util.*

class Main : AppCompatActivity() {

    companion object {
        const val EXTRA_NAMES = "com.sixtyninefourtwenty.imdefinitelysober.EXTRA_NAMES"
        val addictions = ArrayList<Addiction>()
        var deleting = false
    }

    private lateinit var adapterAddictions: AddictionCardAdapter
    private lateinit var cacheHandler: CacheHandler
    private lateinit var binding: ActivityMainBinding
    private val mainScope = MainScope()
    @SuppressLint("NotifyDataSetChanged")
    private val addNewAddiction = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val name = it.data?.extras?.getString("name") as String
            @Suppress("DEPRECATION") val instant =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    it.data?.extras?.getSerializable("instant", Instant::class.java) as Instant
                else it.data?.extras?.getSerializable("instant") as Instant
            val addiction = Addiction(name, instant, false, 0)
            addictions.add(addiction)
            cacheHandler.writeCache()
            adapterAddictions.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
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
                val viewHolder = it.tag as ViewHolder
                val pos = viewHolder.adapterPosition
                val action: () -> Unit = {
                    addictions.remove(addictions[pos])
                    this.notifyItemRemoved(pos)
                    deleting = true
                    cacheHandler.writeCache()
                }
                showConfirmDialog(getString(R.string.delete), getString(R.string.delete_confirm, addictions[pos].name), action)
            }
            setOnButtonRelapseClickListener {
                val viewHolder = it.tag as ViewHolder
                val pos = viewHolder.adapterPosition
                val action: () -> Unit = {
                    addictions[pos].relapse()
                    addictions[pos].isStopped = false
                    this.notifyItemChanged(pos)
                    cacheHandler.writeCache()
                }
                showConfirmDialog(getString(R.string.relapse), getString(R.string.relapse_confirm, addictions[pos].name), action)
            }
            setOnButtonStopClickListener {
                val viewHolder = it.tag as ViewHolder
                val pos = viewHolder.adapterPosition
                if (addictions[pos].isStopped)
                    Snackbar.make(binding.root, getString(R.string.already_stopped, addictions[pos].name), BaseTransientBottomBar.LENGTH_SHORT).show()
                else {
                    val action: () -> Unit = {
                        addictions[pos].isStopped = true
                        addictions[pos].timeStopped = System.currentTimeMillis()
                        this.notifyItemChanged(pos)
                        cacheHandler.writeCache()
                    }
                    showConfirmDialog(getString(R.string.stop), getString(R.string.stop_confirm, addictions[pos].name), action)
                }
            }
        }
        val recyclerAddictions = findViewById<RecyclerView>(R.id.recyclerAddictions)
        val layoutManager = LinearLayoutManager(this)
        recyclerAddictions.layoutManager = layoutManager
        recyclerAddictions.adapter = adapterAddictions

        mainScope.launch {
            while (true) {
                if (!deleting) {
                    adapterAddictions.notifyDataSetChanged()
                } else {
                    cacheHandler.writeCache()
                    deleting = false
                }
                updatePromptVisibility()
                delay(1000)
            }
        }
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

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}