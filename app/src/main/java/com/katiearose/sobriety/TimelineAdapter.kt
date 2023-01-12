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
import com.katiearose.sobriety.utils.getDateFormatPattern
import com.katiearose.sobriety.utils.getSharedPref
import com.katiearose.sobriety.utils.textResource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TimelineAdapter(addiction: Addiction, private val context: Context):
    ListAdapter<Pair<Long, Long>, TimelineAdapter.TimelineViewHolder>(object : DiffUtil.ItemCallback<Pair<Long, Long>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Long, Long>,
            newItem: Pair<Long, Long>
        ): Boolean = oldItem.first == newItem.first

        override fun areContentsTheSame(
            oldItem: Pair<Long, Long>,
            newItem: Pair<Long, Long>
        ): Boolean = oldItem.second == newItem.second
    }) {

    private val preferences = context.getSharedPref()

    init { submitList(addiction.history.toList()) }

    private val dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss ${preferences.getDateFormatPattern()}")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val binding = ListItemTimelineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimelineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        holder.attemptNo.text = context.getString(R.string.attempt, position + 1)
        val pair = currentList[position]
        if (pair.second == 0L) {
            holder.dateRange.text = context.getString(R.string.time_started, dateFormat.format(Instant.ofEpochMilli(pair.first).atZone(ZoneId.systemDefault())))
            holder.abstainPeriod.textResource = R.string.ongoing
        } else {
            holder.dateRange.text = context.getString(R.string.time_range, dateFormat.format(Instant.ofEpochMilli(pair.first).atZone(ZoneId.systemDefault())),
                dateFormat.format(Instant.ofEpochMilli(pair.second).atZone(ZoneId.systemDefault())))
            holder.abstainPeriod.text = context.getString(R.string.duration, context.convertSecondsToString((pair.second - pair.first) / 1000))
        }
    }

    class TimelineViewHolder(binding: ListItemTimelineBinding): RecyclerView.ViewHolder(binding.root) {
        val attemptNo: TextView = binding.attemptNo
        val dateRange: TextView = binding.dateRange
        val abstainPeriod: TextView = binding.abstainPeriod
    }
}