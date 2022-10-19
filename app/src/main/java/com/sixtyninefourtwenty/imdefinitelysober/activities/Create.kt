package com.sixtyninefourtwenty.imdefinitelysober.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.sixtyninefourtwenty.imdefinitelysober.R
import com.sixtyninefourtwenty.imdefinitelysober.databinding.ActivityCreateBinding
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Create : AppCompatActivity() {

    private lateinit var startDateTime: ZonedDateTime
    private lateinit var names: ArrayList<String>
    private lateinit var binding: ActivityCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.clPickDate.setOnClickListener { pickDate() }
        binding.clPickTime.setOnClickListener { pickTime() }

        binding.btnCreate.setOnClickListener { create() }

        startDateTime = ZonedDateTime.now(ZoneId.systemDefault())

        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        binding.tvDate.text = startDateTime.toLocalDate().toString()
        binding.tvTime.text = startDateTime.toLocalTime().format(formatter).toString()

        names = intent.getStringArrayListExtra(Main.EXTRA_NAMES) as ArrayList<String>
    }

    @Deprecated("Deprecated in Java")
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
                Snackbar.make(binding.root, R.string.error_future_date, LENGTH_SHORT).show()
            else {
                startDateTime = ZonedDateTime.of(
                    Date(it).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    startDateTime.toLocalTime(),
                    ZoneId.systemDefault()
                )
                binding.tvDate.text = startDateTime.toLocalDate().toString()
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
                Snackbar.make(binding.root, R.string.error_future_time, LENGTH_SHORT).show()
            else {
                startDateTime = ZonedDateTime.of(
                    startDateTime.toLocalDate(),
                    LocalTime.of(timePicker.hour, timePicker.minute),
                    ZoneId.systemDefault()
                )
                binding.tvTime.text = startDateTime.toLocalTime().toString()
            }
        }
        timePicker.show(supportFragmentManager, null)
    }

    private fun create() {
        val name = binding.etTitle.text.toString().trim()
        val nameExists = names.contains(name)

        //Don't allow creating without a name, or with a duplicate name
        if (name.isEmpty() || nameExists) {
            binding.til.error = if (name.isEmpty()) getString(R.string.error_empty_name) else getString(
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