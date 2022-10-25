package com.katiearose.sobriety.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.katiearose.sobriety.Addiction
import com.katiearose.sobriety.NoteAdapter
import com.katiearose.sobriety.R
import com.katiearose.sobriety.databinding.ActivityDailyNotesBinding
import com.katiearose.sobriety.databinding.DialogAddNoteBinding
import com.katiearose.sobriety.internal.CacheHandler
import com.katiearose.sobriety.utils.applyThemes
import com.katiearose.sobriety.utils.getKeyValuePairAtIndex
import com.katiearose.sobriety.utils.showConfirmDialog
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

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

        val pos = intent.extras!!.getInt(Main.EXTRA_ADDICTION_POSITION)
        addiction = Main.addictions[pos]
        adapter = NoteAdapter()
        adapter.apply {
            setNotes(addiction.dailyNotes)
            setOnButtonEditClickListener {
                val viewHolder = it.tag as RecyclerView.ViewHolder
                val pos = viewHolder.adapterPosition
                showAddNoteDialog(true, addiction.dailyNotes.getKeyValuePairAtIndex(pos).first)
            }
            setOnButtonExpandCollapseClickListener {
                val viewHolder = it.tag as RecyclerView.ViewHolder
                val actualViewHolder = viewHolder as NoteAdapter.NoteViewHolder
                val card = actualViewHolder.card
                card.visibility = if (card.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                actualViewHolder.expandCollapseButton.apply {
                    setImageResource(if (card.visibility == View.VISIBLE) R.drawable.expand_less_24px else R.drawable.expand_more_24px)
                    contentDescription = if (card.visibility == View.VISIBLE) getString(R.string.collapse) else getString(R.string.expand)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        tooltipText = if (card.visibility == View.VISIBLE) getString(R.string.collapse) else getString(R.string.expand)
                }
            }
            setOnButtonDeleteClickListener {
                val viewHolder = it.tag as RecyclerView.ViewHolder
                val pos = viewHolder.adapterPosition
                val action: () -> Unit = {
                    addiction.dailyNotes.remove(addiction.dailyNotes.getKeyValuePairAtIndex(pos).first)
                    updateNotesList()
                }
                showConfirmDialog(getString(R.string.delete), getString(R.string.delete_note_confirm, dateFormat.format(addiction.dailyNotes.getKeyValuePairAtIndex(pos).first)), action)
            }
        }
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
                    pickedDate = Date(it).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    dialogViewBinding!!.noteDate.text = dateFormat.format(pickedDate)
                }
                datePicker.show(supportFragmentManager, null)
            }
        }
        dialogViewBinding.btnSave.setOnClickListener {
            if (dialogViewBinding!!.noteInput.text == null || dialogViewBinding!!.noteInput.text.toString().isEmpty()) {
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
        val sorted = addiction.dailyNotes.toSortedMap()
        addiction.dailyNotes.clear()
        addiction.dailyNotes.putAll(sorted)
        cacheHandler.writeCache()
        adapter.setNotes(addiction.dailyNotes)
    }
}