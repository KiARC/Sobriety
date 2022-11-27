package com.katiearose.sobriety

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.katiearose.sobriety.databinding.ListItemMilestoneBinding
import com.katiearose.sobriety.shared.Addiction
import com.katiearose.sobriety.shared.SortMode
import com.katiearose.sobriety.shared.toMillis
import com.katiearose.sobriety.utils.getHideCompletedMilestonesPref
import com.katiearose.sobriety.utils.getSharedPref
import com.katiearose.sobriety.utils.getSortMilestonesPref
import kotlinx.datetime.DateTimeUnit
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MilestoneAdapter(
    private val addiction: Addiction, private val context: Context,
    private val deleteButtonAction: (Pair<Int, DateTimeUnit>) -> Unit
) :
    ListAdapter<Pair<Int, DateTimeUnit>, MilestoneAdapter.MilestoneViewHolder>(object :
        DiffUtil.ItemCallback<Pair<Int, DateTimeUnit>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Int, DateTimeUnit>,
            newItem: Pair<Int, DateTimeUnit>
        ): Boolean = oldItem.first == newItem.first

        override fun areContentsTheSame(
            oldItem: Pair<Int, DateTimeUnit>,
            newItem: Pair<Int, DateTimeUnit>
        ): Boolean = oldItem.second == newItem.second
    }) {
    private val preferences = context.getSharedPref()

    init {
        update()
    }

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd MMM yyyy")

    fun update() {
        submitList(when (preferences.getSortMilestonesPref()) {
            "asc" -> addiction.getMilestonesList(SortMode.ASC, preferences.getHideCompletedMilestonesPref())
            "desc" -> addiction.getMilestonesList(SortMode.DESC, preferences.getHideCompletedMilestonesPref())
            else -> addiction.getMilestonesList(SortMode.NONE, preferences.getHideCompletedMilestonesPref())
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MilestoneViewHolder {
        val binding =
            ListItemMilestoneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MilestoneViewHolder(binding) { deleteButtonAction(currentList[it]) }
    }

    override fun onBindViewHolder(holder: MilestoneViewHolder, position: Int) {
        val milestone = currentList[position]
        holder.milestone.text = StringBuilder(milestone.first.toString()).append(" ").append(
            when {
                milestone.second == DateTimeUnit.HOUR -> context.getString(R.string.unit_hour)
                milestone.second == DateTimeUnit.DAY -> context.getString(R.string.unit_day)
                milestone.second == DateTimeUnit.WEEK -> context.getString(R.string.unit_week)
                milestone.second == DateTimeUnit.MONTH -> context.getString(R.string.unit_month)
                milestone.second == DateTimeUnit.YEAR -> context.getString(R.string.unit_year)
                else -> "Unsupported"
            }
        ).toString()
        val goal =
            addiction.lastRelapse.toEpochMilliseconds() + milestone.first * milestone.second.toMillis()
        holder.milestoneProgressBar.progress =
            (((System.currentTimeMillis() - addiction.lastRelapse.toEpochMilliseconds()).toFloat() /
                    (goal - addiction.lastRelapse.toEpochMilliseconds())) * 100).toInt()
        if (holder.milestoneProgressBar.progress == 100) {
            holder.milestoneTime.text = context.getString(R.string.completed)
        } else {
            holder.milestoneTime.text =
                dateTimeFormatter.format(Instant.ofEpochMilli(goal).atZone(ZoneId.systemDefault()))
        }
    }

    class MilestoneViewHolder(
        binding: ListItemMilestoneBinding,
        deleteButtonAction: (Int) -> Unit
    ) :
        ViewHolder(binding.root) {
        val milestone = binding.milestone
        val milestoneTime = binding.milestoneTime
        val milestoneProgressBar = binding.milestoneProgress

        init {
            binding.btnDeleteMilestone.setOnClickListener { deleteButtonAction(adapterPosition) }
        }
    }
}