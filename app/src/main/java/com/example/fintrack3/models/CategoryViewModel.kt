package com.example.fintrack3.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.fintrack3.models.Category
import com.example.fintrack3.repository.CategoryRepository
import java.util.*

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CategoryRepository()

    fun insertCategory(
        userId: String,
        name: String,
        description: String,
        color: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val categoryId = UUID.randomUUID().toString()
        val newCategory = Category(
            id = categoryId,
            userId = userId,
            name = name,
            description = description,
        )
        repository.addCategory(newCategory, onSuccess, onFailure)
    }
}
