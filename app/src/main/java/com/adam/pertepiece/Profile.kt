package com.adam.pertepiece

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String, // L'ID unique qui vient de l'Authentification
    @SerialName("full_name") val fullName: String, // Le nom tapé dans le formulaire
    val role: String = "CITOYEN" // Par défaut
)