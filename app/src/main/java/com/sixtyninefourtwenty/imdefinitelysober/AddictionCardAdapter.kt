package com.sixtyninefourtwenty.imdefinitelysober

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sixtyninefourtwenty.imdefinitelysober.activities.Main
import com.sixtyninefourtwenty.imdefinitelysober.internal.CacheHandler
import com.sixtyninefourtwenty.imdefinitelysober.utils.convertSecondsToString
import com.sixtyninefourtwenty.imdefinitelysober.utils.showConfirmDialog
import com.sixtyninefourtwenty.imdefinitelysober.utils.secondsFromNow

class AddictionCardAdapter(private val context: Context, private val cacheHandler: CacheHandler) :
    RecyclerView.Adapter<AddictionCardAdapter.AddictionCardViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddictionCardViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.card_addiction, parent, false)
        return AddictionCardViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: AddictionCardViewHolder,
        position: Int
    ) {
        val addiction = Main.addictions[position]

        holder.textViewName.text = addiction.name
        holder.textViewTime.text =
            context.convertSecondsToString(addiction.lastRelapse.secondsFromNow())
        holder.textViewAverage.visibility = if (addiction.averageRelapseDuration == -1L) View.GONE else View.VISIBLE
        holder.textViewAverage.text =
            context.getString(R.string.recent_avg, context.convertSecondsToString(addiction.averageRelapseDuration))

        holder.buttonDelete.setOnClickListener {
            val action: () -> Unit = {
                Main.addictions.remove(addiction)
                notifyItemRemoved(position)
                Main.deleting = true
                cacheHandler.writeCache()
            }
            context.showConfirmDialog(context.getString(R.string.delete), context.getString(R.string.delete_confirm, addiction.name), action)
        }

        holder.buttonReset.setOnClickListener {
            val action: () -> Unit = {
                addiction.relapse()
                notifyItemChanged(position)
                cacheHandler.writeCache()
            }
            context.showConfirmDialog(context.getString(R.string.relapse), context.getString(R.string.relapse_confirm, addiction.name), action)
        }
    }

    override fun getItemCount() = Main.addictions.size

    class AddictionCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewAddictionName)
        val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        val textViewAverage: TextView = itemView.findViewById(R.id.textViewAverage)
        val buttonDelete: ImageView = itemView.findViewById(R.id.imageDelete)
        val buttonReset: ImageView = itemView.findViewById(R.id.imageReset)
    }
}