package com.katiearose.sobriety

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.katiearose.sobriety.utils.convertSecondsToString
import java.text.DateFormat
import java.util.*

class TimelineAdapter(addiction: Addiction, private val context: Context):
    RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    private var history = addiction.history.toList()
    private val dateFormat = DateFormat.getDateTimeInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_timeline, parent, false)
        return TimelineViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        holder.attemptNo.text = context.getString(R.string.attempt, position + 1)
        val pair = history[position]
        if (pair.second == 0L) {
            holder.dateRange.text = context.getString(R.string.time_started, dateFormat.format(Date(pair.first)))
            holder.abstainPeriod.text = context.getString(R.string.ongoing)
        } else {
            holder.dateRange.text = context.getString(R.string.time_range, dateFormat.format(Date(pair.first)), dateFormat.format(Date(pair.second)))
            holder.abstainPeriod.text = context.getString(R.string.duration, context.convertSecondsToString((pair.second - pair.first) / 1000))
        }
    }

    override fun getItemCount(): Int {
        return history.size
    }

    class TimelineViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val attemptNo: TextView = itemView.findViewById(R.id.attempt_no)
        val dateRange: TextView = itemView.findViewById(R.id.date_range)
        val abstainPeriod: TextView = itemView.findViewById(R.id.abstain_period)
    }
}