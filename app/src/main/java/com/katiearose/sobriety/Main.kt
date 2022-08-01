package com.katiearose.sobriety

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.FileNotFoundException
import java.time.Instant


class Main : AppCompatActivity() {

    companion object {
        const val EXTRA_NAMES = "com.katiearose.sobriety.EXTRA_NAMES"
        fun secondsToString(given: Long): String {
            var time = given
            val s = time % 60
            time -= s
            val m = (time % 3600) / 60
            time -= m * 60
            val h = (time % 86400) / 3600
            time -= h * 3600
            val d = time / 86400
            return "$d days, $h hours, $m minutes and $s seconds"
        }

        fun timeSinceInstant(given: Instant) = Instant.now().epochSecond - given.epochSecond

        val addictions = ArrayList<Addiction>()
        var deleting = false;
    }

    private lateinit var addCardButton: FloatingActionButton
    private lateinit var cardHolder: LinearLayout
    private lateinit var prompt: TextView

    private lateinit var adapterAddictions: AddictionCardAdapter
    private lateinit var cacheHandler: CacheHandler
    private val createCardRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addCardButton = findViewById(R.id.addCardButton)
        addCardButton.setOnClickListener { newCardDialog() }
        prompt = findViewById(R.id.prompt)
        cacheHandler = CacheHandler(this)
        try {
            this.openFileInput("Sobriety.cache").use {
                addictions.addAll(cacheHandler.readCache(it))
            }
        } catch (e: FileNotFoundException) {
        }

        updatePromptVisibility()

        //Create adapter, and layout manager for recyclerview and attach them
        adapterAddictions = AddictionCardAdapter(this)
        val recyclerAddictions = findViewById<RecyclerView>(R.id.recyclerAddictions)
        val layoutManager = LinearLayoutManager(this)
        recyclerAddictions.layoutManager = layoutManager
        recyclerAddictions.adapter = adapterAddictions
        //main handler to refresh all cards in sync
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.postDelayed(object : Runnable {
            @SuppressLint("NotifyDataSetChanged")
            override fun run() {
                //Skip the refresh, when a delete was initiated < 1 second ago, to not reset delete animation
                if(!deleting){
                    adapterAddictions.notifyDataSetChanged()
                }else{
                    cacheHandler.writeCache()
                    deleting = false
                }
                mainHandler.postDelayed(this, 1000L)
            }
        }, 1000L)
    }

    fun updatePromptVisibility() {
        prompt.visibility = when (addictions.size == 0) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

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

    fun dialogConfirm(title: String, confirmAction: () -> Unit) {
        this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(
                    "ok"
                ) { _, _ ->
                    confirmAction()
                }
                setNegativeButton("cancel") { _: DialogInterface, _: Int -> }
            }
            builder.setTitle(title)
            builder.create()
            builder.show()
        }
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        writeCache()
//    }

    /**
     * This gets called once the Create Activity is closed (Necessary to hide the prompt in case
     * a first addiction was added to the list.
     */
    override fun onResume() {
        updatePromptVisibility()
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == createCardRequestCode) {
            if (resultCode == RESULT_OK) {
                val name = data?.extras?.get("name") as String
                val instant = data.extras?.get("instant") as Instant
                val addiction = Addiction(name, instant)
                addictions.add(addiction)
                cacheHandler.writeCache()
                adapterAddictions.notifyDataSetChanged()
            }
        }
    }
}