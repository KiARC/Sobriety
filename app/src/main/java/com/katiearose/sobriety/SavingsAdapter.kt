package com.katiearose.sobriety

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class SavingsAdapter(private val addiction: Addiction, private val context: Context): Adapter<SavingsAdapter.SavingsViewHolder>() {
    private var savings = addiction.savings.toList()
    private lateinit var onButtonEditClickListener: View.OnClickListener
    private lateinit var onButtonDeleteClickListener: View.OnClickListener

    @SuppressLint("NotifyDataSetChanged")
    fun update() {
        savings = addiction.savings.toList()
        notifyDataSetChanged()
    }

    fun getCurrentList(): List<Pair<String, Pair<Double, String>>> {
        return savings
    }

    fun setOnButtonEditClickListener(onButtonEditClickListener: View.OnClickListener) {
        this.onButtonEditClickListener = onButtonEditClickListener
    }

    fun setOnButtonDeleteClickListener(onButtonDeleteClickListener: View.OnClickListener) {
        this.onButtonDeleteClickListener = onButtonDeleteClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_saving, parent, false)
        return SavingsViewHolder(itemView, onButtonEditClickListener, onButtonDeleteClickListener)
    }

    override fun onBindViewHolder(holder: SavingsViewHolder, position: Int) {
        val pair = savings[position]
        holder.savingName.text = pair.first
        holder.savingsAmountPerDay.text = context.getString(R.string.other_saved_per_day, pair.second.first, pair.second.second)
    }

    override fun getItemCount(): Int {
        return savings.size
    }

    class SavingsViewHolder(itemView: View, onButtonEditClickListener: View.OnClickListener,
                            onButtonDeleteClickListener: View.OnClickListener
    ): ViewHolder(itemView) {
        val savingName: TextView = itemView.findViewById(R.id.savings_name)
        val savingsAmountPerDay: TextView = itemView.findViewById(R.id.savings_amount_per_day)
        init {
            itemView.findViewById<ImageView>(R.id.btn_edit_saving).apply {
                tag = this@SavingsViewHolder
                setOnClickListener(onButtonEditClickListener)
            }
            itemView.findViewById<ImageView>(R.id.btn_delete_saving).apply {
                tag = this@SavingsViewHolder
                setOnClickListener(onButtonDeleteClickListener)
            }
        }
    }
}