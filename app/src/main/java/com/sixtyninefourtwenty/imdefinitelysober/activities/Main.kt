package com.sixtyninefourtwenty.imdefinitelysober.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sixtyninefourtwenty.imdefinitelysober.Addiction
import com.sixtyninefourtwenty.imdefinitelysober.AddictionCardAdapter
import com.sixtyninefourtwenty.imdefinitelysober.R
import com.sixtyninefourtwenty.imdefinitelysober.databinding.ActivityMainBinding
import com.sixtyninefourtwenty.imdefinitelysober.internal.CacheHandler
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
    private val createCardRequestCode = 1
    private lateinit var binding: ActivityMainBinding
    private val mainScope = MainScope()

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
        adapterAddictions = AddictionCardAdapter(this, cacheHandler)
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

    //i'll handle this later
    @Suppress("DEPRECATION")
    private fun newCardDialog() {
        //Pass current addiction names to create activity, to prevent creation of elements with identical keys
        val addictionNames = arrayListOf<String>()
        addictions.forEach {
            addictionNames.add(it.name)
        }
        val intent = Intent(this, Create::class.java)
        intent.putStringArrayListExtra(EXTRA_NAMES, addictionNames)
        startActivityForResult(intent, createCardRequestCode)
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

    //i'll handle this later
    @SuppressLint("NotifyDataSetChanged")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == createCardRequestCode && resultCode == RESULT_OK) {
            val name = data?.extras?.get("name") as String
            val instant = data.extras?.get("instant") as Instant
            val addiction = Addiction(name, instant)
            addictions.add(addiction)
            cacheHandler.writeCache()
            adapterAddictions.notifyDataSetChanged()
        }
    }
}