package com.example.pocketsafe.ui.activity

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R
import com.example.pocketsafe.data.AppDatabase
import com.example.pocketsafe.data.BillSplit
import com.example.pocketsafe.data.BillSplitMember
import com.example.pocketsafe.ui.activity.BaseActivity
import com.example.pocketsafe.ui.adapters.BillSplitMemberAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.properties.Delegates

class BillSplitActivity : BaseActivity() {
    
    private lateinit var db: AppDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var occasionInput: EditText
    private lateinit var memberNameInput: EditText
    private lateinit var memberAmountInput: EditText
    private lateinit var membersRecyclerView: RecyclerView
    private lateinit var totalAmountText: TextView
    private lateinit var shareCodeText: TextView
    private lateinit var downloadCodeInput: EditText
    
    private val members = mutableListOf<BillSplitMember>()
    private lateinit var memberAdapter: BillSplitMemberAdapter
    private var currentShareCode = ""
    private var billId by Delegates.notNull<Long>()
    
    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            showPixelToast("Storage permission required for saving receipts")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_split)
        
        setupDatabase()
        setupViews()
        checkStoragePermission()
        setupNavigation()
    }
    
    private fun setupDatabase() {
        db = AppDatabase.getDatabase(this)
        firestore = FirebaseFirestore.getInstance()
    }
    
    private fun setupViews() {
        // Initialize views
        occasionInput = findViewById(R.id.occasionInput)
        memberNameInput = findViewById(R.id.memberNameInput)
        memberAmountInput = findViewById(R.id.memberAmountInput)
        membersRecyclerView = findViewById(R.id.membersRecyclerView)
        totalAmountText = findViewById(R.id.totalAmountText)
        shareCodeText = findViewById(R.id.shareCodeText)
        downloadCodeInput = findViewById(R.id.downloadCodeInput)
        
        // Apply pixel font
        val pixelFont = ResourcesCompat.getFont(this, R.font.pixel_game)
        listOf(occasionInput, memberNameInput, memberAmountInput, 
               totalAmountText, shareCodeText, downloadCodeInput).forEach {
            when (it) {
                is EditText -> it.typeface = pixelFont
                is TextView -> it.typeface = pixelFont
            }
        }
        
        // Setup RecyclerView
        memberAdapter = BillSplitMemberAdapter(members) { member ->
            removeMember(member)
        }
        membersRecyclerView.layoutManager = LinearLayoutManager(this)
        membersRecyclerView.adapter = memberAdapter
        
        // Setup buttons
        findViewById<Button>(R.id.addMemberButton).setOnClickListener {
            addMember()
        }
        
        findViewById<Button>(R.id.saveSplitButton).setOnClickListener {
            saveSplit()
        }
        
        findViewById<Button>(R.id.downloadReceiptButton).setOnClickListener {
            downloadReceiptByCode()
        }
    }
    
    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
    
    private fun addMember() {
        val name = memberNameInput.text.toString().trim()
        val amount = memberAmountInput.text.toString().toDoubleOrNull()
        
        if (name.isEmpty() || amount == null || amount <= 0) {
            showPixelToast("Please enter valid name and amount")
            return
        }
        
        val member = BillSplitMember(
            name = name,
            amount = amount,
            splitId = ""
        )
        
        members.add(member)
        memberAdapter.notifyItemInserted(members.size - 1)
        
        // Clear inputs
        memberNameInput.text.clear()
        memberAmountInput.text.clear()
        
        updateTotal()
    }
    
    private fun removeMember(member: BillSplitMember) {
        memberAdapter.removeMember(member)
        updateTotal()
    }
    
    private fun updateTotal() {
        val total = members.sumOf { it.amount }
        totalAmountText.text = "Total: R${String.format("%.2f", total)}"
    }
    
    private fun saveSplit() {
        val occasion = occasionInput.text.toString().trim()
        
        if (occasion.isEmpty() || members.isEmpty()) {
            showPixelToast("Please enter occasion and add members")
            return
        }
        
        lifecycleScope.launch {
            try {
                // Generate unique share code
                val shareCode = generateShareCode()
                
                // Create BillSplit entity
                val billSplit = BillSplit(
                    id = shareCode,
                    occasion = occasion,
                    totalAmount = members.sumOf { it.amount },
                    memberCount = members.size,
                    createdDate = System.currentTimeMillis(),
                    shareCode = shareCode
                )
                
                // Save to local database
                withContext(Dispatchers.IO) {
                    db.billSplitDao().insertBillSplit(billSplit)
                    
                    // Save members
                    members.forEach { member ->
                        db.billSplitDao().insertMember(
                            member.copy(splitId = shareCode)
                        )
                    }
                }
                
                // Save to Firebase
                saveToFirebase(billSplit, members)
                
                // Generate PDF immediately
                generatePdfReceipt(occasion, members, shareCode)
                
                // Update UI
                currentShareCode = shareCode
                shareCodeText.text = "Share Code: $shareCode"
                shareCodeText.visibility = View.VISIBLE
                
                showPixelToast("Split saved! PDF receipt generated")
                
            } catch (e: Exception) {
                showPixelToast("Error saving split: ${e.message}")
            }
        }
    }
    
    private fun generateShareCode(): String {
        return UUID.randomUUID().toString().take(8).uppercase()
    }
    
    private fun saveToFirebase(billSplit: BillSplit, members: List<BillSplitMember>) {
        val data = hashMapOf(
            "occasion" to billSplit.occasion,
            "totalAmount" to billSplit.totalAmount,
            "memberCount" to billSplit.memberCount,
            "createdDate" to billSplit.createdDate,
            "members" to members.map { 
                mapOf(
                    "name" to it.name,
                    "amount" to it.amount
                )
            }
        )
        
        firestore.collection("billSplits")
            .document(billSplit.shareCode)
            .set(data)
            .addOnFailureListener {
                showPixelToast("Failed to sync with cloud")
            }
    }
    
    private fun generatePdfReceipt(occasion: String, members: List<BillSplitMember>, shareCode: String) {
        try {
            val fileName = "Receipt-$occasion-${System.currentTimeMillis()}.pdf"
            
            // Create file in Downloads folder
            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for Android 10+
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, "Download/PocketSafe")
                }
                
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let { contentResolver.openOutputStream(it) }
            } else {
                // Direct file access for older versions
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val pocketSafeDir = File(downloadsDir, "PocketSafe")
                if (!pocketSafeDir.exists()) pocketSafeDir.mkdirs()
                
                val file = File(pocketSafeDir, fileName)
                FileOutputStream(file)
            }
            
            file?.let { outputStream ->
                // Create PDF
                val writer = PdfWriter(outputStream)
                val pdf = PdfDocument(writer)
                val document = Document(pdf)
                
                // Add pixel-themed header
                document.add(Paragraph("POCKETSAFE BILL SPLIT RECEIPT")
                    .setFontSize(20f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.BLACK))
                
                document.add(Paragraph("").setMarginBottom(10f))
                
                // Add occasion and code
                document.add(Paragraph("Occasion: $occasion")
                    .setFontSize(16f)
                    .setBold())
                
                document.add(Paragraph("Share Code: $shareCode")
                    .setFontSize(14f)
                    .setFontColor(ColorConstants.DARK_GRAY))
                
                document.add(Paragraph("").setMarginBottom(20f))
                
                // Create table for members
                val table = Table(2)
                table.addHeaderCell("Member")
                table.addHeaderCell("Amount")
                
                members.forEach { member ->
                    table.addCell(member.name)
                    table.addCell("R${String.format("%.2f", member.amount)}")
                }
                
                document.add(table)
                
                document.add(Paragraph("").setMarginBottom(20f))
                
                // Add total
                val total = members.sumOf { it.amount }
                document.add(Paragraph("TOTAL: R${String.format("%.2f", total)}")
                    .setFontSize(18f)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT))
                
                // Add timestamp
                document.add(Paragraph("Generated: ${Date()}")
                    .setFontSize(10f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20f))
                
                document.close()
                outputStream.close()
                
                // Save receipt info to database for analytics
                saveReceiptInfo(fileName, shareCode, occasion)
                
                showPixelToast("Receipt saved to Downloads/PocketSafe")
            }
            
        } catch (e: Exception) {
            showPixelToast("Error generating PDF: ${e.message}")
        }
    }
    
    private fun saveReceiptInfo(fileName: String, shareCode: String, occasion: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Save receipt metadata for analytics
            val receiptInfo = hashMapOf(
                "fileName" to fileName,
                "shareCode" to shareCode,
                "occasion" to occasion,
                "createdDate" to System.currentTimeMillis(),
                "type" to "BILL_SPLIT"
            )
            
            // Save to Firebase for analytics
            firestore.collection("receipts")
                .document(shareCode)
                .set(receiptInfo)
        }
    }
    
    private fun downloadReceiptByCode() {
        val code = downloadCodeInput.text.toString().trim().uppercase()
        
        if (code.isEmpty()) {
            showPixelToast("Please enter a share code")
            return
        }
        
        lifecycleScope.launch {
            try {
                // First check local database
                val localSplit = withContext(Dispatchers.IO) {
                    db.billSplitDao().getBillSplitByCode(code)
                }
                
                if (localSplit != null) {
                    // Load from local
                    val members = withContext(Dispatchers.IO) {
                        db.billSplitDao().getMembersForSplit(code)
                    }
                    generatePdfReceipt(localSplit.occasion, members, code)
                } else {
                    // Try Firebase
                    firestore.collection("billSplits")
                        .document(code)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val occasion = document.getString("occasion") ?: "Unknown"
                                val membersList = document.get("members") as? List<Map<String, Any>> ?: emptyList()
                                
                                val members = membersList.map { map ->
                                    BillSplitMember(
                                        name = map["name"] as? String ?: "",
                                        amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
                                        splitId = code
                                    )
                                }
                                
                                generatePdfReceipt(occasion, members, code)
                            } else {
                                showPixelToast("Share code not found")
                            }
                        }
                        .addOnFailureListener {
                            showPixelToast("Error downloading receipt")
                        }
                }
            } catch (e: Exception) {
                showPixelToast("Error: ${e.message}")
            }
        }
    }
    
    private fun showPixelToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun setupNavigation() {
        // Setup bottom navigation if needed
        findViewById<View>(R.id.bottomNavigation)?.let { navView ->
            // Add navigation logic here if required
        }
    }
}
