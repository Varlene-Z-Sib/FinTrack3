package com.example.fintrackv20

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fintrackv20.roomDB.FinTrackDB
import kotlinx.coroutines.launch

class TransactionViewModel(private val finTrackDB: FinTrackDB, userId: String?) : ViewModel() {

    private val _transactions = MutableLiveData<List<TransactionItem>>()
    val transactions: LiveData<List<TransactionItem>> = _transactions

    init {
        if (!userId.isNullOrEmpty()) {
            loadTransactions(userId)
        } else {
            // Handle the case where userId is null or empty, perhaps show an error or empty list
            _transactions.value = emptyList()
        }
    }

    private fun loadTransactions(userId: String) {
        viewModelScope.launch {
            val transactionList = finTrackDB.transactionDao().getAllUserTransactions(userId)
            _transactions.value = transactionList.map { transaction ->
                TransactionItem(
                    amount = String.format("R %.2f", transaction.amount),
                    description = transaction.description,
                    hasAttachment = !transaction.image.isNullOrEmpty()
                )
            }
        }
    }

    data class TransactionItem(
        val amount: String,
        val description: String,
        val hasAttachment: Boolean
    )
}