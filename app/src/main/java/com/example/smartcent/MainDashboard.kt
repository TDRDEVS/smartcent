package com.example.smartcent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import android.widget.Button
import com.google.android.material.imageview.ShapeableImageView

class MainDashboard : AppCompatActivity() {

    private lateinit var thisMonthText: TextView
    private lateinit var expensesText: TextView
    private lateinit var incomeText: TextView
    private lateinit var goalsText: TextView
    private lateinit var welcomeText: TextView
    private lateinit var dashboardProfileImage: ShapeableImageView
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_dashboard)

        // Bind views
        dashboardProfileImage = findViewById(R.id.dashboardProfileImage)
        thisMonthText = findViewById(R.id.thisMonthText)
        expensesText = findViewById(R.id.expensesText)
        goalsText = findViewById(R.id.goalsText)
        incomeText = findViewById(R.id.incomeText)
        welcomeText = findViewById(R.id.welcomeText)

        // Load profile image + name initially
        loadProfileImage()
        loadProfileName()

        // Make profile image clickable → open ViewProfile
        dashboardProfileImage.setOnClickListener {
            startActivity(Intent(this, ViewProfile::class.java))
        }

        // Get userId from Intent
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show()
            finish()
        }



        findViewById<ImageButton>(R.id.budgetProgressButton).setOnClickListener {
            startActivity(Intent(this, BudgetProgress::class.java).apply {
                putExtra("USER_ID", userId)
            })
        }
        findViewById<ImageButton>(R.id.incomeButton).setOnClickListener {
            startActivity(Intent(this, AddIncome::class.java).apply {
                putExtra("USER_ID", userId)
            })
        }
        findViewById<ImageButton>(R.id.addExButton).setOnClickListener {
            startActivity(Intent(this, AddExpense::class.java).apply {
                putExtra("USER_ID", userId)
            })
        }
        findViewById<ImageButton>(R.id.category).setOnClickListener {
            startActivity(Intent(this, AddCategory::class.java).apply {
                putExtra("USER_ID", userId)
            })
        }
        findViewById<ImageButton>(R.id.ViewReports).setOnClickListener {
            startActivity(Intent(this, ViewReports::class.java).apply {
                putExtra("USER_ID", userId)
            })
        }
        findViewById<ImageButton>(R.id.setGoals).setOnClickListener {
            startActivity(Intent(this, SetGoals::class.java).apply {
                putExtra("USER_ID", userId)
            })
        }
        findViewById<ImageButton>(R.id.expenseListView).setOnClickListener {
            startActivity(Intent(this, ExpenseList::class.java).apply {
                putExtra("USER_ID", userId)
            })
        }
        findViewById<ImageButton>(R.id.Logout).setOnClickListener {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
        }
        findViewById<Button>(R.id.studyButton).setOnClickListener {
            startActivity(Intent(this, Study::class.java))
        }
        findViewById<ImageButton>(R.id.viewAchievements).setOnClickListener {
            startActivity(Intent(this, Achievements::class.java).apply {
                putExtra("USER_ID", userId)
            })
        }


    // Monthly expenses click handler (unchanged)
    thisMonthText.setOnClickListener {
        try {
            val dbHelper = DatabaseHelper(this)
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            val startOfMonth = String.format(
                "%04d-%02d-%02d",
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH) + 1,
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            )
            calendar.set(
                java.util.Calendar.DAY_OF_MONTH,
                calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            )
            val endOfMonth = String.format(
                "%04d-%02d-%02d",
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH) + 1,
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            )
            val monthlyExpenses = dbHelper.getExpensesByDateRange(userId, startOfMonth, endOfMonth)
            val totalMonthly = monthlyExpenses.sumOf { it.amount }
            expensesText.text = String.format(Locale.US, "Expenses (This Month): R%.2f", totalMonthly)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Unable to load monthly expenses", Toast.LENGTH_SHORT).show()
        }
    }
}

    private fun loadProfileImage() {
        val prefs = getSharedPreferences("SmartCentPrefs", MODE_PRIVATE)
        val savedImageUri = prefs.getString("profileImageUri", null)

        if (!savedImageUri.isNullOrEmpty()) {
            try {
                dashboardProfileImage.setImageURI(Uri.parse(savedImageUri))
            } catch (e: Exception) {
                dashboardProfileImage.setImageResource(R.drawable.ic_profile) // fallback
            }
        } else {
            dashboardProfileImage.setImageResource(R.drawable.ic_profile) // default placeholder
        }
    }

    private fun loadProfileName() {
        val prefs = getSharedPreferences("SmartCentPrefs", MODE_PRIVATE)
        val savedName = prefs.getString("profileName", null)

        welcomeText.text = if (!savedName.isNullOrEmpty()) {
            "Welcome back, $savedName 🪙"
        } else {
            "Welcome back 🪙"
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh profile image + name when returning from ViewProfile
        loadProfileImage()
        loadProfileName()

        if (userId != -1) {
            val dbHelper = DatabaseHelper(this)
            val totalExpenses = dbHelper.getTotalExpenses(userId)
            val totalIncome = dbHelper.getTotalIncome(userId)
            val budgetGoal = try {
                dbHelper.getBudgetGoal(userId)
            } catch (e: Exception) {
                0.0
            }
            expensesText.text = String.format(Locale.US, "Total Expenses: R%.2f", totalExpenses)
            goalsText.text = String.format(Locale.US, "Budget Goal: R%.2f", budgetGoal)
            incomeText.text = String.format(Locale.US, "Income: R%.2f", totalIncome)
        }
    }
}
