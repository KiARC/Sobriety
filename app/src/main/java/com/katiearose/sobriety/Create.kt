package com.katiearose.sobriety

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class Create : AppCompatActivity() {
    private lateinit var createButton: Button
    private lateinit var datePickerButton: ConstraintLayout
    private lateinit var timePickerButton: ConstraintLayout
    private lateinit var nameEntry: EditText
    private lateinit var dateView: TextView
    private lateinit var timeView: TextView
    private lateinit var startDateTime: ZonedDateTime

    private lateinit var names: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        createButton = findViewById(R.id.btnCreate)

        datePickerButton = findViewById(R.id.clPickDate)
        timePickerButton = findViewById(R.id.clPickTime)

        nameEntry = findViewById(R.id.etTitle)
        dateView = findViewById(R.id.tvDate)
        timeView = findViewById(R.id.tvTime)

        datePickerButton.setOnClickListener { pickDate() }
        timePickerButton.setOnClickListener { pickTime() }

        createButton.setOnClickListener { create() }

        startDateTime = ZonedDateTime.now(ZoneId.systemDefault())


        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        dateView.text = startDateTime.toLocalDate().toString()
        timeView.text = startDateTime.toLocalTime().format(formatter).toString()

        names = intent.getStringArrayListExtra(Main.EXTRA_NAMES) as ArrayList<String>
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, Intent())
        finish()
    }

    private fun pickDate() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Starting Date")
        val input = DatePicker(this)
        input.updateDate(startDateTime.year, startDateTime.monthValue - 1, startDateTime.dayOfMonth)
        builder.setView(input)
        builder.setPositiveButton(
            "OK"
        ) { _, _ ->
            startDateTime = ZonedDateTime.of(
                LocalDate.of(input.year, input.month + 1, input.dayOfMonth),
                startDateTime.toLocalTime(),
                ZoneId.systemDefault()
            )
            dateView.text = startDateTime.toLocalDate().toString()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun pickTime() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Starting Time")
        val input = TimePicker(this)
        builder.setView(input)
        builder.setPositiveButton(
            "OK"
        ) { _, _ ->
            if (input.validateInput()) startDateTime = ZonedDateTime.of(
                startDateTime.toLocalDate(),
                LocalTime.of(input.hour, input.minute),
                ZoneId.systemDefault()
            )
            timeView.text = startDateTime.toLocalTime().toString()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun create() {
        val name = nameEntry.text.toString().trim()
        val nameExists = names.contains(name)

        //Don't allow creating without a name, or with a duplicate name
        if (name == "" || nameExists) {
            if (nameExists){
                Snackbar.make(findViewById(R.id.clCreate),"Can't create duplicate entries", Snackbar.LENGTH_SHORT).show()
            }
            val animationShake =
                AnimationUtils.loadAnimation(this, R.anim.shake)
            nameEntry.startAnimation(animationShake)
            return
        }

        val instant = startDateTime.toInstant()
        val intent = Intent()
        intent.putExtra("instant", instant)
        intent.putExtra("name", name)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}