package com.example.avito_testing_2026_autum.ai.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequestDto(
    @SerialName("model")
    val model: String,
    @SerialName("messages")
    val messages: List<MessageDto>,
    @SerialName("temperature")
    val temperature: Float = 0.7f
)

@Serializable
data class MessageDto(
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: String
)

@Serializable
data class ChatResponseDto(
    @SerialName("choices")
    val choices: List<ChoiceDto>
)

@Serializable
data class ChoiceDto(
    @SerialName("message")
    val message: MessageDto,
    @SerialName("finish_reason")
    val finishReason: String? = null
)