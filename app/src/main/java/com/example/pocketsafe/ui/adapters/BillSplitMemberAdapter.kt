package com.example.pocketsafe.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R
import com.example.pocketsafe.data.BillSplitMember

class BillSplitMemberAdapter(
    private val members: MutableList<BillSplitMember>,
    private val onRemoveMember: (BillSplitMember) -> Unit
) : RecyclerView.Adapter<BillSplitMemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberName: TextView = itemView.findViewById(R.id.tvMemberName)
        val memberAmount: TextView = itemView.findViewById(R.id.tvMemberAmount)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemoveMember)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bill_split_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        
        // Apply pixel font
        val pixelFont = ResourcesCompat.getFont(holder.itemView.context, R.font.pixel_game)
        holder.memberName.typeface = pixelFont
        holder.memberAmount.typeface = pixelFont
        
        holder.memberName.text = member.name
        holder.memberAmount.text = "R${String.format("%.2f", member.amount)}"
        
        holder.btnRemove.setOnClickListener {
            onRemoveMember(member)
        }
    }

    override fun getItemCount(): Int = members.size

    fun removeMember(member: BillSplitMember) {
        val position = members.indexOf(member)
        if (position != -1) {
            members.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
