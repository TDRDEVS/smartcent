package com.example.smartcent

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.widget.Button


class BudgetProgress : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_progress)

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()   // go back to the existing MainDashboard
        }



        dbHelper = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val progressText = findViewById<TextView>(R.id.progressText)

        // Get values from DB
        val income = dbHelper.getTotalIncome(userId)
        val totalExpenses = dbHelper.getTotalExpenses(userId)
        val budgetGoal = dbHelper.getBudgetGoal(userId)
        val budgetMax = dbHelper.getBudgetMax(userId)

        // Formula: Remaining = Income - Expenses
        val remaining = income - totalExpenses

        // Calculate target progress
        val targetProgress = if (budgetGoal > 0) {
            ((remaining / budgetGoal) * 100).toInt().coerceIn(0, 100)
        } else 0

// Animate smoothly from current progress to target
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, targetProgress).apply {
            duration = 1000 // 1 second animation
            interpolator = android.view.animation.DecelerateInterpolator()
            start()
        }

// Update text alongside animation
        progressText.text = "Progress: $targetProgress% of goal"


        }
    }

