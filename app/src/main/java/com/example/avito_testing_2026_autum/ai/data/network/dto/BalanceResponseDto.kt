package com.example.avito_testing_2026_autum.ai.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BalanceResponseDto(
    @SerialName("balance")
    val balance: List<BalanceItemDto> = emptyList()
)

@Serializable
data class BalanceItemDto(
    @SerialName("usage")
    val usage: String,
    @SerialName("value")
    val value: Long
)