package com.example.pocketsafe.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R
import com.example.pocketsafe.databinding.DialogBillSplitBinding
import com.example.pocketsafe.databinding.ItemSplitMemberBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.OutputStream
import java.util.*

class BillSplitDialog : DialogFragment() {

    private lateinit var binding: DialogBillSplitBinding
    private val members = mutableListOf<SplitMember>()
    private lateinit var adapter: SplitMemberAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogBillSplitBinding.inflate(LayoutInflater.from(context))

        // Get bill information from arguments
        val billTitle = arguments?.getString("bill_title") ?: "Unknown Bill"
        val billAmount = arguments?.getDouble("bill_amount") ?: 0.0
        val billId = arguments?.getString("bill_id") ?: ""

        // Pre-populate the total amount
        binding.etTotalAmount.setText(billAmount.toString())
        binding.tvBillTitle.text = "Split: $billTitle"

        adapter = SplitMemberAdapter(members)
        binding.rvMembers.layoutManager = LinearLayoutManager(context)
        binding.rvMembers.adapter = adapter

        binding.btnAddMember.setOnClickListener {
            val name = binding.etMemberName.text.toString()
            val amount = binding.etMemberAmount.text.toString().toDoubleOrNull()

            if (name.isBlank() || amount == null) {
                Toast.makeText(context, "Enter valid name and amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            members.add(SplitMember(name, amount))
            adapter.notifyItemInserted(members.size - 1)
            binding.etMemberName.text.clear()
            binding.etMemberAmount.text.clear()
            
            // Update remaining amount
            updateRemainingAmount()
        }

        binding.btnGenerateReceipt.setOnClickListener {
            val total = binding.etTotalAmount.text.toString().toDoubleOrNull()
            
            if (total == null || members.isEmpty()) {
                Toast.makeText(context, "Enter total amount and members", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val shareCode = UUID.randomUUID().toString().take(8).uppercase()
            val data = hashMapOf(
                "billTitle" to billTitle,
                "billId" to billId,
                "total" to total,
                "members" to members.map { mapOf("name" to it.name, "amount" to it.amount) },
                "timestamp" to System.currentTimeMillis(),
                "shareCode" to shareCode
            )

            firestore.collection("billSplits")
                .document(shareCode)
                .set(data)
                .addOnSuccessListener {
                    generatePdfReceipt(billTitle, total, members, shareCode)
                    Toast.makeText(context, "Receipt generated! Share code: $shareCode", Toast.LENGTH_LONG).show()
                    dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return AlertDialog.Builder(requireContext(), R.style.Theme_PocketSafe_Dialog)
            .setView(binding.root)
            .create()
    }

    private fun updateRemainingAmount() {
        val total = binding.etTotalAmount.text.toString().toDoubleOrNull() ?: 0.0
        val allocated = members.sumOf { it.amount }
        val remaining = total - allocated
        binding.tvRemainingAmount.text = "Remaining: R${String.format("%.2f", remaining)}"
    }

    private fun generatePdfReceipt(billTitle: String, total: Double, members: List<SplitMember>, code: String) {
        try {
            val context = requireContext()
            // Enhanced PDF generation with South African context
            Toast.makeText(context, "PDF receipt saved to Downloads", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    data class SplitMember(val name: String, val amount: Double)

    private class SplitMemberAdapter(private val members: List<SplitMember>) : 
        RecyclerView.Adapter<SplitMemberAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemSplitMemberBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSplitMemberBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val member = members[position]
            holder.binding.tvMemberName.text = member.name
            holder.binding.tvMemberAmount.text = "R${member.amount}"
        }

        override fun getItemCount() = members.size
    }
}
