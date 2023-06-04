package com.katiearose.sobriety

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import com.katiearose.sobriety.databinding.ListItemTimelineAltBinding
import com.katiearose.sobriety.shared.Addiction
import com.katiearose.sobriety.utils.convertSecondsToString
import com.katiearose.sobriety.utils.getDateFormatPattern
import com.katiearose.sobriety.utils.getSharedPref
import com.katiearose.sobriety.utils.textResource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TimelineAdapterAlt(context: Context, addiction: Addiction) : ListAdapter<Long, TimelineAdapterAlt.TimelineAltViewHolder>(
    object : DiffUtil.ItemCallback<Long>() {
        override fun areItemsTheSame(oldItem: Long, newItem: Long): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: Long, newItem: Long): Boolean = oldItem == newItem
    }
) {

    private val preferences = context.getSharedPref()

    init {
        val result = mutableListOf<Long>()
        val entries = addiction.history.entries
        for (entry in entries) {
            result.add(entry.key)
            result.add(entry.value)
        }
        submitList(result)
    }

    // From com.github.vipulasri.timelineview.TimelineView
    enum class LineType {
        NORMAL,
        START,
        END,
        ONLYONE
    }

    private val dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss ${preferences.getDateFormatPattern()}")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineAltViewHolder {
        val binding = ListItemTimelineAltBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimelineAltViewHolder(binding, viewType)
    }

    override fun onBindViewHolder(holder: TimelineAltViewHolder, position: Int) {
        val context = holder.binding.root.context
        holder.binding.date.text = if (getItem(position) != 0L) dateFormat.format(Instant.ofEpochMilli(getItem(position)).atZone(
            ZoneId.systemDefault())) else context.getString(R.string.present)
        if (position % 2 == 0) {
            holder.binding.attempt.text = context.getString(R.string.attempt_started, position / 2 + 1)
        } else {
            if (getItem(position) != 0L) {
                holder.binding.attempt.text = context.getString(R.string.attempt_ended, position / 2 + 1,
                    context.convertSecondsToString((getItem(position) - getItem(position - 1)) / 1000))
            } else {
                holder.binding.attempt.textResource = R.string.ongoing
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> LineType.START
            position == (itemCount - 1) -> LineType.END
            (position % 2 == 0) -> if (getItem(position) != getItem(position - 1))
                LineType.START else LineType.NORMAL
            else -> if (getItem(position) != getItem(position + 1))
                LineType.END else LineType.NORMAL
        }.ordinal
    }

    class TimelineAltViewHolder(val binding: ListItemTimelineAltBinding, viewType: Int) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.timelineView.initLine(viewType)
        }
    }


}