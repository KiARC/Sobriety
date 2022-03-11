package com.katiearose.sobriety

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime


class Create : AppCompatActivity() {
    private lateinit var createButton: Button
    private lateinit var datePickerButton: Button
    private lateinit var timePickerButton: Button
    private lateinit var cancelButton: Button
    private lateinit var nameEntry: TextView
    private lateinit var dateView: TextView
    private lateinit var timeView: TextView
    private lateinit var startDateTime: ZonedDateTime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        createButton = findViewById(R.id.createButton)
        datePickerButton = findViewById(R.id.datePickerButton)
        timePickerButton = findViewById(R.id.timePickerButton)
        cancelButton = findViewById(R.id.cancelButton)
        nameEntry = findViewById(R.id.nameEntry)
        dateView = findViewById(R.id.dateViewer)
        timeView = findViewById(R.id.timeViewer)
        datePickerButton.setOnClickListener { pickDate() }
        timePickerButton.setOnClickListener { pickTime() }
        cancelButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED, Intent())
            finish()
        }
        createButton.setOnClickListener { create() }
        startDateTime = ZonedDateTime.now(ZoneId.systemDefault())
        dateView.text = startDateTime.toLocalDate().toString()
        timeView.text = startDateTime.toLocalTime().toString()
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
        val instant = startDateTime.toInstant()
        val intent = Intent()
        intent.putExtra("instant", instant)
        intent.putExtra("name", nameEntry.text.toString())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}