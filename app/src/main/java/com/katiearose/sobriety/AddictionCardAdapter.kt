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
import com.katiearose.sobriety.utils.convertSecondsToString
import com.katiearose.sobriety.utils.secondsFromNow
import java.text.DateFormat
import java.util.*
import kotlin.math.absoluteValue

class AddictionCardAdapter(private val context: Context) :
    RecyclerView.Adapter<AddictionCardAdapter.AddictionCardViewHolder>() {

    private lateinit var deleteButtonAction: (Int) -> Unit
    private lateinit var relapseButtonAction: (Int) -> Unit
    private lateinit var stopButtonAction: (Int) -> Unit
    private lateinit var timelineButtonAction: (Int) -> Unit
    private lateinit var priorityTextViewAction: (Int) -> Unit
    private lateinit var miscButtonAction: (Int) -> Unit
    private val dateFormat = DateFormat.getDateTimeInstance()

    fun setDeleteButtonAction(deleteButtonAction: (Int) -> Unit) {
        this.deleteButtonAction = deleteButtonAction
    }

    fun setRelapseButtonAction(relapseButtonAction: (Int) -> Unit) {
        this.relapseButtonAction = relapseButtonAction
    }

    fun setStopButtonAction(stopButtonAction: (Int) -> Unit) {
        this.stopButtonAction = stopButtonAction
    }

    fun setTimelineButtonAction(timelineButtonAction: (Int) -> Unit) {
        this.timelineButtonAction = timelineButtonAction
    }

    fun setPriorityTextViewAction(priorityTextViewAction: (Int) -> Unit) {
        this.priorityTextViewAction = priorityTextViewAction
    }

    fun setMiscButtonAction(miscButtonAction: (Int) -> Unit) {
        this.miscButtonAction = miscButtonAction
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddictionCardViewHolder {
        val binding = CardAddictionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddictionCardViewHolder(binding, deleteButtonAction, relapseButtonAction,
            stopButtonAction, timelineButtonAction, priorityTextViewAction, miscButtonAction)
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
        holder.textViewPriority.setTextColor(when (addiction.priority) {
            Addiction.Priority.HIGH -> ResourcesCompat.getColor(context.resources, R.color.red, context.theme)
            Addiction.Priority.MEDIUM -> ResourcesCompat.getColor(context.resources, R.color.orange, context.theme)
            Addiction.Priority.LOW -> ResourcesCompat.getColor(context.resources, R.color.green, context.theme)
        })
        if (addiction.isFuture()) {
            holder.textViewTime.text = context.getString(R.string.time_until_tracked,
                context.convertSecondsToString(addiction.lastRelapse.secondsFromNow().absoluteValue))
        } else {
            holder.textViewTime.text = if (!addiction.isStopped) context.convertSecondsToString(addiction.lastRelapse.secondsFromNow())
            else context.getString(R.string.stop_notice,
                dateFormat.format(Date(addiction.timeStopped)),
                context.convertSecondsToString((addiction.timeStopped - addiction.lastRelapse.toEpochMilli()) / 1000))
        }
        holder.textViewAverage.visibility = if (addiction.averageRelapseDuration == -1L) View.GONE else View.VISIBLE
        holder.textViewAverage.text =
            context.getString(R.string.recent_avg, context.convertSecondsToString(addiction.averageRelapseDuration))
    }

    override fun getItemCount() = Main.addictions.size

    class AddictionCardViewHolder(binding: CardAddictionBinding, deleteButtonAction: (Int) -> Unit,
                                  relapseButtonAction: (Int) -> Unit,
                                  stopButtonAction: (Int) -> Unit,
                                  timelineButtonAction: (Int) -> Unit,
                                  priorityTextViewAction: (Int) -> Unit,
                                  miscButtonAction: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        val textViewName: TextView = binding.textViewAddictionName
        val textViewPriority: TextView = binding.textViewPriority
        val textViewTime: TextView = binding.textViewTime
        val textViewAverage: TextView = binding.textViewAverage
        init {
            binding.imageDelete.setOnClickListener { deleteButtonAction(adapterPosition) }
            binding.imageReset.setOnClickListener { relapseButtonAction(adapterPosition) }
            binding.imageStop.setOnClickListener { stopButtonAction(adapterPosition) }
            binding.imageTimeline.setOnClickListener { timelineButtonAction(adapterPosition) }
            textViewPriority.setOnClickListener { priorityTextViewAction(adapterPosition) }
            binding.imageMisc.setOnClickListener { miscButtonAction(adapterPosition) }
        }
    }
}