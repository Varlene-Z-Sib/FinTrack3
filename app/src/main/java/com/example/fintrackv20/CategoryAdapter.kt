package com.example.fintrackv20.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fintrackv20.R
import com.example.fintrackv20.roomDB.Category

class CategoryAdapter(private var categoryList: List<Category>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: TextView = itemView.findViewById(R.id.tvCategoryName)
        // You might add more views here if you want to display other category information
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false) // Create item layout
        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentCategory = categoryList[position]
        holder.categoryNameTextView.text = currentCategory.name
        // Bind other category data to the views in the ViewHolder if needed
    }

    override fun getItemCount() = categoryList.size

    fun updateCategories(newCategoryList: List<Category>) {
        categoryList = newCategoryList
        notifyDataSetChanged() // Tell the RecyclerView to redraw its items
    }
}