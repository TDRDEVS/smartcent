package com.example.smartcent

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Welcome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val signInButton = findViewById<Button>(R.id.signInButton)

        signUpButton.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        signInButton.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }
    }