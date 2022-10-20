package com.sixtyninefourtwenty.imdefinitelysober

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sixtyninefourtwenty.imdefinitelysober.activities.Main
import com.sixtyninefourtwenty.imdefinitelysober.utils.convertSecondsToString
import com.sixtyninefourtwenty.imdefinitelysober.utils.secondsFromNow
import java.text.DateFormat
import java.util.*

class AddictionCardAdapter(private val context: Context) :
    RecyclerView.Adapter<AddictionCardAdapter.AddictionCardViewHolder>() {

    private lateinit var onButtonDeleteClickListener: OnClickListener
    private lateinit var onButtonRelapseClickListener: OnClickListener
    private lateinit var onButtonStopClickListener: OnClickListener
    private lateinit var onTimelineButtonClickListener: OnClickListener
    private val dateFormat = DateFormat.getDateTimeInstance()

    fun setOnButtonDeleteClickListener(onButtonDeleteClickListener: OnClickListener) {
        this.onButtonDeleteClickListener = onButtonDeleteClickListener
    }

    fun setOnButtonRelapseClickListener(onButtonRelapseClickListener: OnClickListener) {
        this.onButtonRelapseClickListener = onButtonRelapseClickListener
    }

    fun setOnButtonStopClickListener(onButtonStopClickListener: OnClickListener) {
        this.onButtonStopClickListener = onButtonStopClickListener
    }

    fun setOnTimelineButtonClickListener(onTimelineButtonClickListener: OnClickListener) {
        this.onTimelineButtonClickListener = onTimelineButtonClickListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddictionCardViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.card_addiction, parent, false)
        return AddictionCardViewHolder(itemView, onButtonDeleteClickListener, onButtonRelapseClickListener,
        onButtonStopClickListener, onTimelineButtonClickListener)
    }

    override fun onBindViewHolder(
        holder: AddictionCardViewHolder,
        position: Int
    ) {
        val addiction = Main.addictions[position]

        holder.textViewName.text = addiction.name
        holder.textViewTime.text = if (!addiction.isStopped) context.convertSecondsToString(addiction.lastRelapse.secondsFromNow())
        else context.getString(R.string.stop_notice,
            dateFormat.format(Date(addiction.timeStopped)),
            context.convertSecondsToString((addiction.timeStopped - addiction.lastRelapse.toEpochMilli()) / 1000))
        holder.textViewAverage.visibility = if (addiction.averageRelapseDuration == -1L) View.GONE else View.VISIBLE
        holder.textViewAverage.text =
            context.getString(R.string.recent_avg, context.convertSecondsToString(addiction.averageRelapseDuration))
    }

    override fun getItemCount() = Main.addictions.size

    class AddictionCardViewHolder(itemView: View, onButtonDeleteClickListener: OnClickListener,
                                  onButtonRelapseClickListener: OnClickListener,
                                  onButtonStopClickListener: OnClickListener,
                                  onTimelineButtonClickListener: OnClickListener) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewAddictionName)
        val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        val textViewAverage: TextView = itemView.findViewById(R.id.textViewAverage)

        init {
            itemView.findViewById<ImageView>(R.id.imageDelete).apply {
                tag = this@AddictionCardViewHolder
                setOnClickListener(onButtonDeleteClickListener)
            }
            itemView.findViewById<ImageView>(R.id.imageReset).apply {
                tag = this@AddictionCardViewHolder
                setOnClickListener(onButtonRelapseClickListener)
            }
            itemView.findViewById<ImageView>(R.id.imageStop).apply {
                tag = this@AddictionCardViewHolder
                setOnClickListener(onButtonStopClickListener)
            }
            itemView.findViewById<ImageView>(R.id.imageTimeline).apply {
                tag = this@AddictionCardViewHolder
                setOnClickListener(onTimelineButtonClickListener)
            }
        }
    }
}