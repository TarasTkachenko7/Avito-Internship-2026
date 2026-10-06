package com.example.avito_testing_2026_autum.ai.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModelsResponseDto(
    @SerialName("data")
    val data: List<ModelDto>
)

@Serializable
data class ModelDto(
    @SerialName("id")
    val id: String
)