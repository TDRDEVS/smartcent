package com.example.smartcent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.imageview.ShapeableImageView

class ViewProfile : AppCompatActivity() {

    private lateinit var profileImage: ShapeableImageView
    private lateinit var nameEdit: TextInputEditText
    private lateinit var emailEdit: TextInputEditText
    private lateinit var saveProfileButton: MaterialButton
    private lateinit var uploadImageButton: MaterialButton

    companion object {
        private const val PICK_IMAGE_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)

        profileImage = findViewById(R.id.profileImage)
        nameEdit = findViewById(R.id.nameEdit)
        emailEdit = findViewById(R.id.emailEdit)
        saveProfileButton = findViewById(R.id.saveProfileButton)
        uploadImageButton = findViewById(R.id.uploadImageButton)

        // Load saved data
        loadProfileData()

        // Upload button
        uploadImageButton.setOnClickListener {
            openImageChooser()
        }

        // Save button
        saveProfileButton.setOnClickListener {
            saveProfileData()
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                profileImage.setImageURI(imageUri)

                // Save image URI to SharedPreferences
                val prefs = getSharedPreferences("SmartCentPrefs", MODE_PRIVATE)
                prefs.edit().putString("profileImageUri", imageUri.toString()).apply()
            }
        }
    }

    private fun saveProfileData() {
        val name = nameEdit?.text?.toString()?.trim()
        val email = emailEdit?.text?.toString()?.trim()

        if (name.isNullOrEmpty() || email.isNullOrEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val prefs = getSharedPreferences("SmartCentPrefs", MODE_PRIVATE)
            prefs.edit()
                .putString("profileName", name)
                .putString("profileEmail", email)
                .apply()

            Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving profile: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadProfileData() {
        val prefs = getSharedPreferences("SmartCentPrefs", MODE_PRIVATE)
        val savedName = prefs.getString("profileName", "")
        val savedEmail = prefs.getString("profileEmail", "")
        val savedImageUri = prefs.getString("profileImageUri", null)

        nameEdit.setText(savedName)
        emailEdit.setText(savedEmail)

        if (savedImageUri != null) {
            profileImage.setImageURI(Uri.parse(savedImageUri))
        }
    }
}
