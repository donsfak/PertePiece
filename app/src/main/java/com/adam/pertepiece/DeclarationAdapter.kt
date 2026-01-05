package com.adam.pertepiece

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class DeclarationAdapter(
    private var declarations: List<Declaration>,
    private val onItemClick: (Declaration) -> Unit
) : RecyclerView.Adapter<DeclarationAdapter.DeclarationViewHolder>() {

    class DeclarationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDocType: TextView = view.findViewById(R.id.tvDocType)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val ivItemImage: ImageView = view.findViewById(R.id.ivItemImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeclarationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_declaration, parent, false)
        return DeclarationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeclarationViewHolder, position: Int) {
        val item = declarations[position]

        // 1. Textes
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

        // 3. Statut
        val status = item.status ?: "EN_ATTENTE"
        holder.tvStatus.text = status
        when (status) {
            "EN_ATTENTE" -> holder.tvStatus.setTextColor(Color.parseColor("#FF9800"))
            "RETROUVE", "TROUVE", "VALIDE" -> holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            "REJETE" -> holder.tvStatus.setTextColor(Color.parseColor("#F44336"))
            else -> holder.tvStatus.setTextColor(Color.GRAY)
        }

        // 4. GESTION DE L'IMAGE (CORRIGÉE) 📸
        if (!item.imageUrl.isNullOrBlank()) {
            // CAS A : IL Y A UNE PHOTO
            // 1. On enlève le filtre de couleur (sinon la photo sera grise)
            holder.ivItemImage.clearColorFilter()

            // 2. On zoome pour remplir le carré (joli)
            holder.ivItemImage.scaleType = ImageView.ScaleType.CENTER_CROP

            // 3. On charge l'image
            holder.ivItemImage.load(item.imageUrl) {
                crossfade(true)
                // Placeholder pendant le chargement (optionnel)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_gallery)
            }
        } else {
            // CAS B : PAS DE PHOTO (URL vide ou null)
            // 1. On annule tout chargement Coil en cours sur cette vue
            holder.ivItemImage.load(null) // Important pour stopper Coil

            // 2. On met l'icône par défaut
            holder.ivItemImage.setImageResource(android.R.drawable.ic_menu_gallery)

            // 3. On dézoome pour voir l'icône en entier au centre (CRUCIAL)
            holder.ivItemImage.scaleType = ImageView.ScaleType.CENTER_INSIDE

            // 4. On teinte l'icône en gris pour faire "placeholder" (CRUCIAL)
            holder.ivItemImage.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.SRC_IN)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = declarations.size

    fun updateList(newList: List<Declaration>) {
        declarations = newList
        notifyDataSetChanged()
    }

    fun updateData(newList: List<Declaration>) {
        updateList(newList)
    }
}