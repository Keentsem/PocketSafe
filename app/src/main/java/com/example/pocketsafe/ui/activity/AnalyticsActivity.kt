package com.example.pocketsafe.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R
import com.example.pocketsafe.ui.MainMenu
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.example.pocketsafe.ui.adapter.CategoryLegendAdapter
import com.example.pocketsafe.ui.adapter.CategoryLegendItem
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.pocketsafe.data.AppDatabase
import com.example.pocketsafe.data.BillSplit

/**
 * Analytics Activity - Displays comprehensive analytics dashboard
 * Features budget status, spending breakdown, MySplit analytics, and South African context
 */
class AnalyticsActivity : AppCompatActivity() {

    private lateinit var tvAnalyticsTitle: TextView
    private lateinit var tvBudgetStatusText: TextView
    private lateinit var tvBudgetAmount: TextView
    private lateinit var tvBudgetPercentage: TextView
    private lateinit var imgBudgetStatusIcon: ImageView
    private lateinit var tvTotalSplitBills: TextView
    private lateinit var tvTotalSplitAmount: TextView
    private lateinit var tvAverageSplit: TextView
    private lateinit var tvBraaiAmount: TextView
    private lateinit var tvTaxiAmount: TextView
    private lateinit var tvRentAmount: TextView
    private lateinit var flDownloadAnimation: View
    private lateinit var rvCategoryLegend: RecyclerView
    private lateinit var rvMySplitActivities: RecyclerView
    private lateinit var rvCardUsage: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        Log.d("AnalyticsActivity", "Analytics Activity created successfully")

        initializeViews()
        setupPixelFont()
        setupPieChart()
        setupNavigationBar()
        loadAnalyticsData()
    }

    private fun initializeViews() {
        try {
            // Header
            tvAnalyticsTitle = findViewById(R.id.tvAnalyticsTitle)
            
            // Budget Status
            tvBudgetStatusText = findViewById(R.id.tvBudgetStatusText)
            tvBudgetAmount = findViewById(R.id.tvBudgetAmount)
            tvBudgetPercentage = findViewById(R.id.tvBudgetPercentage)
            imgBudgetStatusIcon = findViewById(R.id.imgBudgetStatusIcon)
            
            // MySplit Analytics
            tvTotalSplitBills = findViewById(R.id.tvTotalSplitBills)
            tvTotalSplitAmount = findViewById(R.id.tvTotalSplitAmount)
            tvAverageSplit = findViewById(R.id.tvAverageSplit)
            
            // South African Context
            tvBraaiAmount = findViewById(R.id.tvBraaiAmount)
            tvTaxiAmount = findViewById(R.id.tvTaxiAmount)
            tvRentAmount = findViewById(R.id.tvRentAmount)
            
            // Animation
            flDownloadAnimation = findViewById(R.id.flDownloadAnimation)
            
            // RecyclerViews
            rvCategoryLegend = findViewById(R.id.rvCategoryLegend)
            rvMySplitActivities = findViewById(R.id.rvMySplitActivities)
            rvCardUsage = findViewById(R.id.rvCardUsage)
            
            // Bottom Navigation
            bottomNavigation = findViewById(R.id.bottom_navigation)
            
            // Pie Chart
            pieChart = findViewById(R.id.pieChart)
            
            // Setup RecyclerViews
            rvCategoryLegend.layoutManager = LinearLayoutManager(this)
            rvMySplitActivities.layoutManager = LinearLayoutManager(this)
            rvCardUsage.layoutManager = LinearLayoutManager(this)
            
            Log.d("AnalyticsActivity", "All views initialized successfully")
        } catch (e: Exception) {
            Log.e("AnalyticsActivity", "Error initializing views: ${e.message}")
        }
    }

    private fun setupPixelFont() {
        try {
            val pixelFont = ResourcesCompat.getFont(this, R.font.pixel_game)
            
            // Apply pixel font to all TextViews
            val textViews = listOf(
                tvAnalyticsTitle,
                tvBudgetStatusText,
                tvBudgetAmount,
                tvBudgetPercentage,
                tvTotalSplitBills,
                tvTotalSplitAmount,
                tvAverageSplit,
                tvBraaiAmount,
                tvTaxiAmount,
                tvRentAmount
            )
            
            textViews.forEach { textView ->
                textView.typeface = pixelFont
            }
            
            Log.d("AnalyticsActivity", "Pixel font applied successfully")
        } catch (e: Exception) {
            Log.e("AnalyticsActivity", "Error applying pixel font: ${e.message}")
        }
    }

    private fun setupPieChart() {
        try {
            pieChart.setUsePercentValues(true)
            pieChart.description.isEnabled = false
            pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
            pieChart.dragDecelerationFrictionCoef = 0.95f
            pieChart.isDrawHoleEnabled = false
            pieChart.setHoleColor(Color.WHITE)
            pieChart.setTransparentCircleColor(Color.WHITE)
            pieChart.setTransparentCircleAlpha(110)
            pieChart.holeRadius = 58f
            pieChart.transparentCircleRadius = 61f
            pieChart.setDrawCenterText(true)
            pieChart.rotationAngle = 0f
            pieChart.isRotationEnabled = true
            pieChart.isHighlightPerTapEnabled = true
            
            Log.d("AnalyticsActivity", "Pie chart setup completed")
        } catch (e: Exception) {
            Log.e("AnalyticsActivity", "Error setting up pie chart: ${e.message}")
        }
    }

    private fun setupNavigationBar() {
        try {
            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        Log.d("AnalyticsActivity", "Home navigation clicked")
                        navigateToMainMenu()
                        true
                    }
                    R.id.navigation_expenses -> {
                        Log.d("AnalyticsActivity", "Banking navigation clicked")
                        navigateToBanking()
                        true
                    }
                    else -> false
                }
            }
            
            Log.d("AnalyticsActivity", "Navigation bar setup completed")
        } catch (e: Exception) {
            Log.e("AnalyticsActivity", "Error setting up navigation bar: ${e.message}")
        }
    }

    private fun loadAnalyticsData() {
        try {
            // Load budget status
            updateBudgetStatus()
            
            // Load MySplit analytics from database
            lifecycleScope.launch {
                val billSplits = withContext(Dispatchers.IO) {
                    AppDatabase.getDatabase(this@AnalyticsActivity).billSplitDao().getAllBillSplits()
                }
                updateMySplitAnalytics(billSplits)
            }
            
            // Load South African spending context
            updateSouthAfricanContext()
            
            // Load pie chart data
            updatePieChartData()
            
            // Load category legend data
            updateCategoryLegendData()
            
            Log.d("AnalyticsActivity", "Analytics data loaded successfully")
        } catch (e: Exception) {
            Log.e("AnalyticsActivity", "Error loading analytics data: ${e.message}")
        }
    }

    private fun updateBudgetStatus() {
        try {
            // Sample budget data - in real app this would come from database
            val spent = 2500.0
            val budget = 5000.0
            val percentage = (spent / budget * 100).toInt()
            
            tvBudgetAmount.text = "R${String.format("%.0f", spent)} / R${String.format("%.0f", budget)}"
            tvBudgetPercentage.text = "$percentage%"
            
            // Update status based on percentage using ContextCompat for API compatibility
            when {
                percentage >= 90 -> {
                    tvBudgetStatusText.text = "Over Budget"
                    tvBudgetStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
                    imgBudgetStatusIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_light))
                }
                percentage >= 75 -> {
                    tvBudgetStatusText.text = "Near Budget"
                    tvBudgetStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
                    imgBudgetStatusIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light))
                }
                else -> {
                    tvBudgetStatusText.text = "Under Budget"
                    tvBudgetStatusText.setTextColor(ContextCompat.getColor(this, R.color.gold))
                    imgBudgetStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.gold))
                }
            }
            
            Log.d("AnalyticsActivity", "Budget status updated: $percentage%")
        } catch (e: Exception) {
            Log.e("AnalyticsActivity", "Error updating budget status: ${e.message}")
        }
    }

    private fun updateMySplitAnalytics(billSplits: List<BillSplit>) {
        try {
            val totalBills = billSplits.size
            val totalAmount = billSplits.sumOf { it.totalAmount }
            val averageAmount = if (totalBills > 0) totalAmount / totalBills else 0.0
            
            tvTotalSplitBills.text = totalBills.toString()
            tvTotalSplitAmount.text = "R${String.format("%.2f", totalAmount)}"
            tvAverageSplit.text = "R${String.format("%.2f", averageAmount)}"
            
            Log.d("AnalyticsActivity", "MySplit analytics updated: $totalBills bills, R$totalAmount total")
        } catch (e: Exception) {
            Log.e("AnalyticsActivity", "Error updating MySplit analytics: ${e.message}")
        }
    }

    private fun updateSouthAfricanContext() {
        try {
            // Sample South African spending data
            val braaiAmount = 450.0
            val taxiAmount = 320.0
            val rentAmount = 1200.0
            
            tvBraaiAmount.text = "R${String.format("%.0f", braaiAmount)}"
            tvTaxiAmount.text = "R${String.format("%.0f", taxiAmount)}"
            tvRentAmount.text = "R${String.format("%.0f", rentAmount)}"
            
            Log.d("AnalyticsActivity", "South African context updated")
        } catch (e: Exception) {
            Log.e("AnalyticsActivity", "Error updating South African context: ${e.message}")
        }
    }

    private fun updatePieChartData() {
        try {
            // Sample expense category data with South African context
            val entries = listOf(
                PieEntry(25f, "Groceries"),
                PieEntry(20f, "Transport"),
                PieEntry(15f, "Entertainment"),
                PieEntry(12f, "Braai & Social"),
                PieEntry(10f, "Utilities"),
                PieEntry(8f, "Clothing"),
                PieEntry(10f, "Other")
            )
            
            val dataSet = PieDataSet(entries, "Expense Categories")
            
            // Custom pixel-retro colors
            val colors = listOf(
                ContextCompat.getColor(this, R.color.gold),        // Gold
                Color.parseColor("#8B4513"),  // Saddle Brown
                Color.parseColor("#CD853F"),  // Peru
                Color.parseColor("#D2691E"),  // Chocolate
                Color.parseColor("#A0522D"),  // Sienna
                Color.parseColor("#F4A460"),  // Sandy Brown
                Color.parseColor("#DEB887")   // Burlywood
            )
            
            dataSet.colors = colors
            dataSet.valueTextSize = 11f
            dataSet.valueTextColor = Color.WHITE
            dataSet.sliceSpace = 3f
            dataSet.selectionShift = 5f
            
            val data = PieData(dataSet)
            data.setValueFormatter(PercentFormatter())
            data.setValueTextSize(11f)
            data.setValueTextColor(Color.WHITE)
            
            pieChart.data = data
            pieChart.invalidate()
            
            // Update legend styling
            val legend = pieChart.legend
            legend.textColor = ContextCompat.getColor(this, R.color.gold)
            legend.textSize = 10f
            legend.isEnabled = true
            
            Log.d("AnalyticsActivity", "Pie chart data updated with expense categories")
        } catch (e: Exception) {
            Log.e("AnalyticsActivity", "Error updating pie chart data: ${e.message}")
        }
    }

    private fun updateCategoryLegendData() {
        try {
            // Sample category legend data matching pie chart
            val totalBudget = 5000.0
            val categoryLegendItems = listOf(
                CategoryLegendItem(ContextCompat.getColor(this, R.color.gold), "Groceries", 25f, totalBudget * 0.25),
                CategoryLegendItem(Color.parseColor("#8B4513"), "Transport", 20f, totalBudget * 0.20),
                CategoryLegendItem(Color.parseColor("#CD853F"), "Entertainment", 15f, totalBudget * 0.15),
                CategoryLegendItem(Color.parseColor("#D2691E"), "Braai & Social", 12f, totalBudget * 0.12),
                CategoryLegendItem(Color.parseColor("#A0522D"), "Utilities", 10f, totalBudget * 0.10),
                CategoryLegendItem(Color.parseColor("#F4A460"), "Clothing", 8f, totalBudget * 0.08),
                CategoryLegendItem(Color.parseColor("#DEB887"), "Other", 10f, totalBudget * 0.10)
            )
            
            val adapter = CategoryLegendAdapter(categoryLegendItems)
            rvCategoryLegend.adapter = adapter
            
            Log.d("AnalyticsActivity", "Category legend data updated")
        } catch (e: Exception) {
            Log.e("AnalyticsActivity", "Error updating category legend data: ${e.message}")
        }
    }

    private fun showDownloadAnimation() {
        try {
            flDownloadAnimation.visibility = View.VISIBLE
            
            // Hide animation after 2 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                flDownloadAnimation.visibility = View.GONE
            }, 2000)
            
            Log.d("AnalyticsActivity", "Download animation shown")
        } catch (e: Exception) {
            Log.e("AnalyticsActivity", "Error showing download animation: ${e.message}")
        }
    }

    private fun navigateToMainMenu() {
        try {
            val intent = Intent(this, MainMenu::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("AnalyticsActivity", "Error navigating to MainMenu: ${e.message}")
        }
    }

    private fun navigateToBanking() {
        try {
            val intent = Intent(this, BankingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("AnalyticsActivity", "Error navigating to BankingActivity: ${e.message}")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToMainMenu()
    }
}
