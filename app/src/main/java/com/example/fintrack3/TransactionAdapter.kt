package com.example.fintrack3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fintrack3.models.Transaction

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

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

        fun bind(item: Transaction) {
            // Format amount to string with currency symbol if you want
            txtAmount.text = "$${item.amount}" // Customize formatting as needed
            txtDescription.text = item.description

            // Show attachment icon if image URL is present and not empty
            imgAttachment.visibility = if (!item.image.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            // Use unique Firestore document ID if available
            return oldItem.transactionId == newItem.transactionId
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
