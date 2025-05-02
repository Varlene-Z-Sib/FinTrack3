package com.example.fintrackv20

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fintrackv20.R
import com.example.fintrackv20.TransactionViewModel

class TransactionAdapter : ListAdapter<TransactionViewModel.TransactionItem, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
        private val txtDescription: TextView = itemView.findViewById(R.id.txtDescription)
        private val imgAttachment: ImageView = itemView.findViewById(R.id.imgAttachment)

        fun bind(item: TransactionViewModel.TransactionItem) {
            txtAmount.text = item.amount
            txtDescription.text = item.description
            imgAttachment.visibility = if (item.hasAttachment) View.VISIBLE else View.GONE
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionViewModel.TransactionItem>() {
        override fun areItemsTheSame(oldItem: TransactionViewModel.TransactionItem, newItem: TransactionViewModel.TransactionItem): Boolean {
            return oldItem.description == newItem.description && oldItem.amount == newItem.amount // Adjust based on a unique identifier if available
        }

        override fun areContentsTheSame(oldItem: TransactionViewModel.TransactionItem, newItem: TransactionViewModel.TransactionItem): Boolean {
            return oldItem == newItem
        }
    }
}