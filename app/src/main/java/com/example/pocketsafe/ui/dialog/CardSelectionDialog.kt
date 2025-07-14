package com.example.pocketsafe.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R
import com.example.pocketsafe.model.BankCard

/**
 * Dialog for selecting a payment card from saved cards
 * Follows the pixel-retro theme with gold (#f3c34e) and brown (#5b3f2c) colors
 */
class CardSelectionDialog(
    context: Context,
    private val cards: List<BankCard>,
    private val onCardSelected: (BankCard) -> Unit,
    private val onAddNewCard: () -> Unit
) : Dialog(context) {

    private lateinit var recyclerCards: RecyclerView
    private lateinit var emptyStateView: LinearLayout
    private lateinit var btnCancel: Button
    private lateinit var btnAddCard: Button
    private lateinit var tvDialogTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_card_selection)

        initializeViews()
        setupRecyclerView()
        setupButtons()
        applyPixelFont()
        updateVisibility()
    }

    private fun initializeViews() {
        try {
            recyclerCards = findViewById(R.id.recyclerCards)
            emptyStateView = findViewById(R.id.emptyStateView)
            btnCancel = findViewById(R.id.btnCancel)
            btnAddCard = findViewById(R.id.btnAddCard)
            tvDialogTitle = findViewById(R.id.tvDialogTitle)
        } catch (e: Exception) {
            Log.e("CardSelectionDialog", "Error initializing views: ${e.message}")
        }
    }

    private fun setupRecyclerView() {
        try {
            recyclerCards.layoutManager = LinearLayoutManager(context)
            val adapter = CardSelectionAdapter(cards.filter { !it.isLocked }) { card ->
                onCardSelected(card)
                dismiss()
            }
            recyclerCards.adapter = adapter
        } catch (e: Exception) {
            Log.e("CardSelectionDialog", "Error setting up RecyclerView: ${e.message}")
        }
    }

    private fun setupButtons() {
        try {
            btnCancel.setOnClickListener {
                dismiss()
            }

            btnAddCard.setOnClickListener {
                onAddNewCard()
                dismiss()
            }
        } catch (e: Exception) {
            Log.e("CardSelectionDialog", "Error setting up buttons: ${e.message}")
        }
    }

    private fun applyPixelFont() {
        try {
            val pixelFont = ResourcesCompat.getFont(context, R.font.pixel_game)
            tvDialogTitle.typeface = pixelFont
            btnCancel.typeface = pixelFont
            btnAddCard.typeface = pixelFont
        } catch (e: Exception) {
            Log.e("CardSelectionDialog", "Error applying pixel font: ${e.message}")
        }
    }

    private fun updateVisibility() {
        val availableCards = cards.filter { !it.isLocked }
        if (availableCards.isEmpty()) {
            recyclerCards.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
        } else {
            recyclerCards.visibility = View.VISIBLE
            emptyStateView.visibility = View.GONE
        }
    }
}

/**
 * Adapter for card selection in the dialog
 */
class CardSelectionAdapter(
    private val cards: List<BankCard>,
    private val onCardClick: (BankCard) -> Unit
) : RecyclerView.Adapter<CardSelectionAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_selection, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cards[position])
    }

    override fun getItemCount(): Int = cards.size

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgCardType: ImageView = itemView.findViewById(R.id.imgCardType)
        private val tvCardName: TextView = itemView.findViewById(R.id.tvCardName)
        private val tvCardDetails: TextView = itemView.findViewById(R.id.tvCardDetails)
        private val imgLockOverlay: ImageView = itemView.findViewById(R.id.imgLockOverlay)

        fun bind(card: BankCard) {
            try {
                tvCardName.text = card.name
                tvCardDetails.text = "**** ${card.lastFourDigits}"

                // Set card type icon
                val cardIcon = when (card.type.lowercase()) {
                    "visa" -> R.drawable.visa
                    "mastercard" -> R.drawable.mastercard
                    else -> R.drawable.banking
                }
                imgCardType.setImageResource(cardIcon)

                // Show/hide lock overlay
                imgLockOverlay.visibility = if (card.isLocked) View.VISIBLE else View.GONE

                // Apply pixel font
                val pixelFont = ResourcesCompat.getFont(itemView.context, R.font.pixel_game)
                tvCardName.typeface = pixelFont
                tvCardDetails.typeface = pixelFont

                // Set click listener only for unlocked cards
                itemView.setOnClickListener {
                    if (!card.isLocked) {
                        onCardClick(card)
                    }
                }

                // Dim locked cards
                itemView.alpha = if (card.isLocked) 0.5f else 1.0f

            } catch (e: Exception) {
                Log.e("CardSelectionAdapter", "Error binding card: ${e.message}")
            }
        }
    }
}
