package com.katiearose.sobriety.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
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
import com.katiearose.sobriety.Addiction
import com.katiearose.sobriety.AddictionCardAdapter
import com.katiearose.sobriety.R
import com.katiearose.sobriety.databinding.ActivityMainBinding
import com.katiearose.sobriety.internal.CacheHandler
import java.io.FileNotFoundException
import java.time.Instant
import java.util.*

class Main : AppCompatActivity() {

    companion object {
        const val EXTRA_NAMES = "com.katiearose.sobriety.EXTRA_NAMES"
        private const val MINUTE = 60
        private const val HOUR = MINUTE * 60
        private const val DAY = HOUR * 24
        private const val WEEK = DAY * 7
        private const val MONTH = DAY * 31
        private const val YEAR = MONTH * 12
        fun secondsToString(given: Long, context: Context): String {
            if (given == -1L) return "" // -1 is returned if addiction has never been relapsed, don't bother calculating
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
            if (y != 0L) stringBuilder.append(context.getString(R.string.years, y)).append(" ")
            if (mo != 0L) stringBuilder.append(context.getString(R.string.months, mo)).append(" ")
            if (w != 0L) stringBuilder.append(context.getString(R.string.weeks, w)).append(" ")
            if (d != 0L) stringBuilder.append(context.getString(R.string.days, d)).append(" ")
            if (h != 0L) stringBuilder.append(context.getString(R.string.hours, h)).append(" ")
            if (m != 0L) stringBuilder.append(context.getString(R.string.minutes, m)).append(" ")
            if (!(y == 0L && mo == 0L && w == 0L && d == 0L && h == 0L && m == 0L)) stringBuilder.append(
                context.getString(R.string.and)
            ).append(" ")
            stringBuilder.append(context.getString(R.string.seconds, s))
            return stringBuilder.toString()
        }

        fun timeSinceInstant(given: Instant) = Instant.now().epochSecond - given.epochSecond

        val addictions = ArrayList<Addiction>()
        var deleting = false
    }
    private lateinit var adapterAddictions: AddictionCardAdapter
    private lateinit var cacheHandler: CacheHandler
    private val createCardRequestCode = 1
    private lateinit var binding: ActivityMainBinding

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