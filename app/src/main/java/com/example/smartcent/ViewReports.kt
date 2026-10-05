package com.example.smartcent

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class ViewReports : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var startDate: EditText
    private lateinit var endDate: EditText
    private lateinit var filterButton: Button
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_reports)



        dbHelper = DatabaseHelper(this)
        dbHelper.normalizeOldDates()
        listView = findViewById(R.id.reportListView)
        startDate = findViewById(R.id.startDate)
        endDate = findViewById(R.id.endDate)
        filterButton = findViewById(R.id.filterButton)
        val viewPieChartButton = findViewById<Button>(R.id.viewPieChartButton)

        // Get logged-in userId from SharedPreferences
        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        userId = prefs.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "No user session found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }



        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }







        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }








        // Date pickers
        startDate.setOnClickListener { showDatePicker(startDate) }
        endDate.setOnClickListener { showDatePicker(endDate) }

        // Default: show all reports for this user
        loadReports(dbHelper.getExpensesForUser(userId))

        // Filter button
        filterButton.setOnClickListener {
            val start = startDate.text.toString().trim()
            val end = endDate.text.toString().trim()
            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show()
            } else {
                val filtered = dbHelper.getExpensesByDateRange(userId, start, end)

                Toast.makeText(this, "Found ${filtered.size} expenses", Toast.LENGTH_SHORT).show()

                // Debug loop to inspect what’s actually stored
                val allExpenses = dbHelper.getExpensesForUser(userId)
                for (exp in allExpenses) {
                    Log.d("DEBUG_EXPENSE", "Date stored: ${exp.date}")
                }

                loadReports(filtered)
            }
        }




// Pie chart button
        viewPieChartButton.setOnClickListener {
            val start = startDate.text.toString().trim()
            val end = endDate.text.toString().trim()

            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show()
            } else {
                val fragment = SpendingGraphFragment.newInstance(userId, start, end)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }


    private fun showDatePicker(editText: EditText) {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                // Format as YYYY-MM-DD
                val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
                editText.setText(formatted)
            },
            2026, 0, 1
        ).show()


    }




    private fun loadReports(expenses: List<DatabaseHelper.Expense>) {
        val totals = mutableMapOf<String, Double>()
        var grandTotal = 0.0

        for (expense in expenses) {
            totals[expense.category] = totals.getOrDefault(expense.category, 0.0) + expense.amount
            grandTotal += expense.amount
        }

        val displayList = totals.map {
            "${it.key}: R${"%.2f".format(it.value)}"
        }.toMutableList()

        // Add grand total line at the bottom
        displayList.add("Grand Total: R${"%.2f".format(grandTotal)}")

        if (displayList.isEmpty()) {
            Toast.makeText(this, "No data to display", Toast.LENGTH_SHORT).show()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
        listView.adapter = adapter
    }



}
