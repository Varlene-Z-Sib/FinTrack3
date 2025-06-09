package com.example.fintrack3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TransactionViewModel(
    private val firestore: FirebaseFirestore,
    private val userId: String
) : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private var listenerRegistration: ListenerRegistration? = null

    init {
        listenToTransactions()
    }

    private fun listenToTransactions() {
        listenerRegistration = firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle errors
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val transactionList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Transaction::class.java)?.apply {
                            transactionId = doc.id // assign Firestore doc id if you want
                        }
                    }
                    _transactions.postValue(transactionList)
                } else {
                    _transactions.postValue(emptyList())
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
