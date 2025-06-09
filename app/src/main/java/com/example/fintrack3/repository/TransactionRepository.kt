package com.example.fintrack3.repository

import com.example.fintrack3.models.Transaction
import com.google.firebase.firestore.FirebaseFirestore

class TransactionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val transactionRef = db.collection("transactions")

    fun addTransaction(transaction: Transaction, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = transactionRef.document()
        val newTransaction = transaction.copy(transactionId = docRef.id)
        docRef.set(newTransaction)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getTransactionsForUser(userId: String, onSuccess: (List<Transaction>) -> Unit, onFailure: (Exception) -> Unit) {
        transactionRef.whereEqualTo("userId", userId).get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull { it.toObject(Transaction::class.java) }
                onSuccess(list)
            }
            .addOnFailureListener { onFailure(it) }
    }
}
