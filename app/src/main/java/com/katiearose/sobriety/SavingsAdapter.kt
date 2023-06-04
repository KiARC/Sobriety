package com.katiearose.sobriety

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.katiearose.sobriety.databinding.ListItemSavingBinding
import com.katiearose.sobriety.shared.Addiction

class SavingsAdapter(private val addiction: Addiction, private val context: Context,
                     private val editButtonAction: (Pair<String, Pair<Double, String>>) -> Unit,
                     private val deleteButtonAction: (Pair<String, Pair<Double, String>>) -> Unit):
    ListAdapter<Pair<String, Pair<Double, String>>, SavingsAdapter.SavingsViewHolder>(object :
        DiffUtil.ItemCallback<Pair<String, Pair<Double, String>>>() {
        override fun areItemsTheSame(
            oldItem: Pair<String, Pair<Double, String>>,
            newItem: Pair<String, Pair<Double, String>>
        ): Boolean = oldItem.first == newItem.first

        override fun areContentsTheSame(
            oldItem: Pair<String, Pair<Double, String>>,
            newItem: Pair<String, Pair<Double, String>>
        ): Boolean = oldItem.second == newItem.second
    }) {
    init { update() }

    fun update() = submitList(addiction.savings.toList())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingsViewHolder {
        val binding = ListItemSavingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavingsViewHolder(binding,
            { editButtonAction(currentList[it]) },
            { deleteButtonAction(currentList[it]) })
    }

    override fun onBindViewHolder(holder: SavingsViewHolder, position: Int) {
        val pair = currentList[position]
        holder.savingName.text = pair.first
        holder.savingsAmountPerDay.text = context.getString(R.string.other_saved_per_day, pair.second.first, pair.second.second)
    }

    class SavingsViewHolder(binding: ListItemSavingBinding, editButtonAction: (Int) -> Unit,
                            deleteButtonAction: (Int) -> Unit
    ): ViewHolder(binding.root) {
        val savingName = binding.savingsName
        val savingsAmountPerDay = binding.savingsAmountPerDay
        init {
            binding.btnEditSaving.setOnClickListener { editButtonAction(adapterPosition) }
            binding.btnDeleteSaving.setOnClickListener { deleteButtonAction(adapterPosition) }
        }
    }
}