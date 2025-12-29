package com.adam.pertepiece

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView // <--- Indispensable pour l'image
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load // <--- Indispensable pour charger l'URL

class DeclarationAdapter(
    private var declarations: List<Declaration>,
    private val onItemClick: (Declaration) -> Unit
) : RecyclerView.Adapter<DeclarationAdapter.DeclarationViewHolder>() {

    // On récupère les vues définies dans item_declaration.xml
    class DeclarationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDocType: TextView = view.findViewById(R.id.tvDocType) // J'ai renommé tvTitle en tvDocType pour coller au XML
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val ivItemImage: ImageView = view.findViewById(R.id.ivItemImage) // <--- NOUVEAU : L'image
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeclarationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_declaration, parent, false)
        return DeclarationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeclarationViewHolder, position: Int) {
        val item = declarations[position]

        // 1. Textes (Avec sécurité anti-null "?:")
        holder.tvDate.text = item.incidentDate ?: ""
        holder.tvLocation.text = item.incidentLocation ?: "Lieu non précisé"

        // 2. Nom du document
        val docId = item.documentTypeId ?: 0L
        val docName = when (docId) {
            1L -> "CNI"
            2L -> "Passeport"
            3L -> "Permis"
            4L -> "Extrait"
            else -> "Autre"
        }
        holder.tvDocType.text = docName

        // 3. Statut et Couleurs
        val status = item.status ?: "EN_ATTENTE"
        holder.tvStatus.text = status
        when (status) {
            "EN_ATTENTE" -> holder.tvStatus.setTextColor(Color.parseColor("#FF9800")) // Orange
            "RETROUVE", "TROUVE", "VALIDE" -> holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")) // Vert
            "REJETE" -> holder.tvStatus.setTextColor(Color.parseColor("#F44336")) // Rouge
            else -> holder.tvStatus.setTextColor(Color.GRAY)
        }

        // 4. GESTION DE L'IMAGE (C'est ici la magie !) 📸
        if (!item.imageUrl.isNullOrEmpty()) {
            // Si on a une URL, on charge l'image avec Coil
            holder.ivItemImage.load(item.imageUrl) {
                crossfade(true) // Petit effet de fondu sympa
                error(android.R.drawable.ic_menu_gallery) // Si l'image ne charge pas, on met une icône par défaut
            }
        } else {
            // Si pas d'URL, on remet l'icône par défaut (important pour le recyclage des vues)
            holder.ivItemImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // 5. Clic sur la carte
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = declarations.size

    // Fonction pour mettre à jour la liste depuis le MainActivity
    fun updateList(newList: List<Declaration>) {
        declarations = newList
        notifyDataSetChanged()
    }

    // Ajout pour compatibilité si tu appelles updateData ailleurs
    fun updateData(newList: List<Declaration>) {
        updateList(newList)
    }
}