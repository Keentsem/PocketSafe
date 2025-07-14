package com.example.pocketsafe.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Expense
import com.example.pocketsafe.data.Category
import java.text.NumberFormat
import java.util.*

class ExpenseAdapter(
    private var expenses: List<Expense> = emptyList(),
    private var categoryMap: Map<Int, Category> = emptyMap(),
    private val onItemClick: ((Expense) -> Unit)? = null
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {
    
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    /**
     * Update expenses list along with the latest category mapping so that we can display
     * proper category names (and eventually icons/colors) in the list.
     */
    fun updateExpenses(
        newExpenses: List<Expense>,
        newCategoryMap: Map<Int, Category>
    ) {
        expenses = newExpenses
        categoryMap = newCategoryMap
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount() = expenses.size

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        private val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)

        fun bind(expense: Expense) {
            // Display user-friendly category name instead of raw ID
            val categoryName = categoryMap[expense.categoryId]?.name ?: "Unknown"
            categoryTextView.text = categoryName
            amountTextView.text = NumberFormat.getCurrencyInstance(Locale.US).format(expense.amount)
            // Convert the Long timestamp to a readable date string
            val dateFormat = java.text.SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val dateString = dateFormat.format(Date(expense.date))
            dateTextView.text = dateString
            descriptionTextView.text = expense.description ?: "No description"
            
            // Set click listener to handle item clicks
            itemView.setOnClickListener {
                onItemClick?.invoke(expense)
            }
        }
    }
}