package com.adam.pertepiece

object DocumentType {
    // La liste unique pour toute l'application
    val list = listOf(
        "Carte Nationale d'Identité", // ID 1
        "Passeport Biométrique",      // ID 2
        "Permis de Conduire",         // ID 3
        "Extrait de Naissance",       // ID 4
        "Carte Bancaire",             // ID 5
        "Autre Document"              // ID 6
    )

    // Fonction utilitaire pour récupérer le nom depuis l'ID
    fun getName(id: Long?): String {
        if (id == null || id < 1 || id > list.size) return "Document Inconnu"
        return list[(id.toInt() - 1)]
    }
}