package com.example.pocketsafe.ui

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R
import com.example.pocketsafe.data.AppDatabase
import com.example.pocketsafe.data.Category
import com.example.pocketsafe.data.IconType
import com.example.pocketsafe.data.Expense
import com.example.pocketsafe.ui.activity.BaseActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MyPocketActivity : BaseActivity() {
    // Database
    private lateinit var db: AppDatabase

    // UI Components
    private lateinit var pieChart: PieChart
    private lateinit var categorySpinner: Spinner
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var applyFilterButton: Button
    private lateinit var totalAmountTextView: TextView
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var categoryLegendRecyclerView: RecyclerView
    private lateinit var budgetProgressBar: ProgressBar
    private lateinit var budgetPercentageTextView: TextView
    private lateinit var spentAmountTextView: TextView
    private lateinit var budgetAmountTextView: TextView
    private lateinit var remainingBudgetTextView: TextView

    // Adapters
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var categoryLegendAdapter: CategoryLegendAdapter

    // Data
    private var startDate: Date? = null
    private var endDate: Date? = null
    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    private val monthlyBudget = 1000.0

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_my_pocket)

            // Apply pixel-retro theme
            window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.background_brown))

            setupDatabase()
            initializeViews()
            safeLoadInitialData()

        } catch (e: Exception) {
            Log.e("MyPocketActivity", "Failed to initialize", e)
            Toast.makeText(this, "Failed to initialize My Pocket view", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupDatabase() {
        try {
            db = AppDatabase.getDatabase(applicationContext)
            Log.d("MyPocketActivity", "Database initialized successfully")
        } catch (e: Exception) {
            Log.e("MyPocketActivity", "Database initialization error: ${e.message}", e)
            throw Exception("Failed to initialize database")
        }
    }

    private fun initializeViews() {
        try {
            // Initialize all views
            pieChart = findViewById(R.id.pieChart)
            categorySpinner = findViewById(R.id.categorySpinner)
            startDateButton = findViewById(R.id.startDateButton)
            endDateButton = findViewById(R.id.endDateButton)
            applyFilterButton = findViewById(R.id.applyFilterButton)
            expensesRecyclerView = findViewById(R.id.expensesRecyclerView)
            categoryLegendRecyclerView = findViewById(R.id.categoryLegendRecyclerView)
            budgetProgressBar = findViewById(R.id.budgetProgressBar)
            budgetPercentageTextView = findViewById(R.id.budgetPercentageTextView)
            spentAmountTextView = findViewById(R.id.spentAmountTextView)
            budgetAmountTextView = findViewById(R.id.budgetAmountTextView)
            remainingBudgetTextView = findViewById(R.id.remainingBudgetTextView)
            totalAmountTextView = findViewById(R.id.totalAmountTextView)

            // Setup components
            setupPieChart()
            setupCategorySpinner()
            setupDateButtons()
            setupFilterButton()
            setupRecyclerViews()
            setupBudgetView()

        } catch (e: Exception) {
            Log.e("MyPocketActivity", "Error initializing views: ${e.message}", e)
            throw e
        }
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            setDrawEntryLabels(false)
            setUsePercentValues(true)
            setCenterTextSize(16f)
            setCenterTextColor(ContextCompat.getColor(this@MyPocketActivity, R.color.primary))
            legend.isEnabled = false
        }
    }

    private fun setupCategorySpinner() {
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadExpenses()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDateButtons() {
        startDateButton.setOnClickListener { showDatePickerDialog(true) }
        endDateButton.setOnClickListener { showDatePickerDialog(false) }
    }

    private fun setupFilterButton() {
        applyFilterButton.setOnClickListener {
            if (startDate == null || endDate == null) {
                Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadExpenses()
        }
    }

    private fun setupRecyclerViews() {
        // Initialize expense adapter with empty data and category map
        expenseAdapter = ExpenseAdapter(emptyList(), emptyMap()) { expense ->
            showExpenseDetail(expense)
        }
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)
        expensesRecyclerView.adapter = expenseAdapter
        expensesRecyclerView.isNestedScrollingEnabled = false

        // Initialize category legend adapter
        categoryLegendAdapter = CategoryLegendAdapter(emptyList())
        categoryLegendRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoryLegendRecyclerView.adapter = categoryLegendAdapter
    }

    private fun setupBudgetView() {
        budgetAmountTextView.text = getString(R.string.budget_format, formatCurrency(monthlyBudget))
        updateBudgetProgress(0.0)
    }

    private fun safeLoadInitialData() {
        lifecycleScope.launch {
            try {
                loadCategories()
                loadExpenses()
            } catch (e: Exception) {
                Log.e("MyPocketActivity", "Failed to load data: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showError("Failed to load data: ${e.message ?: "Unknown error"}")
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e("MyPocketActivity", message)
    }

    private fun formatCurrency(amount: Double): String {
        return String.format("$%.2f", amount)
    }

    private fun updateBudgetProgress(spentAmount: Double, budget: Double = monthlyBudget) {
        val percentage = if (budget > 0) (spentAmount / budget * 100).toInt().coerceIn(0, 100) else 0
        val remaining = budget - spentAmount

        budgetProgressBar.progress = percentage
        budgetPercentageTextView.text = "$percentage%"
        spentAmountTextView.text = getString(R.string.spent_amount_format, formatCurrency(spentAmount))
        budgetAmountTextView.text = getString(R.string.budget_format, formatCurrency(budget))
        remainingBudgetTextView.text = getString(R.string.remaining_budget_format, formatCurrency(remaining))

        val progressDrawable = budgetProgressBar.progressDrawable.mutate()
        val color = when {
            percentage > 90 -> Color.parseColor("#F44336")
            percentage > 75 -> Color.parseColor("#FF9800")
            else -> Color.parseColor("#f3c34e") // Gold color
        }

        try {
            progressDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        } catch (e: Exception) {
            Log.e("MyPocketActivity", "Error setting progress color", e)
        }
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }.time

                if (isStartDate) {
                    startDate = date
                    startDateButton.text = "Start: ${dateFormatter.format(date)}"
                } else {
                    endDate = date
                    endDateButton.text = "End: ${dateFormatter.format(date)}"
                }
            },
            year, month, day
        ).show()
    }

    private fun loadCategories() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val categoriesFlow = db.categoryDao().getAllCategories()
                val categories = categoriesFlow.firstOrNull() ?: emptyList()

                val categoryNames = mutableListOf("All Categories")
                categoryNames.addAll(categories.map { it.name })

                withContext(Dispatchers.Main) {
                    val adapter = ArrayAdapter(
                        this@MyPocketActivity,
                        android.R.layout.simple_spinner_item,
                        categoryNames
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categorySpinner.adapter = adapter

                    if (categories.isEmpty()) {
                        createDefaultCategories()
                    }

                    loadExpenses()
                }
            } catch (e: Exception) {
                Log.e("MyPocketActivity", "Error loading categories: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showError("Failed to load categories")
                }
            }
        }
    }

    private fun createDefaultCategories() {
        val defaultCategories = listOf(
            Category(name = "Food & Dining", iconType = IconType.FOOD, icon = "restaurant", color = Color.parseColor("#4CAF50"), monthlyAmount = 300.0),
            Category(name = "Transportation", iconType = IconType.TRANSPORT, icon = "car", color = Color.parseColor("#FFC107"), monthlyAmount = 150.0),
            Category(name = "Shopping", iconType = IconType.SHOPPING, icon = "cart", color = Color.parseColor("#2196F3"), monthlyAmount = 200.0),
            Category(name = "Bills & Utilities", iconType = IconType.NECESSITY, icon = "bill", color = Color.parseColor("#F44336"), monthlyAmount = 250.0),
            Category(name = "Entertainment", iconType = IconType.ENTERTAINMENT, icon = "movie", color = Color.parseColor("#9C27B0"), monthlyAmount = 100.0)
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                defaultCategories.forEach { category ->
                    db.categoryDao().insertCategory(category)
                }
                loadCategories()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Failed to create default categories")
                }
            }
        }
    }

    private fun loadExpenses() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Load categories first
                val allCategoriesFlow = db.categoryDao().getAllCategories()
                val allCategories = allCategoriesFlow.firstOrNull() ?: emptyList()
                val categoryMap = allCategories.associateBy({ it.id }, { it })

                // Get selected category
                val selectedCategoryName = withContext(Dispatchers.Main) {
                    categorySpinner.selectedItem?.toString() ?: "All Categories"
                }

                // Get expenses
                val allExpenses: List<Expense> = if (selectedCategoryName == "All Categories") {
                    db.expenseDao().getAllExpenses()
                } else {
                    val category = allCategories.find { it.name == selectedCategoryName }
                    if (category != null) {
                        db.expenseDao().getExpensesByCategory(category.id)
                    } else {
                        emptyList()
                    }
                }

                // Filter by date
                val filteredExpenses = allExpenses.filter { expense ->
                    val expenseDate = Date(expense.date)
                    val afterStartDate = startDate == null || expenseDate >= startDate
                    val beforeEndDate = endDate == null || expenseDate <= endDate
                    afterStartDate && beforeEndDate
                }

                // Sort expenses by most recent first so the list is ordered
                val sortedExpenses = filteredExpenses.sortedByDescending { it.date }

                // --- DEBUG LOGGING ---
                Log.d("MyPocketActivity", "Loaded expenses count: ${sortedExpenses.size}")
                if (sortedExpenses.isNotEmpty()) {
                    // Log the first 3 expenses for quick inspection
                    sortedExpenses.take(3).forEachIndexed { idx, exp ->
                        Log.d(
                            "MyPocketActivity",
                            "Expense[$idx] id=${exp.id}, catId=${exp.categoryId}, amt=${exp.amount}, date=${Date(exp.date)}"
                        )
                    }
                }

                // If no expenses at all, seed sample data once for demo
                if (sortedExpenses.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MyPocketActivity, "No expenses found. Seeding demo data...", Toast.LENGTH_SHORT).show()
                    }
                    createSampleExpenses(categoryMap.values.toList())
                    return@launch // loadExpenses will be called again after seeding
                }

                // Process data for UI
                val categoryTotals = mutableMapOf<Int, Double>()
                sortedExpenses.forEach { expense ->
                    if (expense.categoryId > 0) {
                        categoryTotals[expense.categoryId] =
                            (categoryTotals[expense.categoryId] ?: 0.0) + expense.amount
                    }
                }

                // Sort categories by total spent (descending) so pie + legend are ordered
                val categoryTotalsSorted = categoryTotals.toList().sortedByDescending { it.second }

                // Create chart data
                val pieEntries = mutableListOf<PieEntry>()
                val legendItems = mutableListOf<CategoryLegendItem>()
                var totalBudget = 0.0

                categoryTotalsSorted.forEach { (categoryId, amount) ->
                    val category = categoryMap[categoryId]
                    if (category != null) {
                        pieEntries.add(PieEntry(amount.toFloat(), category.name))
                        legendItems.add(
                            CategoryLegendItem(
                                name = category.name,
                                amount = amount,
                                color = category.color ?: Color.parseColor("#f3c34e"),
                                categoryId = categoryId
                            )
                        )
                        totalBudget += category.monthlyAmount
                    }
                }

                val totalSpent = sortedExpenses.sumOf { it.amount }
                if (totalBudget <= 0) totalBudget = monthlyBudget

                // Update UI
                withContext(Dispatchers.Main) {
                    updateBudgetProgress(totalSpent, totalBudget)

                    if (pieEntries.isNotEmpty()) {
                        updatePieChart(pieEntries)
                        categoryLegendAdapter.updateCategories(legendItems)
                    } else {
                        pieChart.clear()
                        pieChart.setNoDataText("No expenses to display")
                        pieChart.invalidate()
                        categoryLegendAdapter.updateCategories(emptyList())
                    }

                    totalAmountTextView.text = "Total: ${formatCurrency(totalSpent)}"
                    // Update list with sorted (most recent first) expenses and provide category mapping
                    expenseAdapter.updateExpenses(sortedExpenses, categoryMap)

                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("MyPocketActivity", "Failed to load expenses", e)
                    showError("Failed to load expenses")
                }
            }
        }
    }

    private fun updatePieChart(pieEntries: List<PieEntry>) {
        val dataSet = PieDataSet(pieEntries, "Categories")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueFormatter = PercentFormatter(pieChart)

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.invalidate()
    }

    private fun showExpenseDetail(expense: Expense) {
        val message = "Description: ${expense.description}\n" +
                "Amount: ${formatCurrency(expense.amount)}\n" +
                "Date: ${dateFormatter.format(Date(expense.date))}"

        AlertDialog.Builder(this)
            .setTitle("Expense Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun createSampleExpenses(categories: List<Category>) {
        val sampleExpenses = listOf(
            Expense(description = "Sample expense 1", amount = 10.99, categoryId = categories[0].id, date = System.currentTimeMillis() - 86400000),
            Expense(description = "Sample expense 2", amount = 5.99, categoryId = categories[1].id, date = System.currentTimeMillis() - 43200000),
            Expense(description = "Sample expense 3", amount = 7.99, categoryId = categories[2].id, date = System.currentTimeMillis() - 21600000),
            Expense(description = "Sample expense 4", amount = 12.99, categoryId = categories[3].id, date = System.currentTimeMillis() - 10800000),
            Expense(description = "Sample expense 5", amount = 15.99, categoryId = categories[4].id, date = System.currentTimeMillis())
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                sampleExpenses.forEach { expense ->
                    db.expenseDao().insertExpense(expense)
                }
                loadExpenses()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Failed to create sample expenses")
                }
            }
        }
    }
}