package com.example.smartcent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class AddExpense : AppCompatActivity() {
    private var imageUri: Uri? = null
    private lateinit var photoPreview: ImageView
    private lateinit var addPhotoButton: Button
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
        photoPreview.setImageURI(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_expense)

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

        val dateEditText = findViewById<EditText>(R.id.dateEditText)
        val startTime = findViewById<EditText>(R.id.startTime)
        val endTime = findViewById<EditText>(R.id.endTime)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val descriptionEdit = findViewById<EditText>(R.id.descriptionEdit)
        val amountInput = findViewById<EditText>(R.id.Amountinput)
        val saveButton = findViewById<Button>(R.id.saveButton)

        photoPreview = findViewById(R.id.photoPreview)
        addPhotoButton = findViewById(R.id.addPhotoButton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Photo picker
        addPhotoButton.setOnClickListener {
            if (photoPreview.visibility == View.VISIBLE) {
                photoPreview.visibility = View.GONE
            } else {
                photoPreview.visibility = View.VISIBLE
                if (imageUri == null) {
                    imagePicker.launch("image/*")
                }
            }
        }

        // Date picker
        dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    dateEditText.setText("$day/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }


        // Time picker helper
        fun showTimePicker(editText: EditText) {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    editText.setText(String.format("%02d:%02d", hour, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        startTime.setOnClickListener { showTimePicker(startTime) }
        endTime.setOnClickListener { showTimePicker(endTime) }

        // Load categories for this user
        val categories = dbHelper.getCategoriesForUser(userId)
        if (categories.isEmpty()) {
            Toast.makeText(this, "No categories found. Please add some first.", Toast.LENGTH_SHORT)
                .show()
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Save expense
        saveButton.setOnClickListener {
            val date = dateEditText.text.toString().trim()
            val start = startTime.text.toString().trim()
            val end = endTime.text.toString().trim()
            val description = descriptionEdit.text.toString().trim()
            val amountText = amountInput.text.toString().trim()

            when {
                date.isEmpty() -> {
                    dateEditText.error = "Date required"; return@setOnClickListener
                }

                start.isEmpty() -> {
                    startTime.error = "Start time required"; return@setOnClickListener
                }

                end.isEmpty() -> {
                    endTime.error = "End time required"; return@setOnClickListener
                }

                description.isEmpty() -> {
                    descriptionEdit.error = "Description required"; return@setOnClickListener
                }

                amountText.isEmpty() -> {
                    amountInput.error = "Amount required"; return@setOnClickListener
                }

                else -> {
                    try {
                        val amount = amountText.toDouble()
                        val category = categorySpinner.selectedItem?.toString() ?: ""
                        val photo = imageUri?.toString()

                        val success = dbHelper.insertExpense(
                            date,
                            start,
                            end,
                            description,
                            amount,
                            category,
                            photo,
                            userId
                        )


                        if (success) {
                            var message = "✅ Expense saved successfully!"
                            // Existing expense total tracking
                            val prefs = getSharedPreferences("SmartCentPrefs", MODE_PRIVATE)
                            val editor = prefs.edit()
                            val currentTotal = prefs.getFloat("TOTAL_EXPENSES", 0f)
                            editor.putFloat("TOTAL_EXPENSES", currentTotal + amount.toFloat())
                            editor.apply()

                            //  Gamification
                            val expenseCount = dbHelper.getExpenseCount(userId)
                            when (expenseCount) {
                                1 -> {
                                    dbHelper.unlockAchievement(userId, "expenses", "First Expense Logged")
                                    Toast.makeText(this, "🎉 First Expense Badge Unlocked!", Toast.LENGTH_SHORT).show()
                                }
                                10 -> {
                                    dbHelper.unlockAchievement(userId, "expenses", "10 Expenses Tracker")
                                    Toast.makeText(this, "⭐ 10 Expenses Badge Unlocked!", Toast.LENGTH_SHORT).show()
                                }
                                50 -> {
                                    dbHelper.unlockAchievement(userId, "expenses", "50 Expenses Milestone")
                                    Toast.makeText(this, "🏆 Expense Master Badge Unlocked!", Toast.LENGTH_LONG).show()
                                }
                            }


                            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Error saving expense", Toast.LENGTH_SHORT).show()
                        }

                    }catch (e: Exception) {

                        amountInput.error = "Invalid amount"

                    }
                    }
                }
            }
        }
    }


