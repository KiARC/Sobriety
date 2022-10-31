package com.katiearose.sobriety

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.katiearose.sobriety.utils.getSharedPref
import com.katiearose.sobriety.utils.getSortNotesPref
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NoteAdapter(private val addiction: Addiction, context: Context): RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private val dateFormat = DateTimeFormatter.ofPattern("MMMM dd yyyy")
    private lateinit var notes: List<Pair<LocalDate, String>>
    private val preferences = context.getSharedPref()
    private lateinit var onButtonEditClickListener: OnClickListener
    private lateinit var onButtonExpandCollapseClickListener: OnClickListener
    private lateinit var onButtonDeleteClickListener: OnClickListener

    @SuppressLint("NotifyDataSetChanged")
    fun update() {
        notes = when (preferences.getSortNotesPref()) {
            "asc" -> addiction.dailyNotes.toList().sortedWith { n1, n2 ->
                n1.first.compareTo(n2.first)
            }
            "desc" -> addiction.dailyNotes.toList().sortedWith { n1, n2 ->
                n2.first.compareTo(n1.first)
            }
            else -> addiction.dailyNotes.toList()
        }
        notifyDataSetChanged()
    }

    fun getCurrentList(): List<Pair<LocalDate, String>> {
        return notes
    }

    fun setOnButtonEditClickListener(onButtonEditClickListener: OnClickListener) {
        this.onButtonEditClickListener = onButtonEditClickListener
    }

    fun setOnButtonExpandCollapseClickListener(onButtonExpandCollapseClickListener: OnClickListener) {
        this.onButtonExpandCollapseClickListener = onButtonExpandCollapseClickListener
    }

    fun setOnButtonDeleteClickListener(onButtonDeleteClickListener: OnClickListener) {
        this.onButtonDeleteClickListener = onButtonDeleteClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_note, parent, false)
        return NoteViewHolder(itemView, onButtonEditClickListener, onButtonExpandCollapseClickListener,
        onButtonDeleteClickListener)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val pair = notes[position]
        holder.date.text = dateFormat.format(pair.first)
        holder.note.text = pair.second
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    class NoteViewHolder(itemView: View, onButtonEditClickListener: OnClickListener,
                         onButtonExpandCollapseClickListener: OnClickListener,
                         onButtonDeleteClickListener: OnClickListener): ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.date)
        val note: TextView = itemView.findViewById(R.id.note)
        val card: CardView = itemView.findViewById(R.id.note_card)
        val expandCollapseButton: ImageView = itemView.findViewById<ImageView>(R.id.btn_expand_collapse).apply {
            tag = this@NoteViewHolder
            setOnClickListener(onButtonExpandCollapseClickListener)
        }
        init {
            itemView.findViewById<ImageView>(R.id.btn_edit).apply {
                tag = this@NoteViewHolder
                setOnClickListener(onButtonEditClickListener)
            }
            itemView.findViewById<ImageView>(R.id.btn_delete).apply {
                tag = this@NoteViewHolder
                setOnClickListener(onButtonDeleteClickListener)
            }
        }
    }
}