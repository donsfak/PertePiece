package com.adam.pertepiece

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi 

@OptIn(InternalSerializationApi::class) 
@Serializable
data class Declaration(
    val id: String? = null,

    @SerialName("user_id") val userId: String,
    @SerialName("document_type_id") val documentTypeId: Long,
    @SerialName("incident_date") val incidentDate: String,
    @SerialName("incident_location") val incidentLocation: String,
    val description: String,
    val status: String = "EN_ATTENTE"
)