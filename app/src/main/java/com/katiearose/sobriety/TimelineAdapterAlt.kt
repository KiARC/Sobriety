package com.katiearose.sobriety

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import com.katiearose.sobriety.databinding.ListItemTimelineAltBinding
import com.katiearose.sobriety.shared.Addiction
import com.katiearose.sobriety.utils.convertSecondsToString
import java.text.DateFormat
import java.util.Date

class TimelineAdapterAlt(addiction: Addiction) : ListAdapter<Long, TimelineAdapterAlt.TimelineAltViewHolder>(
    object : DiffUtil.ItemCallback<Long>() {
        override fun areItemsTheSame(oldItem: Long, newItem: Long): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: Long, newItem: Long): Boolean = oldItem == newItem
    }
) {

    init {
        val result = mutableListOf<Long>()
        val entries = addiction.history.entries
        for (entry in entries) {
            result.add(entry.key)
            result.add(entry.value)
        }
        submitList(result)
    }

    private val dateFormat = DateFormat.getDateTimeInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineAltViewHolder {
        val binding = ListItemTimelineAltBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimelineAltViewHolder(binding, viewType)
    }

    override fun onBindViewHolder(holder: TimelineAltViewHolder, position: Int) {
        val context = holder.binding.root.context
        holder.binding.date.text = if (getItem(position) != 0L) dateFormat.format(Date(getItem(position)))
        else context.getString(R.string.present)
        if (position % 2 == 0) {
            holder.binding.attempt.text = context.getString(R.string.attempt_started, position / 2 + 1)
        } else {
            if (getItem(position) != 0L) {
                holder.binding.attempt.text = context.getString(R.string.attempt_ended, position / 2 + 1,
                    context.convertSecondsToString((getItem(position) - getItem(position - 1)) / 1000))
            } else {
                holder.binding.attempt.text = context.getString(R.string.ongoing)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = TimelineView.getTimeLineViewType(position, itemCount)

    class TimelineAltViewHolder(val binding: ListItemTimelineAltBinding, viewType: Int) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.timelineView.initLine(viewType)
        }
    }


}