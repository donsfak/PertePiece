package com.adam.pertepiece

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeclarationAdapter(
    private var declarations: List<Declaration>,
    private val onItemClick: (Declaration) -> Unit
) : RecyclerView.Adapter<DeclarationAdapter.DeclarationViewHolder>() {

    class DeclarationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val cardView: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeclarationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_declaration, parent, false)
        return DeclarationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeclarationViewHolder, position: Int) {
        val item = declarations[position]
        
        holder.tvDate.text = item.incidentDate
        holder.tvLocation.text = item.incidentLocation
        
        // Map document type ID to name
        val docName = when (item.documentTypeId) {
            1L -> "Carte Nationale d'Identité"
            2L -> "Passeport Biométrique"
            3L -> "Permis de Conduire"
            4L -> "Extrait de Naissance"
            else -> "Autre Document"
        }
        holder.tvTitle.text = docName

        holder.tvStatus.text = item.status
        
        // Color code status (Simple version)
        when (item.status) {
            "EN_ATTENTE" -> holder.tvStatus.setTextColor(Color.parseColor("#FF9800"))
            "RETROUVE", "TROUVE", "VALIDE" -> holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            "REJETE" -> holder.tvStatus.setTextColor(Color.parseColor("#F44336"))
            else -> holder.tvStatus.setTextColor(Color.GRAY)
        }

        holder.cardView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = declarations.size

    fun updateList(newList: List<Declaration>) {
        declarations = newList
        notifyDataSetChanged()
    }
}
