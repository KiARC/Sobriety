package com.katiearose.sobriety

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.katiearose.sobriety.databinding.ListItemTimelineBinding
import com.katiearose.sobriety.shared.Addiction
import com.katiearose.sobriety.utils.convertSecondsToString
import java.text.DateFormat
import java.util.*

class TimelineAdapter(addiction: Addiction, private val context: Context):
    ListAdapter<Pair<Long, Long>, TimelineAdapter.TimelineViewHolder>(object : DiffUtil.ItemCallback<Pair<Long, Long>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Long, Long>,
            newItem: Pair<Long, Long>
        ): Boolean {
            return oldItem.first == newItem.first
        }

        override fun areContentsTheSame(
            oldItem: Pair<Long, Long>,
            newItem: Pair<Long, Long>
        ): Boolean {
            return oldItem.second == newItem.second
        }

    }) {

    init { submitList(addiction.history.toList()) }

    private val dateFormat = DateFormat.getDateTimeInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val binding = ListItemTimelineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimelineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        holder.attemptNo.text = context.getString(R.string.attempt, position + 1)
        val pair = currentList[position]
        if (pair.second == 0L) {
            holder.dateRange.text = context.getString(R.string.time_started, dateFormat.format(Date(pair.first)))
            holder.abstainPeriod.text = context.getString(R.string.ongoing)
        } else {
            holder.dateRange.text = context.getString(R.string.time_range, dateFormat.format(Date(pair.first)), dateFormat.format(Date(pair.second)))
            holder.abstainPeriod.text = context.getString(R.string.duration, context.convertSecondsToString((pair.second - pair.first) / 1000))
        }
    }

    class TimelineViewHolder(binding: ListItemTimelineBinding): RecyclerView.ViewHolder(binding.root) {
        val attemptNo: TextView = itemView.findViewById(R.id.attempt_no)
        val dateRange: TextView = itemView.findViewById(R.id.date_range)
        val abstainPeriod: TextView = itemView.findViewById(R.id.abstain_period)
    }
}