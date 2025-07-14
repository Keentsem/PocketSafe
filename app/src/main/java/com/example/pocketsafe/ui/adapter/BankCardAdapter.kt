package com.example.pocketsafe.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R
import com.example.pocketsafe.model.BankCard
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter for displaying bank cards in a RecyclerView
 * Follows the pixel-retro theme with gold (#f3c34e) and brown (#5b3f2c) colors
 */
class BankCardAdapter(
    private val context: Context,
    private var cards: List<BankCard> = emptyList(),
    private val onCardClick: (BankCard) -> Unit,
    private val onCardLockToggle: (BankCard, Boolean) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<BankCardAdapter.CardViewHolder>() {

    private val pixelFont: Typeface? by lazy {
        ResourcesCompat.getFont(context, R.font.pixel_game)
    }
    
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_bank_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        
        // Apply pixel font to all text views
        pixelFont?.let { font ->
            holder.tvCardName.typeface = font
            holder.tvCardNumber.typeface = font
            holder.tvCardLimit.typeface = font
            holder.tvCardSpending.typeface = font
        }
        
        // Set card data
        holder.tvCardName.text = card.name
        holder.tvCardNumber.text = "•••• ${card.lastFourDigits}"
        
        // Set card logo based on type
        val logoResId = when (card.type.lowercase()) {
            "visa" -> R.drawable.visa
            "mastercard" -> R.drawable.mastercard
            else -> R.drawable.banking // fallback to generic banking icon
        }
        holder.imgCardLogo.setImageResource(logoResId)
        
        // Set spending limit and current spending
        holder.tvCardLimit.text = "Limit: ${currencyFormat.format(card.limit)}"
        holder.tvCardSpending.text = "Spent: ${currencyFormat.format(card.spent)}"
        
        // Calculate and set progress
        val progress = if (card.limit > 0) {
            ((card.spent / card.limit) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
        holder.progressSpending.progress = progress
        
        // Show/hide lock overlay based on card lock status
        holder.imgLockOverlay.visibility = if (card.isLocked) View.VISIBLE else View.GONE
        
        // Set up three-dot menu click listener
        holder.btnCardMenu.setOnClickListener { view ->
            showCardOptionsMenu(view, card)
        }
        
        // Set click listener
        holder.itemView.setOnClickListener { onCardClick(card) }
    }

    override fun getItemCount(): Int = cards.size

    fun updateCards(newCards: List<BankCard>) {
        this.cards = newCards
        notifyDataSetChanged()
    }

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCardLogo: ImageView = itemView.findViewById(R.id.imgCardLogo)
        val tvCardName: TextView = itemView.findViewById(R.id.tvCardName)
        val tvCardNumber: TextView = itemView.findViewById(R.id.tvCardNumber)
        val tvCardLimit: TextView = itemView.findViewById(R.id.tvCardLimit)
        val tvCardSpending: TextView = itemView.findViewById(R.id.tvCardSpending)
        val progressSpending: ProgressBar = itemView.findViewById(R.id.progressSpending)
        val imgLockOverlay: ImageView = itemView.findViewById(R.id.imgLockOverlay)
        val btnCardMenu: ImageView = itemView.findViewById(R.id.btnCardMenu)
    }
    
    private fun showCardOptionsMenu(view: View, card: BankCard) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.card_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_lock -> {
                    onCardLockToggle(card, !card.isLocked)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}
