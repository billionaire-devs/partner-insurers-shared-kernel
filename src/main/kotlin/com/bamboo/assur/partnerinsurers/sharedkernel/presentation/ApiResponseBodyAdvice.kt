package com.bamboo.assur.partnerinsurers.sharedkernel.presentation

import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import java.time.OffsetDateTime
import kotlin.collections.get

@ControllerAdvice
class ApiResponseBodyAdvice(private val servletRequest: HttpServletRequest) : ResponseBodyAdvice<Any> {

    private fun sanitize(input: String?): String? = input?.replace(Regex("[<>\"']"), "")

    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean = true

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? {
        // Don't wrap if already an ApiResponse
        if (body is ApiResponse<*>) return body

        // Obtain servlet request/response to read status and URI
        val servletRequest = if (request is ServletServerHttpRequest) request.servletRequest else this.servletRequest
        val servletResponse = if (response is ServletServerHttpResponse) response.servletResponse else null

        val statusCode = servletResponse?.status?.let { HttpStatus.resolve(it) } ?: HttpStatus.OK

        val reqMeta = RequestMetadata(
            method = sanitize(servletRequest.method) ?: servletRequest.method,
            path = sanitize(servletRequest.requestURI) ?: servletRequest.requestURI,
            query = sanitize(servletRequest.queryString)
        )

        val respMeta = ResponseMetadata(
            status = statusCode.value(),
            reason = statusCode.reasonPhrase,
            timestamp = OffsetDateTime.now()
        )

        val meta = Meta(request = reqMeta, response = respMeta)

        // For non-2xx statuses, treat the controller body as an error payload
        if (!statusCode.is2xxSuccessful) {
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

            val message = sanitize(rawMessage)

            val errorBody = ErrorBody(message = message, details = null)

            return ApiResponse(
                success = false,
                meta = meta,
                data = null,
                error = errorBody
            )
        }

        return ApiResponse(
            success = true,
            meta = meta,
            data = body,
            error = null
        )
    }
}
