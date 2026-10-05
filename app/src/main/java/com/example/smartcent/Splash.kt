package com.example.smartcent

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Simple splash delay, then go to Welcome
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@Splash, Welcome::class.java))
            finish()
        }, 2000) // 2‑second splash delay
    }
}
