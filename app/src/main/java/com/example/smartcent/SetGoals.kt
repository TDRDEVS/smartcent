package com.example.smartcent

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetGoals : AppCompatActivity() {
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_set_goals)

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }



        // Get userId from Intent
        userId = intent.getIntExtra("USER_ID", -1)

        val minEt = findViewById<EditText>(R.id.minimumBudget)
        val maxEt = findViewById<EditText>(R.id.maxBudget)
        val saveBtn = findViewById<Button>(R.id.SaveBudgetButton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        saveBtn.setOnClickListener {
            val min = minEt.text.toString().trim()
            val max = maxEt.text.toString().trim()

            when {
                min.isEmpty() -> minEt.error = "Enter minimum amount"
                max.isEmpty() -> maxEt.error = "Enter maximum amount"
                min.toDoubleOrNull() == null -> minEt.error = "Invalid number"
                max.toDoubleOrNull() == null -> maxEt.error = "Invalid number"
                min.toDouble().let { it < 0 } -> minEt.error = "Cannot be negative"
                max.toDouble().let { it <= 0 } -> maxEt.error = "Must be greater than 0"
                min.toDouble() > max.toDouble() -> maxEt.error = "Max must be greater than Min"
                else -> {
                    val minBudget = min.toDouble()
                    val maxBudget = max.toDouble()

                    lifecycleScope.launch(Dispatchers.IO) {
                        val dbHelper = DatabaseHelper(this@SetGoals)
                        val success = dbHelper.saveBudgetGoal(userId, minBudget, maxBudget)

                        if (success) {
                            val goalCount = dbHelper.getGoalCount(userId)
                            val progress = dbHelper.getGoalProgress(userId)

                            var message = "✅ Budget goals saved"

                            // Milestones by count
                            when (goalCount) {
                                1 -> {
                                    dbHelper.unlockAchievement(userId, "budgets", "First Goal Set")
                                    message += "\n🎉 First Goal Badge Unlocked!"
                                }

                                3 -> {
                                    dbHelper.unlockAchievement(userId, "budgets", "3 Goals Planner")
                                    message += "\n⭐ 3 Goals Badge Unlocked!"
                                }

                                5 -> {
                                    dbHelper.unlockAchievement(
                                        userId,
                                        "budgets",
                                        "5 Goals Strategist"
                                    )
                                    message += "\n🏆 Goal Strategist Badge Unlocked!"
                                }
                            }

                            // Milestones by progress
                            when {
                                progress >= 50 && progress < 100 -> {
                                    dbHelper.unlockAchievement(
                                        userId,
                                        "budgets",
                                        "Reached 50% of Goal"
                                    )
                                    message += "\n💰 Halfway to Goal Badge!"
                                }

                                progress >= 100 -> {
                                    dbHelper.unlockAchievement(userId, "budgets", "Goal Achieved")
                                    message += "\n🥇 Goal Achieved Badge!"
                                }
                            }

                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@SetGoals, message, Toast.LENGTH_LONG).show()
                                finish()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@SetGoals,
                                    "❌ Error saving budget goals",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }
}