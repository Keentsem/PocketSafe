package com.example.pocketsafe.ui.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R
import com.example.pocketsafe.data.BankCard
import com.example.pocketsafe.data.CardType
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter for displaying bank cards in a RecyclerView
 * Follows the pixel-retro theme with gold (#f3c34e) and brown (#5b3f2c) colors
 */
class BankCardAdapter(private val onCardClick: (BankCard) -> Unit) : 
    ListAdapter<BankCard, BankCardAdapter.BankCardViewHolder>(BankCardDiffCallback()) {
    
    private val TAG = "BankCardAdapter"
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bank_card, parent, false)
        return BankCardViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: BankCardViewHolder, position: Int) {
        val card = getItem(position)
        try {
            holder.bind(card)
        } catch (e: Exception) {
            Log.e(TAG, "Error binding card view: ${e.message}", e)
        }
    }
    
    inner class BankCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardContainer: View = itemView.findViewById(R.id.cardContainer)
        private val cardLogo: ImageView = itemView.findViewById(R.id.imgCardLogo)
        private val cardName: TextView = itemView.findViewById(R.id.tvCardName)
        private val cardNumber: TextView = itemView.findViewById(R.id.tvCardNumber)
        private val cardLimit: TextView = itemView.findViewById(R.id.tvCardLimit)
        private val cardSpending: TextView = itemView.findViewById(R.id.tvCardSpending)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressSpending)
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCardClick(getItem(position))
                }
            }
        }
        
        fun bind(card: BankCard) {
            try {
                // Set card name and number
                cardName.text = card.cardName
                cardNumber.text = "•••• ${card.cardNumber}"
                
                // Set card logo based on type
                val logoResId = when (card.cardType) {
                    CardType.VISA -> R.drawable.visa
                    CardType.MASTERCARD -> R.drawable.mastercard
                    CardType.OTHER -> R.drawable.banking
                }
                cardLogo.setImageResource(logoResId)
                
                // Set card limit and spending
                val limitText = if (card.monthlyLimit > 0) {
                    "Limit: ${currencyFormatter.format(card.monthlyLimit)}"
                } else {
                    "No Limit Set"
                }
                cardLimit.text = limitText
                
                cardSpending.text = "Spent: ${currencyFormatter.format(card.currentSpending)}"
                
                // Set progress bar
                if (card.monthlyLimit > 0) {
                    val progress = ((card.currentSpending / card.monthlyLimit) * 100).toInt()
                    progressBar.progress = progress.coerceIn(0, 100)
                    progressBar.visibility = View.VISIBLE
                    
                    // Set progress bar color based on spending percentage
                    val progressColor = when {
                        progress >= 90 -> Color.parseColor("#FF5252") // Red for near limit
                        progress >= 75 -> Color.parseColor("#FFA726") // Orange for warning
                        else -> Color.parseColor("#4CAF50") // Green for good
                    }
                    progressBar.progressDrawable.setTint(progressColor)
                } else {
                    progressBar.visibility = View.INVISIBLE
                }
                
                // Set card background color
                try {
                    val cardColor = Color.parseColor(card.color)
                    val shape = GradientDrawable()
                    shape.shape = GradientDrawable.RECTANGLE
                    shape.cornerRadius = 16f
                    shape.setColor(cardColor)
                    cardContainer.background = shape
                } catch (e: Exception) {
                    // Fallback to default color if parsing fails
                    cardContainer.setBackgroundColor(Color.parseColor("#f3c34e"))
                    Log.e(TAG, "Error setting card color: ${e.message}")
                }
                
                // Set active/inactive status indicator
                if (card.isActive) {
                    statusIndicator.setBackgroundColor(Color.parseColor("#4CAF50")) // Green for active
                } else {
                    statusIndicator.setBackgroundColor(Color.parseColor("#FF5252")) // Red for inactive
                }
                
                // Set text color based on card background brightness
                try {
                    val color = Color.parseColor(card.color)
                    val brightness = (Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114) / 1000
                    val textColor = if (brightness > 125) Color.BLACK else Color.WHITE
                    
                    cardName.setTextColor(textColor)
                    cardNumber.setTextColor(textColor)
                    cardLimit.setTextColor(textColor)
                    cardSpending.setTextColor(textColor)
                } catch (e: Exception) {
                    // Fallback to default text color
                    Log.e(TAG, "Error setting text color: ${e.message}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in bind method: ${e.message}", e)
            }
        }
    }
}

/**
 * DiffUtil callback for efficient RecyclerView updates
 */
class BankCardDiffCallback : DiffUtil.ItemCallback<BankCard>() {
    override fun areItemsTheSame(oldItem: BankCard, newItem: BankCard): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: BankCard, newItem: BankCard): Boolean {
        return oldItem == newItem
    }
}
