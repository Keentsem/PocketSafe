package com.example.pocketsafe.ui.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R
import com.example.pocketsafe.model.BankCard
import com.example.pocketsafe.ui.adapter.BankCardAdapter
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat

/**
 * Banking Activity for managing bank cards and tracking spending
 * Follows the pixel-retro theme with gold (#f3c34e) and brown (#5b3f2c) colors
 */
class BankingActivity : BaseActivity() {
    
    private lateinit var recyclerCards: RecyclerView
    private lateinit var emptyStateView: LinearLayout
    private lateinit var btnAddCard: Button
    private lateinit var loadingIndicator: View
    private lateinit var imgComingSoon: ImageView
    private lateinit var tvComingSoon: TextView
    private lateinit var imgProfilePicture: ImageView
    private lateinit var btnEditProfile: ImageView
    
    private lateinit var cardAdapter: BankCardAdapter
    private val sampleCards = mutableListOf<BankCard>()
    
    // Permission launcher for storage access
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("BankingActivity", "Storage permission granted, opening image picker")
            launchImagePicker()
        } else {
            Log.w("BankingActivity", "Storage permission denied")
            showPixelToast("Storage permission is required to change profile picture")
        }
    }
    
    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            Log.d("BankingActivity", "Image picker result: ${result.resultCode}")
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    Log.d("BankingActivity", "Selected image URI: $uri")
                    updateProfilePicture(uri)
                } ?: run {
                    Log.w("BankingActivity", "No image URI returned from picker")
                    showPixelToast("No image selected")
                }
            } else {
                Log.d("BankingActivity", "Image picker cancelled or failed")
            }
        } catch (e: Exception) {
            Log.e("BankingActivity", "Error handling image picker result: ${e.message}", e)
            showPixelToast("Error processing selected image")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_banking_placeholder)
            
            // Apply pixel theme
            try {
                window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.background_brown))
            } catch (e: Exception) {
                Log.e("BankingActivity", "Error setting background color: ${e.message}")
                // Fallback to hardcoded color if resource not found
                window.decorView.setBackgroundColor(android.graphics.Color.parseColor("#5b3f2c"))
            }
            
            // Initialize views with null safety
            try {
                recyclerCards = findViewById(R.id.recyclerCards)
                emptyStateView = findViewById(R.id.emptyStateView)
                btnAddCard = findViewById(R.id.btnAddCard)
                loadingIndicator = findViewById(R.id.loadingIndicator)
                imgComingSoon = findViewById(R.id.imgComingSoon)
                tvComingSoon = findViewById(R.id.tvComingSoon)
                imgProfilePicture = findViewById(R.id.imgProfilePicture)
                btnEditProfile = findViewById(R.id.btnEditProfile)
                
                // Set up RecyclerView with adapter
                recyclerCards.layoutManager = LinearLayoutManager(this)
                
                // Initialize adapter with sample cards
                setupSampleCards()
                cardAdapter = BankCardAdapter(this, sampleCards, 
                    onCardClick = { card ->
                        // Handle card click - show edit dialog
                        showEditCardDialog(card)
                    },
                    onCardLockToggle = { card, shouldLock ->
                        // Handle card lock/unlock
                        toggleCardLock(card, shouldLock)
                    }
                )
                recyclerCards.adapter = cardAdapter
                
                // Show cards or empty state
                updateCardListVisibility()
                
                // Set up Add Card button click listener
                btnAddCard.setOnClickListener {
                    showAddCardDialog()
                }
                
                // Set up profile edit button click listener
                btnEditProfile.setOnClickListener {
                    Log.d("BankingActivity", "Profile edit button clicked")
                    try {
                        openImagePicker()
                    } catch (e: Exception) {
                        Log.e("BankingActivity", "Error opening image picker: ${e.message}", e)
                        showPixelToast("Error opening image picker")
                    }
                }
                
                // Set up navigation bar
                setupNavigationBarClickListeners()
                
                // Apply pixel fonts to all text views
                applyPixelFontToTextViews()
                
                Log.d("BankingActivity", "Banking activity initialized successfully")
                
            } catch (e: Exception) {
                Log.e("BankingActivity", "Error initializing views: ${e.message}", e)
                showPixelToast("Error initializing banking interface")
            }
            
        } catch (e: Exception) {
            Log.e("BankingActivity", "Error in onCreate: ${e.message}", e)
            showPixelToast("Error starting banking activity")
        }
    }
    
    private fun setupSampleCards() {
        try {
            // Add sample cards with the pixel-retro theme colors
            sampleCards.add(BankCard(
                id = "1",
                name = "Main Debit Card",
                type = "visa",
                lastFourDigits = "1234",
                limit = 1000.0,
                spent = 750.0,
                color = ContextCompat.getColor(this, R.color.primary),
                isLocked = false
            ))
            
            sampleCards.add(BankCard(
                id = "2",
                name = "Travel Card",
                type = "mastercard",
                lastFourDigits = "5678",
                limit = 2000.0,
                spent = 500.0,
                color = ContextCompat.getColor(this, R.color.secondary),
                isLocked = false
            ))
        } catch (e: Exception) {
            Log.e("BankingActivity", "Error setting up sample cards: ${e.message}")
        }
    }
    
    private fun updateCardListVisibility() {
        try {
            // Hide coming soon elements
            imgComingSoon.visibility = View.GONE
            tvComingSoon.visibility = View.GONE
            
            if (sampleCards.isEmpty()) {
                recyclerCards.visibility = View.GONE
                emptyStateView.visibility = View.VISIBLE
            } else {
                recyclerCards.visibility = View.VISIBLE
                emptyStateView.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e("BankingActivity", "Error updating card list visibility: ${e.message}")
        }
    }
    
    private fun showAddCardDialog() {
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_card, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create()
            
            // Apply pixel font to dialog text views
            applyPixelFontToDialog(dialogView)
            
            // Set up save button
            dialogView.findViewById<Button>(R.id.btnSave)?.setOnClickListener {
                // Get card details from dialog
                val cardName = dialogView.findViewById<TextView>(R.id.etCardName)?.text.toString()
                val cardNumber = dialogView.findViewById<TextView>(R.id.etCardNumber)?.text.toString()
                val cardLimit = dialogView.findViewById<TextView>(R.id.etMonthlyLimit)?.text.toString().toDoubleOrNull() ?: 0.0
                
                // Get selected card type
                val rbVisa = dialogView.findViewById<android.widget.RadioButton>(R.id.rbVisa)
                val rbMastercard = dialogView.findViewById<android.widget.RadioButton>(R.id.rbMastercard)
                val cardType = when {
                    rbVisa?.isChecked == true -> "visa"
                    rbMastercard?.isChecked == true -> "mastercard"
                    else -> "other"
                }
                
                // Create new card and add to list
                val lastFour = if (cardNumber.length >= 4) cardNumber.takeLast(4) else "0000"
                val newCard = BankCard(
                    id = System.currentTimeMillis().toString(),
                    name = cardName.ifEmpty { "New Card" },
                    type = cardType,
                    lastFourDigits = lastFour,
                    limit = cardLimit,
                    spent = 0.0,
                    color = ContextCompat.getColor(this, R.color.primary)
                )
                
                sampleCards.add(newCard)
                cardAdapter.updateCards(sampleCards)
                updateCardListVisibility()
                
                showPixelToast("Card added successfully!")
                dialog.dismiss()
            }
            
            // Set up cancel button
            dialogView.findViewById<Button>(R.id.btnCancel)?.setOnClickListener {
                dialog.dismiss()
            }
            
            dialog.show()
        } catch (e: Exception) {
            Log.e("BankingActivity", "Error showing add card dialog: ${e.message}")
            showPixelToast("Error showing add card dialog")
        }
    }
    
    private fun showEditCardDialog(card: BankCard) {
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_card, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create()
            
            // Apply pixel font to dialog text views
            applyPixelFontToDialog(dialogView)
            
            // Pre-fill card details
            dialogView.findViewById<TextView>(R.id.etCardName)?.text = card.name
            dialogView.findViewById<TextView>(R.id.etCardNumber)?.text = "•••• ${card.lastFourDigits}"
            dialogView.findViewById<TextView>(R.id.etMonthlyLimit)?.text = card.limit.toString()
            
            // Set card type radio button
            when (card.type.lowercase()) {
                "visa" -> dialogView.findViewById<android.widget.RadioButton>(R.id.rbVisa)?.isChecked = true
                "mastercard" -> dialogView.findViewById<android.widget.RadioButton>(R.id.rbMastercard)?.isChecked = true
                else -> dialogView.findViewById<android.widget.RadioButton>(R.id.rbOther)?.isChecked = true
            }
            
            // Set up save button
            dialogView.findViewById<Button>(R.id.btnSave)?.setOnClickListener {
                // Get updated card details
                val cardName = dialogView.findViewById<TextView>(R.id.etCardName)?.text.toString()
                val cardLimit = dialogView.findViewById<TextView>(R.id.etMonthlyLimit)?.text.toString().toDoubleOrNull() ?: 0.0
                
                // Get selected card type
                val rbVisa = dialogView.findViewById<android.widget.RadioButton>(R.id.rbVisa)
                val rbMastercard = dialogView.findViewById<android.widget.RadioButton>(R.id.rbMastercard)
                val cardType = when {
                    rbVisa?.isChecked == true -> "visa"
                    rbMastercard?.isChecked == true -> "mastercard"
                    else -> "other"
                }
                
                // Update card in list
                val index = sampleCards.indexOfFirst { it.id == card.id }
                if (index != -1) {
                    sampleCards[index] = card.copy(
                        name = cardName.ifEmpty { card.name },
                        type = cardType,
                        limit = cardLimit
                    )
                    cardAdapter.updateCards(sampleCards)
                }
                
                showPixelToast("Card updated successfully!")
                dialog.dismiss()
            }
            
            // Set up cancel button
            dialogView.findViewById<Button>(R.id.btnCancel)?.setOnClickListener {
                dialog.dismiss()
            }
            
            dialog.show()
        } catch (e: Exception) {
            Log.e("BankingActivity", "Error showing edit card dialog: ${e.message}")
            showPixelToast("Error showing edit card dialog")
        }
    }
    
    private fun applyPixelFontToDialog(dialogView: View) {
        try {
            val pixelFont = ResourcesCompat.getFont(this, R.font.pixel_game)
            
            // Apply to all TextViews in the dialog
            val viewsToApplyFont = listOf<Int>(
                R.id.tvDialogTitle,
                R.id.etCardName,
                R.id.etCardNumber,
                R.id.rbVisa,
                R.id.rbMastercard,
                R.id.rbOther,
                R.id.etMonthlyLimit,
                R.id.btnCancel,
                R.id.btnSave
            )
            
            for (id in viewsToApplyFont) {
                val view = dialogView.findViewById<View>(id)
                if (view is TextView) {
                    view.typeface = pixelFont
                }
            }
            
        } catch (e: Exception) {
            Log.e("BankingActivity", "Error applying pixel font to dialog: ${e.message}")
        }
    }
    
    private fun applyPixelFontToTextViews() {
        try {
            val pixelFont = ResourcesCompat.getFont(this, R.font.pixel_game)
            
            // Apply to all TextViews in the activity
            findViewById<TextView>(R.id.tvTitle)?.typeface = pixelFont
            findViewById<TextView>(R.id.tvCardsHeader)?.typeface = pixelFont
            
            // Apply to TextViews in empty state
            val emptyStateViewGroup = emptyStateView as ViewGroup
            for (i in 0 until emptyStateViewGroup.childCount) {
                val child = emptyStateViewGroup.getChildAt(i)
                if (child is TextView) {
                    child.typeface = pixelFont
                }
            }
            
            // Apply to button
            btnAddCard.typeface = pixelFont
            
        } catch (e: Exception) {
            Log.e("BankingActivity", "Error applying pixel font: ${e.message}")
        }
    }
    
    private fun showPixelToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("BankingActivity", "Error showing toast: ${e.message}")
        }
    }
    
    private fun updateProfilePicture(uri: Uri) {
        try {
            Log.d("BankingActivity", "Updating profile picture with URI: $uri")
            // Update profile picture ImageView with the selected image
            imgProfilePicture.setImageURI(uri)
            showPixelToast("Profile picture updated!")
        } catch (e: Exception) {
            Log.e("BankingActivity", "Error updating profile picture: ${e.message}", e)
            showPixelToast("Error updating profile picture")
        }
    }
    
    private fun openImagePicker() {
        try {
            Log.d("BankingActivity", "Opening image picker")
            
            // Check if we need to request storage permission (for Android 6.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                when {
                    // For Android 13+ (API 33+), we use READ_MEDIA_IMAGES
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.READ_MEDIA_IMAGES
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.d("BankingActivity", "Requesting READ_MEDIA_IMAGES permission")
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                                STORAGE_PERMISSION_REQUEST_CODE
                            )
                            return
                        }
                    }
                    // For Android 6.0 to 12, we use READ_EXTERNAL_STORAGE
                    else -> {
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.d("BankingActivity", "Requesting READ_EXTERNAL_STORAGE permission")
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                STORAGE_PERMISSION_REQUEST_CODE
                            )
                            return
                        }
                    }
                }
            }
            
            // Permission granted or not needed, launch image picker
            launchImagePicker()
            
        } catch (e: Exception) {
            Log.e("BankingActivity", "Error in openImagePicker: ${e.message}", e)
            showPixelToast("Error opening image picker")
        }
    }
    
    private fun launchImagePicker() {
        try {
            Log.d("BankingActivity", "Launching image picker intent")
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("BankingActivity", "Error launching image picker: ${e.message}", e)
            showPixelToast("Error launching image picker")
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("BankingActivity", "Storage permission granted via onRequestPermissionsResult")
                    launchImagePicker()
                } else {
                    Log.w("BankingActivity", "Storage permission denied via onRequestPermissionsResult")
                    showPixelToast("Storage permission is required to change profile picture")
                }
            }
        }
    }
    
    private fun setupNavigationBarClickListeners() {
        try {
            // Set up Home button
            findViewById<LinearLayout>(R.id.btnHomeNav)?.setOnClickListener {
                try {
                    val intent = Intent(this, Class.forName("com.example.pocketsafe.ui.MainMenu"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("BankingActivity", "Error navigating to MainMenu: ${e.message}", e)
                    Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Set up Banking button (current screen - do nothing)
            findViewById<LinearLayout>(R.id.btnBankingNav)?.setOnClickListener {
                // Already on banking screen
                Log.d("BankingActivity", "Already on Banking screen")
            }
            
            // Set up Analytics button
            findViewById<LinearLayout>(R.id.btnAnalyticsNav)?.setOnClickListener {
                try {
                    val intent = Intent(this, Class.forName("com.example.pocketsafe.ViewExpensesActivity"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("BankingActivity", "Error navigating to ViewExpensesActivity: ${e.message}", e)
                    Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("BankingActivity", "Error setting up navigation: ${e.message}", e)
        }
    }
    
    private fun toggleCardLock(card: BankCard, shouldLock: Boolean) {
        try {
            // Find the card in the list and update its lock status
            val cardIndex = sampleCards.indexOfFirst { it.id == card.id }
            if (cardIndex != -1) {
                sampleCards[cardIndex] = card.copy(isLocked = shouldLock)
                cardAdapter.updateCards(sampleCards)
                
                val statusMessage = if (shouldLock) "Card locked" else "Card unlocked"
                Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("BankingActivity", "Error toggling card lock: ${e.message}", e)
            Toast.makeText(this, "Error updating card status", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1001
    }
}
