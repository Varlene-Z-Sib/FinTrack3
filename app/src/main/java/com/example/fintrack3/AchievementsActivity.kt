package com.example.fintrack3

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AchievementsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var achievementAdapter: AchievementAdapter
    private val achievementList = mutableListOf<Achievement>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        recyclerView = findViewById(R.id.recyclerViewAchievements)
        recyclerView.layoutManager = LinearLayoutManager(this)
        achievementAdapter = AchievementAdapter(achievementList)
        recyclerView.adapter = achievementAdapter

        loadAchievementsFromFirestore()
    }

    private fun loadAchievementsFromFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("achievements")
            .get()
            .addOnSuccessListener { result ->
                achievementList.clear()
                for (document in result) {
                    val achievement = document.toObject(Achievement::class.java)
                    achievementList.add(achievement)
                }
                achievementAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load achievements: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}
