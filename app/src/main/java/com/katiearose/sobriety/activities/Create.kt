package com.katiearose.sobriety.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.katiearose.sobriety.R
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class Create : AppCompatActivity() {
    private lateinit var createButton: Button
    private lateinit var datePickerButton: ConstraintLayout
    private lateinit var timePickerButton: ConstraintLayout
    private lateinit var textInputLayout: TextInputLayout
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

        textInputLayout = findViewById(R.id.til)
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
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.pick_starting_date)
            .setCalendarConstraints(CalendarConstraints.Builder().setEnd(System.currentTimeMillis()).build())
            .build()
        datePicker.addOnPositiveButtonClickListener {
            if (it > System.currentTimeMillis())
                Snackbar.make(findViewById(R.id.clCreate), R.string.error_future_date, LENGTH_SHORT).show()
            else {
                startDateTime = ZonedDateTime.of(
                    Date(it).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    startDateTime.toLocalTime(),
                    ZoneId.systemDefault()
                )
                dateView.text = startDateTime.toLocalDate().toString()
            }
        }
        datePicker.show(supportFragmentManager, null)
    }

    private fun pickTime() {
        val isToday = startDateTime.toLocalDate() == ZonedDateTime.now().toLocalDate()
        val timePicker = MaterialTimePicker.Builder()
            .setTitleText(R.string.pick_starting_time)
            .setHour(ZonedDateTime.now().hour)
            .setMinute(ZonedDateTime.now().minute)
            .build()
        timePicker.addOnPositiveButtonClickListener {
            if ((timePicker.hour > ZonedDateTime.now().hour ||
                (timePicker.hour == ZonedDateTime.now().hour && timePicker.minute > ZonedDateTime.now().minute)) &&
                isToday)
                Snackbar.make(findViewById(R.id.clCreate), R.string.error_future_time, LENGTH_SHORT).show()
            else {
                startDateTime = ZonedDateTime.of(
                    startDateTime.toLocalDate(),
                    LocalTime.of(timePicker.hour, timePicker.minute),
                    ZoneId.systemDefault()
                )
                timeView.text = startDateTime.toLocalTime().toString()
            }
        }
        timePicker.show(supportFragmentManager, null)
    }

    private fun create() {
        val name = nameEntry.text.toString().trim()
        val nameExists = names.contains(name)

        //Don't allow creating without a name, or with a duplicate name
        if (name.isEmpty() || nameExists) {
            textInputLayout.error = if (name.isEmpty()) getString(R.string.error_empty_name) else getString(
                R.string.error_duplicate_entry
            )
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