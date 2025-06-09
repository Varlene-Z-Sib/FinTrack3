package com.example.fintrack3

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlinx.coroutines.tasks.await


class GraphActivity : AppCompatActivity() {

    private var userId: String? = null
    private lateinit var db: FirebaseFirestore

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var periodSpinner: Spinner
    private lateinit var tvNoData: TextView
    private lateinit var tvNoDataBar: TextView

    private val periods = arrayOf("Today", "This Week", "This Month", "This Year")
    private var selectedPeriod = "This Month"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        userId = intent.getStringExtra("USER_ID")
        db = FirebaseFirestore.getInstance()

        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.dailyBarChart)
        periodSpinner = findViewById(R.id.spinnerPeriod)
        tvNoData = findViewById(R.id.tvNoData)
        tvNoDataBar = findViewById(R.id.tvNoDataBar)

        setupPeriodSpinner()
        setupPieChart()
        setupBarChart()
        setupBottomNavigation()

        loadChartData(selectedPeriod)
    }

    private fun setupPeriodSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        periodSpinner.adapter = adapter
        periodSpinner.setSelection(2)
        periodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedPeriod = periods[position]
                loadChartData(selectedPeriod)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            centerText = "Category\nSpending"
            setCenterTextSize(16f)
            setUsePercentValues(true)
            legend.isEnabled = true
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
        }
    }

    private fun setupBarChart() {
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(true)
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            legend.isEnabled = true
            animateY(1000)
        }
    }

    private fun loadChartData(period: String) {
        if (userId == null) return

        val (startDate, endDate) = getDateRange()

        CoroutineScope(Dispatchers.IO).launch {
            val categorySpending = mutableMapOf<String, Double>()
            val dailySpending = mutableMapOf<String, Double>()

            val snapshot = db.collection("transactions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", "Expense")
                .whereGreaterThanOrEqualTo("date", Timestamp(startDate))
                .whereLessThanOrEqualTo("date", Timestamp(endDate))
                .get()
                .await()

            for (doc in snapshot.documents) {
                val category = doc.getString("category") ?: continue
                val amount = doc.getDouble("amount") ?: 0.0
                val date = doc.getTimestamp("date")?.toDate() ?: continue

                categorySpending[category] = categorySpending.getOrDefault(category, 0.0) + amount

                val dayKey = SimpleDateFormat("MM/dd", Locale.getDefault()).format(date)
                dailySpending[dayKey] = dailySpending.getOrDefault(dayKey, 0.0) + amount
            }

            withContext(Dispatchers.Main) {
                updatePieChart(categorySpending)
                updateBarChart(dailySpending)
            }
        }
    }

    private fun updatePieChart(categorySpending: Map<String, Double>) {
        if (categorySpending.isEmpty()) {
            pieChart.visibility = View.GONE
            tvNoData.visibility = View.VISIBLE
            return
        }

        val entries = categorySpending.map { PieEntry(it.value.toFloat(), it.key) }

        val dataSet = PieDataSet(entries, "Categories").apply {
            colors = getChartColors(entries.size)
            valueTextSize = 14f
            valueTextColor = Color.BLACK
            sliceSpace = 3f
            selectionShift = 5f
        }

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(pieChart))
        pieChart.data = pieData
        pieChart.visibility = View.VISIBLE
        tvNoData.visibility = View.GONE
        pieChart.invalidate()
        pieChart.animateY(1000)
    }

    private fun updateBarChart(dailySpending: Map<String, Double>) {
        if (dailySpending.isEmpty()) {
            barChart.visibility = View.GONE
            tvNoDataBar.visibility = View.VISIBLE
            return
        }

        val sorted = dailySpending.toSortedMap()
        val entries = sorted.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }
        val labels = sorted.keys.toList()

        val dataSet = BarDataSet(entries, "Daily Spending").apply {
            color = ColorTemplate.rgb("#4CAF50")
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        val barData = BarData(dataSet)
        barData.barWidth = when {
            entries.size > 20 -> 0.4f
            entries.size > 10 -> 0.6f
            else -> 0.8f
        }

        barChart.data = barData
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.visibility = View.VISIBLE
        tvNoDataBar.visibility = View.GONE
        barChart.invalidate()
        barChart.animateY(1000)
    }

    private fun getDateRange(): Pair<Date, Date> {
        val startCal = Calendar.getInstance()
        val endCal = Calendar.getInstance()

        when (selectedPeriod) {
            "Today" -> {
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                endCal.set(Calendar.HOUR_OF_DAY, 23)
            }
            "This Week" -> {
                startCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
                endCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek + 6)
            }
            "This Month" -> {
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
            }
            "This Year" -> {
                startCal.set(Calendar.MONTH, Calendar.JANUARY)
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                endCal.set(Calendar.MONTH, Calendar.DECEMBER)
                endCal.set(Calendar.DAY_OF_MONTH, 31)
            }
        }

        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        endCal.set(Calendar.MINUTE, 59)
        endCal.set(Calendar.SECOND, 59)

        return Pair(startCal.time, endCal.time)
    }

    private fun getChartColors(count: Int): List<Int> {
        val baseColors = ColorTemplate.MATERIAL_COLORS.toList() + ColorTemplate.VORDIPLOM_COLORS.toList()
        val result = ArrayList(baseColors)
        while (result.size < count) {
            result.add(Color.rgb((0..255).random(), (0..255).random(), (0..255).random()))
        }
        return result
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainPage::class.java).apply {
                        putExtra("USER_ID", userId)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    })
                    true
                }
                R.id.nav_transactions -> {
                    startActivity(Intent(this, TransactionPage::class.java).apply {
                        putExtra("USER_ID", userId)
                    })
                    true
                }
                R.id.nav_analysis -> true
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.nav_analysis
    }
}
