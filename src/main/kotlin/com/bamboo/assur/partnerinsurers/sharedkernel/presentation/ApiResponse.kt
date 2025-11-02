package com.bamboo.assur.partnerinsurers.sharedkernel.presentation

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class RequestMetadata(
    val method: String,
    val path: String,
    val query: String? = null
)

@Serializable
data class ResponseMetadata(
    val status: Int,
    val reason: String,
    @Contextual
    val timestamp: OffsetDateTime = OffsetDateTime.now()
)

@Serializable
data class Meta(
    val request: RequestMetadata,
    val response: ResponseMetadata
)

@Serializable
data class ErrorBody(
    val message: String?,
    val details: String? = null
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val meta: Meta,
    val data: T? = null,
    val error: ErrorBody? = null
)
