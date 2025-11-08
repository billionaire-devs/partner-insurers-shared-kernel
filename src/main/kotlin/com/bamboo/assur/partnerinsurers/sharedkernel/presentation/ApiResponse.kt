package com.bamboo.assur.partnerinsurers.sharedkernel.presentation

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

/**
 * Captures HTTP request metadata to include in envelope responses.
 */
@Serializable
data class RequestMetadata(
    /** HTTP method (GET, POST, etc.). */
    val method: String,
    /** Resolved request path. */
    val path: String,
    /** Optional raw query string when present. */
    val query: String? = null
)

/**
 * Captures HTTP response metadata to include in envelope responses.
 */
@Serializable
data class ResponseMetadata(
    /** Numeric status code returned by the controller. */
    val status: Int,
    /** Human readable reason phrase associated with the status code. */
    val reason: String,
    /** Timestamp of the response creation. */
    @Contextual
    val timestamp: OffsetDateTime = OffsetDateTime.now()
)

/**
 * Aggregates request/response information for traceability.
 */
@Serializable
data class Meta(
    val request: RequestMetadata,
    val response: ResponseMetadata
)

/**
 * Structured payload describing the error state.
 */
@Serializable
data class ErrorBody(
    /** Short description of the error. */
    val message: String?,
    /** Optional details (stack trace, validation issues...). */
    val details: String? = null
)

/**
 * Standardized JSON envelope returned by REST controllers.
 */
@Serializable
data class ApiResponse<T>(
    /** Indicates whether the request was handled successfully. */
    val success: Boolean,
    /** Metadata related to the request and response. */
    val meta: Meta,
    /** Optional payload returned on success. */
    val data: T? = null,
    /** Optional error details when `success` is false. */
    val error: ErrorBody? = null
)
