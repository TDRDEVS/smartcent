package com.example.smartcent

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class SpendingGraphFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1
    private var startDate: String = ""
    private var endDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt("user_id")
            startDate = it.getString("start_date") ?: ""
            endDate = it.getString("end_date") ?: ""
        }
        dbHelper = DatabaseHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_spending_graph, container, false)
        val pieChartView = view.findViewById<SimplePieChartView>(R.id.pieChartView)

        // Get spending data
        val data = dbHelper.getSpendingByCategory(userId, startDate, endDate)


        val minGoal = dbHelper.getBudgetGoal(userId)   // your "goal"
        val maxGoal = dbHelper.getBudgetMax(userId)


        // Convert to Map<String, Double>
        val chartData = data.associate { it.category to it.total }
        pieChartView.setData(chartData, minGoal, maxGoal)
        val totalSpent = chartData.values.sum()

        return view
    }

    companion object {
        fun newInstance(userId: Int, startDate: String, endDate: String) =
            SpendingGraphFragment().apply {
                arguments = Bundle().apply {
                    putInt("user_id", userId)
                    putString("start_date", startDate)
                    putString("end_date", endDate)
                }
            }
    }
}
