package com.example.smartcent

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class Achievements : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        dbHelper = DatabaseHelper(this)

        // Get logged-in userId from SharedPreferences
        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        userId = prefs.getInt("user_id", -1)

        if (userId == -1) {
            finish()
            return
        }

        val achievementsList = findViewById<LinearLayout>(R.id.achievementsList)

        // Load achievements from DB
        val achievements = dbHelper.getAchievements(userId)
        val emptyMessage = findViewById<TextView>(R.id.emptyMessage)
        val sectionTitle = findViewById<TextView>(R.id.achievementsSectionTitle)
        val placeholder = findViewById<TextView>(R.id.placeholderText)


        if (achievements.isEmpty()) {
            emptyMessage.visibility = View.VISIBLE
            sectionTitle.visibility = View.GONE
            placeholder.visibility = View.VISIBLE
        } else {
            emptyMessage.visibility = View.GONE
            sectionTitle.visibility = View.VISIBLE
            placeholder.visibility = View.GONE
            // populate achievementsList dynamically
        }



        for ((feature, milestone) in achievements) {
            val card = layoutInflater.inflate(R.layout.item_achievement, achievementsList, false)

            val badgeIcon = card.findViewById<ImageView>(R.id.badgeIcon)
            val badgeTitle = card.findViewById<TextView>(R.id.badgeTitle)

            // Set badge title
            badgeTitle.text = "$feature: $milestone"

            // Choose icon based on feature
            when (feature) {
                "expenses" -> badgeIcon.setImageResource(R.drawable.bagde_expense)
                "categories" -> badgeIcon.setImageResource(R.drawable.badge_category)
                "income" -> badgeIcon.setImageResource(R.drawable.badge_income)
                "budgets" -> badgeIcon.setImageResource(R.drawable.badge_goal)
                "reports" -> badgeIcon.setImageResource(R.drawable.badge_report)
                else -> badgeIcon.setImageResource(R.drawable.badge_default)
            }

            achievementsList.addView(card)
        }
    }
}
