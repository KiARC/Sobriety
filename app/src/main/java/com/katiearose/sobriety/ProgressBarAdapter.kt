package com.katiearose.sobriety

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.katiearose.sobriety.databinding.ListItemProgressBarBinding
import com.katiearose.sobriety.shared.Addiction
import com.katiearose.sobriety.utils.convertRangeToPercentList
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit

class ProgressBarAdapter(
    private val addiction: Addiction, private val context: Context,
) :
    ListAdapter<Triple<Int, Int, DateTimeUnit>, ProgressBarAdapter.ProgressBarViewHolder>(object :
        DiffUtil.ItemCallback<Triple<Int, Int, DateTimeUnit>>() {
        override fun areItemsTheSame(
            oldItem: Triple<Int, Int, DateTimeUnit>,
            newItem: Triple<Int, Int, DateTimeUnit>
        ): Boolean = oldItem.third == newItem.third

        override fun areContentsTheSame(
            oldItem: Triple<Int, Int, DateTimeUnit>,
            newItem: Triple<Int, Int, DateTimeUnit>
        ): Boolean = oldItem.first == newItem.first
    }) {

    init {
        update()
        MainScope().launch {
            while (true) {
                update()
                delay(1000)
            }
        }
    }

    fun update() {
        val list = convertRangeToPercentList(addiction.history.keys.last())
        // Filter initial zeros
        submitList(list.subList(list.indexOfFirst { it.first != 0 }, list.size))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressBarViewHolder {
        val binding =
            ListItemProgressBarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProgressBarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProgressBarViewHolder, position: Int) {
        val bar = currentList[position]
        holder.timeUnitProgress.progress = bar.second
        holder.timeUnit.text = when (bar.third) {
            DateTimeUnit.SECOND ->  context.resources.getQuantityString(R.plurals.seconds, bar.first, bar.first)
            DateTimeUnit.MINUTE -> context.resources.getQuantityString(R.plurals.minutes, bar.first, bar.first)
            DateTimeUnit.HOUR -> context.resources.getQuantityString(R.plurals.hours, bar.first, bar.first)
            DateTimeUnit.DAY -> context.resources.getQuantityString(R.plurals.days, bar.first, bar.first)
            DateTimeUnit.MONTH -> context.resources.getQuantityString(R.plurals.months, bar.first, bar.first)
            DateTimeUnit.YEAR -> context.resources.getQuantityString(R.plurals.years, bar.first, bar.first)
            else -> "Unsupported"
        }
    }

    class ProgressBarViewHolder(
        binding: ListItemProgressBarBinding,
    ) :
        ViewHolder(binding.root) {
        val timeUnit = binding.timeUnit
        val timeUnitProgress = binding.timeUnitProgress
    }
}