package com.bamboo.assur.partnerinsurers.sharedkernel.presentation

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Captures HTTP request metadata to include in envelope responses.
 */
@Serializable
data class RequestMetadata(
    /** HTTP method (GET, POST, etc.). */
    val method: String,
    /**
     * Resolved request path.
     *
     * This is emitted as the raw servlet path and is not HTML-escaped so that
     * log aggregation and debugging remain straightforward.
     */
    val path: String,
    /**
     * Optional raw query string when present. This is emitted without escaping
     * for the same reasons as [path].
     */
    val query: String? = null,
    /**
     * Optional correlation identifier used for distributed tracing. This will
     * typically be populated from headers such as `X-Correlation-Id` or
     * `X-Request-Id` when present.
     */
    val correlationId: String? = null,
)

/**
 * Captures HTTP response metadata to include in envelope responses.
 *
 * Prefer passing an explicit `timestamp` in tests or batch jobs when deterministic
 * values are required; otherwise the property defaults to the current time.
 */
@Serializable
data class ResponseMetadata(
    /** Numeric status code returned by the controller. */
    val status: Int,
    /**
     * Duplicate numeric status code for clients that prefer the `statusCode`
     * naming convention. Always equal to [status].
     */
    val statusCode: Int = status,
    /** Human readable reason phrase associated with the status code. */
    val reason: String,
    /**
     * Timestamp of the response creation.
     *
     * This is serialized as an ISO-8601 string. API consumers that prefer
     * epoch milliseconds can convert this value on their side.
     */
    @Contextual
    val timestamp: Instant = Clock.System.now(),
    /**
     * Optional processing time in milliseconds between request reception and
     * response envelope creation. This value may be `null` when the timing
     * filter is not registered or when the start timestamp is unavailable.
     */
    val processingTimeMs: Long? = null,
)

/**
 * Aggregates request/response information for traceability.
 */
@Serializable
data class Meta(
    val request: RequestMetadata,
    val response: ResponseMetadata,
    /**
     * Optional logical API version (for example `v1`, `2024-11-19`). When
     * configured, this is propagated from
     * [SharedKernelPresentationProperties.MetaDefaults.version].
     */
    val version: String? = null,
    /**
     * Optional environment label such as `dev`, `staging`, or `prod`. When
     * configured, this is propagated from
     * [SharedKernelPresentationProperties.MetaDefaults.environment].
     */
    val environment: String? = null,
)

/**
 * Structured payload describing the error state.
 */
@Serializable
data class ErrorBody(
    /** Short human-readable description of the error. */
    val message: String?,
    /**
     * Optional machine-readable error code that can be used by clients to
     * implement programmatic handling (e.g. `VALIDATION_FAILED`).
     */
    val code: String? = null,
    /**
     * Optional structured details (e.g. validation errors, additional context).
     *
     * The values are treated as opaque data and are **not** HTML-escaped by
     * the shared kernel; escaping should be performed by whichever consumer
     * renders the values in an HTML context.
     */
    val details: Map<String, String?>? = null,
)

/**
 * Standardized JSON envelope returned by REST controllers.
 *
 * Treat this structure as part of the public API contract exposed to consumers.
 * Favour additive, backwards-compatible changes and document any breaking updates.
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
    val error: ErrorBody? = null,
)
