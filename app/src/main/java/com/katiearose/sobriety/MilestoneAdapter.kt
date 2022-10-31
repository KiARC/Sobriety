package com.katiearose.sobriety

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class MilestoneAdapter(private val addiction: Addiction, private val context: Context) :
    RecyclerView.Adapter<MilestoneAdapter.MilestoneViewHolder>() {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd MMM yyyy")
    private var milestones = addiction.milestones.map { it }.sortedWith { m1, m2 ->
        (m1.first * m1.second.duration.toMillis()).compareTo(m2.first * m2.second.duration.toMillis())
    }
    private lateinit var onButtonDeleteClickListener: View.OnClickListener

    @SuppressLint("NotifyDataSetChanged")
    fun update() {
        milestones = addiction.milestones.map { it }.sortedWith { m1, m2 ->
            (m1.first * m1.second.duration.toMillis()).compareTo(m2.first * m2.second.duration.toMillis())
        }
        notifyDataSetChanged()
    }

    fun setOnButtonDeleteClickListener(onButtonDeleteClickListener: View.OnClickListener) {
        this.onButtonDeleteClickListener = onButtonDeleteClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MilestoneViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_milestone, parent, false)
        return MilestoneViewHolder(itemView, onButtonDeleteClickListener)
    }

    override fun onBindViewHolder(holder: MilestoneViewHolder, position: Int) {
        val milestone = milestones[position]
        holder.milestone.text = StringBuilder(milestone.first.toString()).append(" ").append(
            when (milestone.second) {
                ChronoUnit.HOURS -> context.getString(R.string.unit_hour)
                ChronoUnit.DAYS -> context.getString(R.string.unit_day)
                ChronoUnit.WEEKS -> context.getString(R.string.unit_week)
                ChronoUnit.MONTHS -> context.getString(R.string.unit_month)
                ChronoUnit.YEARS -> context.getString(R.string.unit_year)
                else -> "Unsupported"
            }
        ).toString()
        val goal =
            addiction.lastRelapse.toEpochMilli() + milestone.first * milestone.second.duration.toMillis()
        holder.milestoneProgressBar.progress =
            (((System.currentTimeMillis() - addiction.lastRelapse.toEpochMilli()).toFloat() /
                    (goal - addiction.lastRelapse.toEpochMilli())) * 100).toInt()
        if (holder.milestoneProgressBar.progress == 100) {
            holder.milestoneTime.text = context.getString(R.string.completed)
        } else {
            holder.milestoneTime.text =
                dateTimeFormatter.format(Date(goal).toInstant().atZone(ZoneId.systemDefault()))
        }
    }

    override fun getItemCount(): Int {
        return milestones.size
    }

    class MilestoneViewHolder(itemView: View, onButtonDeleteClickListener: View.OnClickListener) :
        ViewHolder(itemView) {
        val milestone: TextView = itemView.findViewById(R.id.milestone)
        val milestoneTime: TextView = itemView.findViewById(R.id.milestone_time)
        val milestoneProgressBar: ProgressBar = itemView.findViewById(R.id.milestone_progress)

        init {
            itemView.findViewById<ImageView>(R.id.btn_delete_milestone).apply {
                tag = this@MilestoneViewHolder
                setOnClickListener(onButtonDeleteClickListener)
            }
        }
    }
}