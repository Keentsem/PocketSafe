package com.example.pocketsafe.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R

data class CategoryLegendItem(
    val color: Int,
    val name: String,
    val percentage: Float,
    val amount: Double
)

class CategoryLegendAdapter(
    private val categories: List<CategoryLegendItem>
) : RecyclerView.Adapter<CategoryLegendAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorIndicator: View = itemView.findViewById(R.id.vColorIndicator)
        val categoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val categoryPercentage: TextView = itemView.findViewById(R.id.tvCategoryPercentage)
        val categoryAmount: TextView = itemView.findViewById(R.id.tvCategoryAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_legend, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        
        holder.colorIndicator.setBackgroundColor(category.color)
        holder.categoryName.text = category.name
        holder.categoryPercentage.text = "${category.percentage.toInt()}%"
        holder.categoryAmount.text = "R${String.format("%.0f", category.amount)}"
        
        // Apply pixel font
        val pixelFont = androidx.core.content.res.ResourcesCompat.getFont(
            holder.itemView.context, 
            R.font.pixel_game
        )
        holder.categoryName.typeface = pixelFont
        holder.categoryPercentage.typeface = pixelFont
        holder.categoryAmount.typeface = pixelFont
    }

    override fun getItemCount(): Int = categories.size
}
