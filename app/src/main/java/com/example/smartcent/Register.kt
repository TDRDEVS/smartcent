package com.example.smartcent

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Register : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        val registerButton = findViewById<Button>(R.id.registerButton)
        val usernameEt = findViewById<EditText>(R.id.userNameEdit)
        val passwordEt = findViewById<EditText>(R.id.passwordEdit)
        val confirmPasswordEt = findViewById<EditText>(R.id.confirmPasswordEdit)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        registerButton.setOnClickListener {
            val username = usernameEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()
            val confirmPassword = confirmPasswordEt.text.toString().trim()

            when {
                username.isEmpty() -> usernameEt.error = "Username required"
                password.isEmpty() -> passwordEt.error = "Password required"
                password.length < 6 -> passwordEt.error = "Password must be at least 6 characters"
                confirmPassword.isEmpty() -> confirmPasswordEt.error = "Confirm your password"
                password != confirmPassword -> confirmPasswordEt.error = "Passwords do not match"
                else -> {
                    val success = dbHelper.insertUser(username, password)
                    if (success) {
                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Login::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
