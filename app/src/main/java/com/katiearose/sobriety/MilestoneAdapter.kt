package com.katiearose.sobriety

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.katiearose.sobriety.databinding.ListItemMilestoneBinding
import com.katiearose.sobriety.utils.getHideCompletedMilestonesPref
import com.katiearose.sobriety.utils.getSharedPref
import com.katiearose.sobriety.utils.getSortMilestonesPref
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MilestoneAdapter(private val addiction: Addiction, private val context: Context) :
    ListAdapter<Pair<Int, ChronoUnit>, MilestoneAdapter.MilestoneViewHolder>(object : DiffUtil.ItemCallback<Pair<Int, ChronoUnit>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Int, ChronoUnit>,
            newItem: Pair<Int, ChronoUnit>
        ): Boolean {
            return oldItem.first == newItem.first
        }

        override fun areContentsTheSame(
            oldItem: Pair<Int, ChronoUnit>,
            newItem: Pair<Int, ChronoUnit>
        ): Boolean {
            return oldItem.second == newItem.second
        }

    }) {
    private val preferences = context.getSharedPref()
    init { update() }
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd MMM yyyy")
    private lateinit var deleteButtonAction: (Int) -> Unit

    fun update() {
        var milestones = when (preferences.getSortMilestonesPref()) {
            "asc" -> addiction.milestones.map { it }.sortedWith { m1, m2 ->
                (m1.first * m1.second.duration.toMillis()).compareTo(m2.first * m2.second.duration.toMillis())
            }
            "desc" -> addiction.milestones.map { it }.sortedWith { m1, m2 ->
                (m2.first * m2.second.duration.toMillis()).compareTo(m1.first * m1.second.duration.toMillis())
            }
            else -> addiction.milestones.map { it }
        }
        if (preferences.getHideCompletedMilestonesPref())
            milestones = milestones.filter {
                System.currentTimeMillis() < addiction.lastRelapse.toEpochMilli() + it.first * it.second.duration.toMillis()
            }
        submitList(milestones)
    }

    fun setDeleteButtonAction(deleteButtonAction: (Int) -> Unit) {
        this.deleteButtonAction = deleteButtonAction
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MilestoneViewHolder {
        val binding = ListItemMilestoneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MilestoneViewHolder(binding, deleteButtonAction)
    }

    override fun onBindViewHolder(holder: MilestoneViewHolder, position: Int) {
        val milestone = currentList[position]
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
                dateTimeFormatter.format(Instant.ofEpochMilli(goal).atZone(ZoneId.systemDefault()))
        }
    }

    class MilestoneViewHolder(binding: ListItemMilestoneBinding, deleteButtonAction: (Int) -> Unit) :
        ViewHolder(binding.root) {
        val milestone = binding.milestone
        val milestoneTime = binding.milestoneTime
        val milestoneProgressBar = binding.milestoneProgress

        init {
            binding.btnDeleteMilestone.setOnClickListener { deleteButtonAction(adapterPosition) }
        }
    }
}