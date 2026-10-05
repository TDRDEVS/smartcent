package com.example.smartcent

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class BudgetTest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_test)

        val q1Group = findViewById<RadioGroup>(R.id.q1Group)
        val q2Group = findViewById<RadioGroup>(R.id.q2Group)
        val q3Group = findViewById<RadioGroup>(R.id.q3Group)
        val q4Group = findViewById<RadioGroup>(R.id.q4Group)
        val q5Group = findViewById<RadioGroup>(R.id.q5Group)
        val submitButton = findViewById<Button>(R.id.submitTestButton)

        submitButton.setOnClickListener {
            var score = 0

            val q1Selected = findViewById<RadioButton>(q1Group.checkedRadioButtonId)
            val q2Selected = findViewById<RadioButton>(q2Group.checkedRadioButtonId)
            val q3Selected = findViewById<RadioButton>(q3Group.checkedRadioButtonId)
            val q4Selected = findViewById<RadioButton>(q4Group.checkedRadioButtonId)
            val q5Selected = findViewById<RadioButton>(q5Group.checkedRadioButtonId)

            if (q1Selected?.text == "Tracking income and expenses") score++
            if (q2Selected?.text == "Needs before wants") score++
            if (q3Selected?.text == "Emergency fund") score++
            if (q4Selected?.text == "Helps avoid debt") score++
            if (q5Selected?.text == "Review and adjust regularly") score++

            val resultMessage = if (score >= 3) {
                "You scored $score/5. ✅ Pass!"
            } else {
                "You scored $score/5. ❌ Try again!"
            }

            Toast.makeText(this, resultMessage, Toast.LENGTH_LONG).show()
        }
    }
}
