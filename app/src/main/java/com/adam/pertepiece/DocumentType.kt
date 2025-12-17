package com.adam.pertepiece

data class DocumentType(val id: Long, val name: String) {
    override fun toString(): String = name
}