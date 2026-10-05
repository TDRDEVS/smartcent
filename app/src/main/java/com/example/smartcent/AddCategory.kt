package com.example.smartcent

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddCategory : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: ArrayAdapter<String>
    private val categoryList = ArrayList<String>()
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_category)


        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }




        dbHelper = DatabaseHelper(this)

        // Get logged-in userId from SharedPreferences
        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        userId = prefs.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "No user session found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }




        val categoryEt = findViewById<EditText>(R.id.categoryEditText)
        val addBtn = findViewById<Button>(R.id.addCategory)
        val listView = findViewById<ListView>(R.id.categoryListView)

        // Load categories for this user
        categoryList.addAll(dbHelper.getCategoriesForUser(userId))
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categoryList)
        listView.adapter = adapter

        addBtn.setOnClickListener {
            val category = categoryEt.text.toString().trim()

            when {
                category.isEmpty() -> categoryEt.error = "Category cannot be empty"
                category.length < 3 -> categoryEt.error = "Minimum 3 characters"
                categoryList.contains(category) -> categoryEt.error = "Category already exists"
                else -> {
                    val success = dbHelper.insertCategory(category, userId)
                    if (success) {
                        categoryList.clear()
                        categoryList.addAll(dbHelper.getCategoriesForUser(userId))
                        adapter.notifyDataSetChanged()
                        categoryEt.text.clear()

                        var message = "✅ Category added"

                        // 🔥 Gamification: unlock milestones
                        val categoryCount = dbHelper.getCategoryCount(userId)
                        when (categoryCount) {
                            1 -> {
                                dbHelper.unlockAchievement(userId, "categories", "First Category Created")
                                message += "\n🎉 First Category Badge Unlocked!"
                            }
                            5 -> {
                                dbHelper.unlockAchievement(userId, "categories", "5 Categories Organized")
                                message += "\n⭐ 5 Categories Badge Unlocked!"
                            }
                            10 -> {
                                dbHelper.unlockAchievement(userId, "categories", "10 Categories Master")
                                message += "\n🏆 Category Master Badge Unlocked!"
                            }
                            20 -> {
                                dbHelper.unlockAchievement(userId, "categories", "20 Categories Architect")
                                message += "\n🥇 Category Architect Badge Unlocked!"
                            }
                        }

                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }


                }
                    }
                }
            }
        }





