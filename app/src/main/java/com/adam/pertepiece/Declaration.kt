package com.adam.pertepiece

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Declaration(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("document_type_id") val documentTypeId: Long? = 0,
    @SerialName("incident_date") val incidentDate: String? = "",
    @SerialName("incident_location") val incidentLocation: String? = "",
    val description: String? = "",
    val status: String? = "EN_ATTENTE",

    // C'EST ICI LA CORRECTION : Ajoute le '?' après String
    @SerialName("image_url")
    val imageUrl: String? = null
) : java.io.Serializable