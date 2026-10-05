package com.example.smartcent

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddIncome : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

        dbHelper = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        val amountEt = findViewById<EditText>(R.id.incomeAmount)
        val saveBtn = findViewById<Button>(R.id.saveIncomeButton)

        saveBtn.setOnClickListener {
            val amountStr = amountEt.text.toString().trim()
            val amount = amountStr.toDoubleOrNull()

            if (amount == null || amount <= 0) {
                amountEt.error = "Enter a valid amount"
            } else {
                // Run DB work off the main thread
                lifecycleScope.launch(Dispatchers.IO) {
                    val success = dbHelper.addIncome(userId, amount)
                    val incomeCount = dbHelper.getIncomeCount(userId)
                    val totalIncome = dbHelper.getTotalIncome(userId)

                    withContext(Dispatchers.Main) {
                        if (success) {
                            var message = "✅ Income saved successfully!"

                            // Milestones by count
                            when (incomeCount) {
                                1 -> {
                                    dbHelper.unlockAchievement(
                                        userId,
                                        "income",
                                        "First Income Logged"
                                    )
                                    message += "\n🎉 First Income Badge Unlocked!"
                                }

                                5 -> {
                                    dbHelper.unlockAchievement(userId, "income", "5 Income Entries")
                                    message += "\n⭐ 5 Income Badge Unlocked!"
                                }

                                20 -> {
                                    dbHelper.unlockAchievement(
                                        userId,
                                        "income",
                                        "20 Income Entries"
                                    )
                                    message += "\n🏆 Income Tracker Badge Unlocked!"
                                }
                            }

                            // Milestones by total income
                            when {
                                totalIncome >= 10000 && totalIncome < 50000 -> {
                                    dbHelper.unlockAchievement(
                                        userId,
                                        "income",
                                        "Reached R10,000 Total Income"
                                    )
                                    message += "\n💰 R10,000 Income Badge Unlocked!"
                                }

                                totalIncome >= 50000 && totalIncome < 100000 -> {
                                    dbHelper.unlockAchievement(
                                        userId,
                                        "income",
                                        "Reached R50,000 Total Income"
                                    )
                                    message += "\n💎 R50,000 Income Badge Unlocked!"
                                }

                                totalIncome >= 100000 -> {
                                    dbHelper.unlockAchievement(
                                        userId,
                                        "income",
                                        "Reached R100,000 Total Income"
                                    )
                                    message += "\n🥇 R100,000 Income Champion Badge Unlocked!"
                                }
                            }

                            Toast.makeText(this@AddIncome, message, Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@AddIncome,
                                "❌ Error saving income",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }
    }
