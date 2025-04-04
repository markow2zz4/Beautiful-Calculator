package com.example.calculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.calculator.databinding.ItemHistoryBinding

class HistoryAdapter(private val historyList: MutableList<Pair<String, String>>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(private val binding: ItemHistoryBinding)
        : RecyclerView.ViewHolder(binding.root) {


        fun bind(historyInputText: String, historyResultText: String,) {
            binding.historyInputText.text = historyInputText
            binding.equalsResultText.text = historyResultText
        }
    }

    // Метод, создающий новые ViewHolder-ы
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val (expression, result) = historyList[position]
        holder.bind(expression, result)
    }

    override fun getItemCount(): Int = historyList.size

    fun addItem(expression: String, result: String) {
        historyList.add(0, Pair(expression, result))
        notifyItemInserted(0)
    }

    fun clearItems() {
        historyList.clear()
        notifyDataSetChanged()
    }
}