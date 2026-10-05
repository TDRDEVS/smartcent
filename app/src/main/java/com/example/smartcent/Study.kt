package com.example.smartcent

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Study : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, MainDashboard::class.java))
            finish() // optional: closes Study so user can’t return with back button
        }


        // Image thumbnails
        val video1Image = findViewById<ImageView>(R.id.video1Image)
        val video2Image = findViewById<ImageView>(R.id.video2Image)
        val video3Image = findViewById<ImageView>(R.id.video3Image)
        val video4Image = findViewById<ImageView>(R.id.video4Image)
        val video5Image = findViewById<ImageView>(R.id.video5Image)

        // YouTube links (replace with your actual links)
        val video1Url = "https://youtu.be/-bqeNE1DOzA?si=68ETtDo_eF0c1Zkp"
        val video2Url = "https://youtu.be/WBB3Ic8OlR0?si=SKikBz2ZHyr43b7-"
        val video3Url = "https://youtu.be/ErSyvThnakk?si=K_x93YB0jw6IcS8m"
        val video4Url = "https://youtu.be/RIuAbPGu1GQ?si=251FSvIgrVTdEsHM"
        val video5Url = "https://youtu.be/sVKQn2I4HDM?si=Tw07Un7n0O6dowe1"

        video1Image.setOnClickListener { openYoutube(video1Url) }
        video2Image.setOnClickListener { openYoutube(video2Url) }
        video3Image.setOnClickListener { openYoutube(video3Url) }
        video4Image.setOnClickListener { openYoutube(video4Url) }
        video5Image.setOnClickListener { openYoutube(video5Url) }

        // Button to start test
        val testButton = findViewById<Button>(R.id.startTestButton)
        testButton.setOnClickListener {
            startActivity(Intent(this, BudgetTest::class.java))
        }
    }

    private fun openYoutube(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
