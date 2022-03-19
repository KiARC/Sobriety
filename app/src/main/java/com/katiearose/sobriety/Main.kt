package com.katiearose.sobriety

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
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
import java.util.zip.ZipException


class Main : AppCompatActivity() {
    private lateinit var addCardButton: FloatingActionButton
    private lateinit var cardHolder: LinearLayout
    private val addictions = HashMap<String, Instant>()
    private val createCardRequestCode = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addCardButton = findViewById(R.id.addCardButton)
        addCardButton.setOnClickListener { newCardDialog() }
        cardHolder = findViewById(R.id.container)
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
    }

    private fun newCardDialog() {
        val intent = Intent(this, Create::class.java)
        startActivityForResult(intent, createCardRequestCode)
    }

    private fun createNewCard(input: Pair<String, Instant>) {
        val name = input.first
        var date = input.second
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
        title.text = name
        title.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        val timeRunning = TextView(this)
        timeRunning.textSize = 16F
        timeRunning.text = timeSinceInstant(date)
        val buttons = LinearLayout(this)
        buttons.orientation = LinearLayout.HORIZONTAL
        val resetButton = Button(this)
        resetButton.text = "Reset"
        val deleteButton = Button(this)
        deleteButton.text = "Delete"
        var deleted = false

        deleteButton.setOnClickListener {
            val action: () -> Unit = {
                cardHolder.removeView(cardView)
                addictions.remove(name)
                deleted = true
            }
            dialogConfirm("Delete entry \"$name\" ?", action)
        }

        resetButton.setOnClickListener {
            val action: () -> Unit = {
                date = Instant.now()
            }
            dialogConfirm("Reset entry \"$name\" ?", action)
        }

        val cardLinearLayout = LinearLayout(this)
        cardLinearLayout.orientation = LinearLayout.VERTICAL
        cardLinearLayout.addView(title)
        cardLinearLayout.addView(timeRunning)
        buttons.addView(resetButton)
        buttons.addView(deleteButton)
        cardLinearLayout.addView(buttons)
        cardView.addView(cardLinearLayout)
        cardHolder.addView(cardView)
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.postDelayed(object : Runnable {
            override fun run() {
                timeRunning.text = timeSinceInstant(date)
                if (!deleted) mainHandler.postDelayed(this, 1000L)
            }
        }, 1000L)
    }

    private fun dialogConfirm(title: String, confirmAction: () -> Unit){
        this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("ok"
                ) { _, _ ->
                    confirmAction()
                }
                setNegativeButton("cancel"){ _: DialogInterface, _: Int -> }
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
                    addictions.putAll(it.readObject() as HashMap<String, Instant>)
                }
            }
        } catch (e: ZipException) {
            readLegacyCache(cache.inputStream())
        }
        for (addiction in addictions) {
            createNewCard(Pair(addiction.key, addiction.value))
        }
    }

    @Deprecated(
        "For old uncompressed caches.",
        ReplaceWith("readCache(FileInputStream)"),
        DeprecationLevel.WARNING
    )
    private fun readLegacyCache(input: InputStream) {
        ObjectInputStream(input).use {
            addictions.putAll(it.readObject() as HashMap<String, Instant>)
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

    private fun timeSinceInstant(given: Instant): String {
        var distance = Instant.now().epochSecond - given.epochSecond
        val s = distance % 60
        distance -= s
        val m = (distance % 3600) / 60
        distance -= m * 60
        val h = (distance % 86400) / 3600
        distance -= h * 3600
        val d = distance / 86400
        return "$d days, $h hours, $m minutes and $s seconds"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        writeCache()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == createCardRequestCode) {
            if (resultCode == RESULT_OK) {
                val name = data?.extras?.get("name") as String
                val instant = data.extras?.get("instant") as Instant
                addictions[name] = instant
                createNewCard(Pair(name, instant))
            }
        }
    }
}
