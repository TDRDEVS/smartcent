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

class Login : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val usernameEt = findViewById<EditText>(R.id.userNameEdit)
        val passwordEt = findViewById<EditText>(R.id.passwordEdit)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginButton.setOnClickListener {
            val username = usernameEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            when {
                username.isEmpty() -> usernameEt.error = "Username required"
                password.isEmpty() -> passwordEt.error = "Password required"
                else -> {
                    val valid = dbHelper.checkUser(username, password)
                    if (valid) {
                        val userId = dbHelper.getUserId(username) ?: -1
                        if (userId != -1) {
                            // Save session
                            val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
                            prefs.edit()
                                .putInt("user_id", userId)
                                .putString("username", username)
                                .apply()

                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                            // Navigate to dashboard
                            val intent = Intent(this, MainDashboard::class.java)
                            intent.putExtra("USER_ID", userId)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Error fetching user ID", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }
        }

