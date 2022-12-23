package com.katiearose.sobriety.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.katiearose.sobriety.Addiction
import com.katiearose.sobriety.NoteAdapter
import com.katiearose.sobriety.R
import com.katiearose.sobriety.databinding.ActivityDailyNotesBinding
import com.katiearose.sobriety.databinding.DialogAddNoteBinding
import com.katiearose.sobriety.internal.CacheHandler
import com.katiearose.sobriety.utils.applyThemes
import com.katiearose.sobriety.utils.isInputEmpty
import com.katiearose.sobriety.utils.showConfirmDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DailyNotes : AppCompatActivity() {

    private lateinit var binding: ActivityDailyNotesBinding
    private lateinit var adapter: NoteAdapter
    private val dateFormat = DateTimeFormatter.ofPattern("MMMM dd yyyy")
    private lateinit var addiction: Addiction
    private lateinit var cacheHandler: CacheHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemes()
        super.onCreate(savedInstanceState)
        binding = ActivityDailyNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cacheHandler = CacheHandler(this)

        val addictionPosition = intent.extras!!.getSerializable(Main.EXTRA_ADDICTION_POSITION) as Int
        addiction = Main.addictions[addictionPosition]

        adapter = NoteAdapter(addiction, this, { showAddNoteDialog(true, it.first) },
            {
                val action: () -> Unit = {
                    addiction.dailyNotes.remove(it.first)
                    updateNotesList()
                }
                showConfirmDialog(getString(R.string.delete), getString(R.string.delete_note_confirm, dateFormat.format(it.first)), action)
            })
        binding.notesList.layoutManager = LinearLayoutManager(this)
        binding.notesList.adapter = adapter

        binding.addNoteFab.setOnClickListener { showAddNoteDialog(false, LocalDate.now()) }
    }

    private fun showAddNoteDialog(isEdit: Boolean, date: LocalDate) {
        var pickedDate = date
        var dialogViewBinding: DialogAddNoteBinding? = DialogAddNoteBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogViewBinding!!.root)
        if (isEdit) {
            dialogViewBinding.dateStr.visibility = View.GONE
            dialogViewBinding.noteDate.visibility = View.GONE
            dialogViewBinding.noteInput.setText(addiction.dailyNotes[date])
        } else {
            dialogViewBinding.noteDate.text = dateFormat.format(pickedDate)
            dialogViewBinding.noteDate.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker().build()
                datePicker.addOnPositiveButtonClickListener {
                    pickedDate =
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    dialogViewBinding!!.noteDate.text = dateFormat.format(pickedDate)
                }
                datePicker.show(supportFragmentManager, null)
            }
        }
        dialogViewBinding.btnSave.setOnClickListener {
            if (dialogViewBinding!!.noteInput.isInputEmpty()) {
                dialogViewBinding!!.noteInputLayout.error = getString(R.string.error_empty_note)
            } else {
                addiction.dailyNotes[pickedDate] = dialogViewBinding!!.noteInput.text.toString()
                updateNotesList()
                dialog.dismiss()
            }
        }
        dialog.setOnDismissListener { dialogViewBinding = null }
        dialog.show()
    }

    private fun updateNotesList() {
        cacheHandler.writeCache()
        adapter.update()
    }
}