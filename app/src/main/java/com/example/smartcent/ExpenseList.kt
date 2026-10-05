package com.example.smartcent

import ExpenseAdapter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.DatePickerDialog
import android.content.Intent
import java.util.Calendar

class ExpenseList : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var startDate: EditText
    private lateinit var endDate: EditText
    private lateinit var filterButton: Button
    private var userId: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_list)

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }



        dbHelper = DatabaseHelper(this)
        listView = findViewById(R.id.expenseListView)
        startDate = findViewById(R.id.startDate)
        endDate = findViewById(R.id.endDate)
        filterButton = findViewById(R.id.filterButton)

        // Get logged-in userId from SharedPreferences
        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        userId = prefs.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "No user session found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Date pickers
        startDate.setOnClickListener { showDatePicker(startDate) }
        endDate.setOnClickListener { showDatePicker(endDate) }

        // show all expenses for user
        loadExpenses(dbHelper.getExpensesForUser(userId))

        // Filter button
        filterButton.setOnClickListener {
            val start = startDate.text.toString().trim()
            val end = endDate.text.toString().trim()
            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show()
            } else {
                val filtered = dbHelper.getExpensesByDateRange(userId, start, end)
                loadExpenses(filtered)
            }
        }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                // Format as YYYY-MM-DD with leading zeros
                val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
                editText.setText(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }


    private fun loadExpenses(expenses: List<DatabaseHelper.Expense>) {
        if (expenses.isEmpty()) {
            Toast.makeText(this, "No expenses found", Toast.LENGTH_SHORT).show()
        }

        val adapter = ExpenseAdapter(this, expenses)
        listView.adapter = adapter
    }




}
