package com.katiearose.sobriety

import android.content.Context
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.katiearose.sobriety.databinding.ListItemNoteBinding
import com.katiearose.sobriety.utils.getSharedPref
import com.katiearose.sobriety.utils.getSortNotesPref
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NoteAdapter(private val addiction: Addiction, context: Context):
    ListAdapter<Pair<LocalDate, String>, NoteAdapter.NoteViewHolder>(object : DiffUtil.ItemCallback<Pair<LocalDate, String>>() {
        override fun areItemsTheSame(
            oldItem: Pair<LocalDate, String>,
            newItem: Pair<LocalDate, String>
        ): Boolean {
            return oldItem.first == newItem.first
        }

        override fun areContentsTheSame(
            oldItem: Pair<LocalDate, String>,
            newItem: Pair<LocalDate, String>
        ): Boolean {
            return oldItem.second == newItem.second
        }

    }) {
    private val preferences = context.getSharedPref()
    init { update() }
    private val dateFormat = DateTimeFormatter.ofPattern("MMMM dd yyyy")

    private lateinit var editButtonAction: (Int) -> Unit
    private lateinit var onButtonExpandCollapseClickListener: OnClickListener
    private lateinit var deleteButtonAction: (Int) -> Unit

    fun update() {
        submitList(when (preferences.getSortNotesPref()) {
            "asc" -> addiction.dailyNotes.toList().sortedWith { n1, n2 ->
                n1.first.compareTo(n2.first)
            }
            "desc" -> addiction.dailyNotes.toList().sortedWith { n1, n2 ->
                n2.first.compareTo(n1.first)
            }
            else -> addiction.dailyNotes.toList()
        })
    }

    fun setEditButtonAction(editButtonAction: (Int) -> Unit) {
        this.editButtonAction = editButtonAction
    }

    fun setOnButtonExpandCollapseClickListener(onButtonExpandCollapseClickListener: OnClickListener) {
        this.onButtonExpandCollapseClickListener = onButtonExpandCollapseClickListener
    }

    fun setDeleteButtonAction(deleteButtonAction: (Int) -> Unit) {
        this.deleteButtonAction = deleteButtonAction
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ListItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding, editButtonAction, onButtonExpandCollapseClickListener,
            deleteButtonAction)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val pair = currentList[position]
        holder.date.text = dateFormat.format(pair.first)
        holder.note.text = pair.second
    }

    class NoteViewHolder(binding: ListItemNoteBinding, editButtonAction: (Int) -> Unit,
                         onButtonExpandCollapseClickListener: OnClickListener,
                         deleteButtonAction: (Int) -> Unit): ViewHolder(binding.root) {
        val date = binding.date
        val note = binding.note
        val card = binding.noteCard
        val expandCollapseButton = binding.btnExpandCollapse.apply {
            tag = this@NoteViewHolder
            setOnClickListener(onButtonExpandCollapseClickListener)
        }
        init {
            binding.btnEdit.setOnClickListener { editButtonAction(adapterPosition) }
            binding.btnDelete.setOnClickListener { deleteButtonAction(adapterPosition) }
        }
    }
}