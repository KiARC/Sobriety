package com.katiearose.sobriety

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.katiearose.sobriety.activities.Main
import com.katiearose.sobriety.databinding.CardAddictionBinding
import com.katiearose.sobriety.shared.Addiction
import com.katiearose.sobriety.utils.convertRangeToString
import com.katiearose.sobriety.utils.convertSecondsToString
import com.katiearose.sobriety.utils.getDateFormatPattern
import com.katiearose.sobriety.utils.getAverageAttemptsWindow
import com.katiearose.sobriety.utils.getSharedPref
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AddictionCardAdapter(
    private val context: Context,
    private val deleteButtonAction: (Addiction) -> Unit,
    private val relapseButtonAction: (Addiction) -> Unit,
    private val stopButtonAction: (Addiction) -> Unit,
    private val timelineButtonAction: (Addiction) -> Unit,
    private val priorityTextViewAction: (Addiction) -> Unit,
    private val miscButtonAction: (Addiction) -> Unit,
    private val cardButtonAction: (Addiction) -> Unit
) :
    RecyclerView.Adapter<AddictionCardAdapter.AddictionCardViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddictionCardViewHolder {
        val binding =
            CardAddictionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddictionCardViewHolder(binding,
            { deleteButtonAction(Main.addictions[it]) },
            { relapseButtonAction(Main.addictions[it]) },
            { stopButtonAction(Main.addictions[it]) },
            { timelineButtonAction(Main.addictions[it]) },
            { priorityTextViewAction(Main.addictions[it]) },
            { miscButtonAction(Main.addictions[it]) },
            { cardButtonAction(Main.addictions[it]) },
            { Main.addictions[it] })
    }

    override fun onBindViewHolder(
        holder: AddictionCardViewHolder,
        position: Int
    ) {
        val stringBuilder = StringBuilder(context.getString(R.string.priority)).append(": ")
        val addiction = Main.addictions[position]

        holder.textViewName.text = addiction.name
        holder.textViewPriority.text = when (addiction.priority) {
            Addiction.Priority.HIGH -> stringBuilder.append(context.getString(R.string.high))
            Addiction.Priority.MEDIUM -> stringBuilder.append(context.getString(R.string.medium))
            Addiction.Priority.LOW -> stringBuilder.append(context.getString(R.string.low))
        }
        holder.textViewPriority.setTextColor(
            when (addiction.priority) {
                Addiction.Priority.HIGH -> ResourcesCompat.getColor(
                    context.resources,
                    R.color.red,
                    context.theme
                )
                Addiction.Priority.MEDIUM -> ResourcesCompat.getColor(
                    context.resources,
                    R.color.orange,
                    context.theme
                )
                Addiction.Priority.LOW -> ResourcesCompat.getColor(
                    context.resources,
                    R.color.green,
                    context.theme
                )
            }
        )
        val numAttempts = context.getSharedPref().getAverageAttemptsWindow()
        val averageRelapseDuration = addiction.calculateRecentAverage(numAttempts)
        if (averageRelapseDuration == null) {
            holder.textViewAverage.visibility = View.GONE
        } else {
            holder.textViewAverage.visibility = View.VISIBLE
            holder.textViewAverage.text =
                context.getString(
                    R.string.recent_avg,
                    context.convertSecondsToString(averageRelapseDuration / 1000L)
                )
        }

        holder.refresh()
    }

    override fun getItemCount() = Main.addictions.size

    class AddictionCardViewHolder(
        private val binding: CardAddictionBinding, deleteButtonAction: (Int) -> Unit,
        relapseButtonAction: (Int) -> Unit,
        stopButtonAction: (Int) -> Unit,
        timelineButtonAction: (Int) -> Unit,
        priorityTextViewAction: (Int) -> Unit,
        miscButtonAction: (Int) -> Unit,
        cardButtonAction: (Int) -> Unit,
        private val addictionSupplier: (Int) -> Addiction) : RecyclerView.ViewHolder(binding.root) {
        val textViewName: TextView = binding.textViewAddictionName
        val textViewPriority: TextView = binding.textViewPriority
        val textViewAverage: TextView = binding.textViewAverage
        private lateinit var addiction: Addiction
        private val mainScope = MainScope()

        fun refresh() {
            addiction = addictionSupplier(adapterPosition)
            displayInfo()
        }

        private fun getFormattedDate(milli: Long): String {
            val dateFormat = DateTimeFormatter.ofPattern(binding.root.context.getSharedPref().getDateFormatPattern())
            val date = Instant.ofEpochMilli(milli).atZone(ZoneId.systemDefault())
            return dateFormat.format(date)
        }

        private fun displayInfo() {
            binding.textViewTime.text = when (addiction.status) {
                Addiction.Status.Ongoing -> binding.root.context.convertRangeToString(addiction.history.keys.last())

                Addiction.Status.Stopped -> binding.root.context.getString(
                    R.string.stop_notice,
                    getFormattedDate(addiction.history.values.last()),
                    binding.root.context.convertRangeToString(addiction.history.keys.last(), addiction.history.values.last())
                )

                Addiction.Status.Future -> binding.root.context.getString(
                    R.string.time_until_tracked,
                    binding.root.context.convertRangeToString(Instant.now().toEpochMilli(), addiction.history.keys.last())
                )
            }
        }

        init {
            mainScope.launch {
                while (true) {
                    displayInfo()
                    delay(1000)
                }
            }
            binding.imageDelete.setOnClickListener { deleteButtonAction(adapterPosition) }
            binding.imageReset.setOnClickListener { relapseButtonAction(adapterPosition) }
            binding.imageStop.setOnClickListener { stopButtonAction(adapterPosition) }
            binding.imageTimeline.setOnClickListener { timelineButtonAction(adapterPosition) }
            textViewPriority.setOnClickListener { priorityTextViewAction(adapterPosition) }
            binding.imageMisc.setOnClickListener { miscButtonAction(adapterPosition) }
            binding.cardViewAddiction.setOnClickListener { cardButtonAction(adapterPosition) }
        }
    }
}
