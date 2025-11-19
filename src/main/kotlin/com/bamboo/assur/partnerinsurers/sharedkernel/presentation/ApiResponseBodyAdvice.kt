package com.bamboo.assur.partnerinsurers.sharedkernel.presentation

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Spring MVC advice that wraps controller responses in the shared [ApiResponse] envelope.
 *
 * The advice centralizes response formatting so controllers can focus on domain data while
 * still emitting consistent metadata and error payloads.
 */
@ControllerAdvice
class ApiResponseBodyAdvice(
    private val servletRequest: HttpServletRequest,
    private val properties: SharedKernelPresentationProperties,
) : ResponseBodyAdvice<Any> {

    /**
     * Advertises that this advice applies to controller return types when the
     * API envelope feature is enabled.
     */
    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean = properties.apiResponse.enabled

    /**
     * Wraps controller responses into a standardized envelope and enriches them with metadata.
     *
     * Non-JSON responses (e.g. file downloads, HTML) are passed through untouched to avoid
     * corrupting binary or specialized payloads.
     */
    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? {
        // If disabled, short-circuit quickly.
        if (!properties.apiResponse.enabled) {
            return body
        }

        // Only wrap JSON responses to avoid corrupting non-JSON payloads.
        if (!isJsonContentType(selectedContentType)) {
            return body
        }

        // Don't wrap if already an ApiResponse
        if (body is ApiResponse<*>) return body

        // Obtain servlet request/response to read status and URI
        val servletRequest = if (request is ServletServerHttpRequest) request.servletRequest else this.servletRequest
        val servletResponse = if (response is ServletServerHttpResponse) response.servletResponse else null

        val statusCode = servletResponse?.status?.let { HttpStatus.resolve(it) } ?: HttpStatus.OK

        val now: Instant = Clock.System.now()
        val requestStart = servletRequest.getAttribute(SharedKernelRequestTimingFilter.REQUEST_START_ATTRIBUTE) as? Instant
        val processingTimeMs = requestStart?.let { start -> (now - start).inWholeMilliseconds }

        val reqMeta = RequestMetadata(
            method = servletRequest.method,
            path = servletRequest.requestURI,
            query = servletRequest.queryString,
            correlationId = servletRequest.getHeader("X-Correlation-Id")
                ?: servletRequest.getHeader("X-Request-Id"),
        )

        val respMeta = ResponseMetadata(
            status = statusCode.value(),
            statusCode = statusCode.value(),
            reason = statusCode.reasonPhrase,
            timestamp = now,
            processingTimeMs = processingTimeMs,
        )

        val meta = Meta(
            request = reqMeta,
            response = respMeta,
            version = properties.metaDefaults.version,
            environment = properties.metaDefaults.environment,
        )

        // For non-2xx statuses, treat the controller body as an error payload
        if (!statusCode.is2xxSuccessful) {
            val errorBody = extractErrorBody(body)

            return ApiResponse(
                success = false,
                meta = meta,
                data = null,
                error = errorBody,
            )
        }

        return ApiResponse(
            success = true,
            meta = meta,
            data = body,
            error = null,
        )
    }

    private fun isJsonContentType(contentType: MediaType?): Boolean {
        if (contentType == null || contentType == MediaType.ALL) {
            return false
        }

        if (contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return true
        }

        // Support vendor-specific types such as application/vnd.my+json
        return contentType.type.equals("application", ignoreCase = true) &&
            contentType.subtype.lowercase().endsWith("+json")
    }

    private fun extractErrorBody(body: Any?): ErrorBody {
        val rawMessage = when (body) {
            null -> null
            is Map<*, *> -> {
                when {
                    body.containsKey("error") -> body["error"]?.toString()
                    body.containsKey("message") -> body["message"]?.toString()
                    else -> body.toString()
                }
            }

            is String -> body
            else -> body.toString()
        }

        return ErrorBody(message = rawMessage, details = null)
    }
}
