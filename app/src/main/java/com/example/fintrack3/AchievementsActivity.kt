package com.example.fintrack3

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
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

    // TextViews and ProgressBar
    private lateinit var tvUserLevel: TextView
    private lateinit var tvUserPoints: TextView
    private lateinit var tvNextLevel: TextView
    private lateinit var progressLevel: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        // Bind TextViews and ProgressBar
        tvUserLevel = findViewById(R.id.tvUserLevel)
        tvUserPoints = findViewById(R.id.tvUserPoints)
        tvNextLevel = findViewById(R.id.tvNextLevel)
        progressLevel = findViewById(R.id.progressLevel)

        recyclerView = findViewById(R.id.recyclerViewAchievements)
        recyclerView.layoutManager = LinearLayoutManager(this)
        achievementAdapter = AchievementAdapter(achievementList)
        recyclerView.adapter = achievementAdapter

        // Load user achievement summary (replace with real data fetching)
        loadUserPointsAndLevel()

        // Load achievements from Firestore
        loadAchievementsFromFirestore()
    }

    private fun loadUserPointsAndLevel() {
        // TODO: Replace these with your real user data fetching from Firestore or other sources
        val currentLevel = 1
        val totalPoints = 0
        val pointsToNextLevel = 100
        val nextLevel = currentLevel + 1
        val progressPercent = if (pointsToNextLevel != 0) (totalPoints * 100) / pointsToNextLevel else 0

        // Set dynamic text using string resources
        tvUserLevel.text = getString(R.string.user_level, currentLevel)
        tvUserPoints.text = getString(R.string.total_points, totalPoints)
        tvNextLevel.text = getString(R.string.next_level_message, pointsToNextLevel, nextLevel)
        progressLevel.progress = progressPercent
    }

    private fun loadAchievementsFromFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, getString(R.string.load_achievements_failed, exception.message), Toast.LENGTH_LONG).show()
            }
    }
}
