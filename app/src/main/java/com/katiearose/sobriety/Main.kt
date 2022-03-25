package com.katiearose.sobriety

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.Instant
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream


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
    }

    private lateinit var addCardButton: FloatingActionButton
    private lateinit var cardHolder: LinearLayout
    private lateinit var prompt: TextView

    private val addictions = ArrayList<Addiction>()
    private val createCardRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addCardButton = findViewById(R.id.addCardButton)
        addCardButton.setOnClickListener { newCardDialog() }
        cardHolder = findViewById(R.id.container)
        prompt = findViewById(R.id.prompt)
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(16, 16, 16, 16)
        try {
            this.openFileInput("Sobriety.cache").use {
                readCache(it)
            }
        } catch (e: FileNotFoundException) {
        }
        updatePromptVisibility()
    }

    private fun updatePromptVisibility() {
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

    private fun createNewCard(addiction: Addiction) {
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(16, 16, 16, 16)
        val cardView = CardView(this)
        cardView.radius = 15f
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.cardColor, typedValue, true)
        cardView.setCardBackgroundColor(typedValue.data)
        cardView.setContentPadding(36, 36, 36, 36)
        cardView.layoutParams = params
        cardView.cardElevation = 30f
        val title = TextView(this)
        title.textSize = 24F
        title.text = addiction.name
        title.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        val timeRunning = TextView(this)
        timeRunning.textSize = 16F
        timeRunning.text = secondsToString(timeSinceInstant(addiction.lastRelapse))
        val average = TextView(this)
        average.textSize = 12F
        average.text = "Average: ${secondsToString(addiction.averageRelapseDuration)}"
        val buttons = LinearLayout(this)
        buttons.orientation = LinearLayout.HORIZONTAL
        val resetButton = Button(this)
        resetButton.text = "Reset"
        val deleteButton = Button(this)
        deleteButton.text = "Delete"

        deleteButton.setOnClickListener {
            val action: () -> Unit = {
                cardHolder.removeView(cardView)
                addictions.remove(addiction)
                updatePromptVisibility()
            }
            dialogConfirm("Delete entry \"${addiction.name}\" ?", action)
        }

        resetButton.setOnClickListener {
            val action: () -> Unit = {
                addiction.relapse()
                average.text =
                    "Average: ${secondsToString(addiction.averageRelapseDuration)}"
            }
            dialogConfirm("Reset entry \"${addiction.name}\" ?", action)
        }

        val cardLinearLayout = LinearLayout(this)
        cardLinearLayout.orientation = LinearLayout.VERTICAL
        cardLinearLayout.addView(title)
        cardLinearLayout.addView(timeRunning)
        cardLinearLayout.addView(average)
        buttons.addView(resetButton)
        buttons.addView(deleteButton)
        cardLinearLayout.addView(buttons)
        cardView.addView(cardLinearLayout)
        cardHolder.addView(cardView)
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.postDelayed(object : Runnable {
            override fun run() {
                if (!addictions.contains(addiction)) {
                    return
                }
                timeRunning.text = secondsToString(timeSinceInstant(addiction.lastRelapse))
                mainHandler.postDelayed(this, 1000L)
            }
        }, 1000L)
    }

    private fun dialogConfirm(title: String, confirmAction: () -> Unit) {
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

    private fun readCache(input: InputStream) {
        val cache = input.readBytes()
        try {
            InflaterInputStream(cache.inputStream()).use { iis ->
                ObjectInputStream(iis).use {
                    addictions.addAll(it.readObject() as ArrayList<Addiction>)
                }
            }
            for (addiction in addictions) {
                createNewCard(addiction)
            }
        } catch (e: ClassCastException) {
            readCache(cache.inputStream())
        }
    }

    @Deprecated(
        "For old caches before the implementation of the Addiction class.",
        ReplaceWith("readCache(FileInputStream)"),
        DeprecationLevel.WARNING
    )
    private fun readLegacyCache(input: InputStream) {
        val a = HashMap<String, Pair<Instant, CircularBuffer<Long>>>()
        InflaterInputStream(input).use { iis ->
            ObjectInputStream(iis).use {
                for (i in it.readObject() as HashMap<String, Pair<Instant, CircularBuffer<Long>>>) {
                    a[i.key] = i.value
                }
            }
        }
        for (ad in a) {
            val addiction = Addiction(ad.key, ad.value.first)
            createNewCard(addiction)
        }
    }

    private fun writeCache() {
        this.openFileOutput("Sobriety.cache", MODE_PRIVATE).use { fos ->
            DeflaterOutputStream(fos, true).use { dos ->
                ObjectOutputStream(dos).use {
                    it.writeObject(addictions)
                }
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        writeCache()
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
                createNewCard(addiction)
            }
        }
    }
}