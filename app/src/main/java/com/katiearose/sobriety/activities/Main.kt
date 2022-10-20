package com.katiearose.sobriety.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.katiearose.sobriety.Addiction
import com.katiearose.sobriety.AddictionCardAdapter
import com.katiearose.sobriety.databinding.ActivityMainBinding
import com.katiearose.sobriety.internal.CacheHandler
import java.io.FileNotFoundException
import java.time.Instant
import java.util.*

class Main : AppCompatActivity() {

    companion object {
        const val EXTRA_NAMES = "com.katiearose.sobriety.EXTRA_NAMES"
        val addictions = ArrayList<Addiction>()
        var deleting = false
    }

    private lateinit var adapterAddictions: AddictionCardAdapter
    private lateinit var cacheHandler: CacheHandler
    private lateinit var binding: ActivityMainBinding
    @SuppressLint("NotifyDataSetChanged")
    private val addNewAddiction = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val name = it.data?.extras?.getString("name") as String
            val instant =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    it.data?.extras?.getSerializable("instant", Instant::class.java) as Instant
                else it.data?.extras?.getSerializable("instant") as Instant
            val addiction = Addiction(name, instant)
            addictions.add(addiction)
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
        adapterAddictions = AddictionCardAdapter(this, cacheHandler)
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

    fun updatePromptVisibility() {
        binding.prompt.visibility = when (addictions.size == 0) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun newCardDialog() {
        //Pass current addiction names to create activity, to prevent creation of elements with identical keys
        val addictionNames = arrayListOf<String>()
        addictions.forEach { addictionNames.add(it.name) }
        val intent = Intent(this, Create::class.java)
            .putStringArrayListExtra(EXTRA_NAMES, addictionNames)
        addNewAddiction.launch(intent)
    }

    fun dialogConfirm(title: String, confirmAction: () -> Unit) {
        this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->
                    confirmAction()
                }
                setNegativeButton(android.R.string.cancel) { _: DialogInterface, _: Int -> }
            }
            builder.setTitle(title)
            builder.create()
            builder.show()
        }
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