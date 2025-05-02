package com.example.fintrackv20.roomDB

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FinTrackDB.getInstance(application)
    private val dao = db.categoryDao()

    fun insertCategory(userId: String, name: String) {
        viewModelScope.launch {
            dao.insert(Category(userId = userId, name = name))
        }
    }
}
