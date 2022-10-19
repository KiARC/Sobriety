package com.sixtyninefourtwenty.imdefinitelysober.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sixtyninefourtwenty.imdefinitelysober.Addiction
import com.sixtyninefourtwenty.imdefinitelysober.AddictionCardAdapter
import com.sixtyninefourtwenty.imdefinitelysober.internal.CacheHandler
import com.sixtyninefourtwenty.imdefinitelysober.R
import java.io.FileNotFoundException
import java.time.Instant
import java.util.*

class Main : AppCompatActivity() {

    companion object {
        const val EXTRA_NAMES = "com.sixtyninefourtwenty.imdefinitelysober.EXTRA_NAMES"
        private const val MINUTE = 60
        private const val HOUR = MINUTE * 60
        private const val DAY = HOUR * 24
        private const val WEEK = DAY * 7
        private const val MONTH = DAY * 31
        private const val YEAR = MONTH * 12
        fun secondsToString(given: Long, context: Context): String {
            var time = given
            val s = time % MINUTE
            time -= s
            val m = (time % HOUR) / MINUTE
            time -= m * MINUTE
            val h = (time % DAY) / HOUR
            time -= h * HOUR
            val d = (time % WEEK) / DAY
            time -= d * DAY
            val w = (time % MONTH) / WEEK
            time -= w * WEEK
            val mo = (time % YEAR) / MONTH
            time -= mo * MONTH
            val y = time / YEAR
            val stringBuilder = StringBuilder()
            if (y != 0L) stringBuilder.append(context.getString(R.string.years))
            if (mo != 0L) stringBuilder.append(context.getString(R.string.months))
            if (w != 0L) stringBuilder.append(context.getString(R.string.weeks))
            if (d != 0L) stringBuilder.append(context.getString(R.string.days))
            if (h != 0L) stringBuilder.append(context.getString(R.string.hours))
            if (m != 0L) stringBuilder.append(context.getString(R.string.minutes))
            if (!(y == 0L && mo == 0L && w == 0L && d == 0L && h == 0L && m == 0L)) stringBuilder.append(
                context.getString(R.string.and)
            )
            stringBuilder.append(context.getString(R.string.seconds))
            return stringBuilder.toString()
        }

        fun timeSinceInstant(given: Instant) = Instant.now().epochSecond - given.epochSecond

        val addictions = ArrayList<Addiction>()
        var deleting = false
    }

    private lateinit var addCardButton: FloatingActionButton
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
        prompt.visibility = if (addictions.size == 0) View.VISIBLE else View.GONE
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

    fun dialogConfirm(title: String, confirmAction: () -> Unit) {
        this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->
                    confirmAction()
                }
                setNegativeButton(android.R.string.cancel, null)
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