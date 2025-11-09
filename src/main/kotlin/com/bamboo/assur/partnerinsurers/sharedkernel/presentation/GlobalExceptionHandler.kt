package com.bamboo.assur.partnerinsurers.sharedkernel.presentation

import com.bamboo.assur.partnerinsurers.sharedkernel.domain.DomainException
import com.bamboo.assur.partnerinsurers.sharedkernel.domain.EntityAlreadyExistsException
import com.bamboo.assur.partnerinsurers.sharedkernel.domain.EntityNotFoundException
import com.bamboo.assur.partnerinsurers.sharedkernel.domain.FailedToSaveEntityException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanInstantiationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mapping.model.MappingInstantiationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Consolidated exception handling for REST controllers, mapping common failures to
 * the shared `ApiResponse` envelope while logging actionable details.
 */
@OptIn(ExperimentalTime::class)
@Suppress("TooManyFunctions")
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /** HTML-escapes user-supplied strings to avoid reflected XSS when echoing input. */
    private fun escapeHtml(input: String?): String? = input
        ?.replace("&", "&amp;")
        ?.replace("<", "&lt;")
        ?.replace(">", "&gt;")
        ?.replace("\"", "&quot;")
        ?.replace("'", "&#x27;")

    /** Builds a standardized `ApiResponse` error payload with metadata. */
    private fun buildApiResponse(
        status: HttpStatus,
        message: String?,
        request: HttpServletRequest,
        details: Map<String, Any>? = null
    ): ApiResponse<Any> {
        val reqMeta = RequestMetadata(
            method = escapeHtml(request.method) ?: request.method,
            path = escapeHtml(request.requestURI) ?: "UNKNOWN",
            query = escapeHtml(request.queryString)
        )

        val respMeta = ResponseMetadata(
            status = status.value(),
            reason = status.reasonPhrase,
            timestamp = Clock.System.now()
        )

        val errorBody = ErrorBody(
            message = escapeHtml(message),
            details = details?.mapValues { (_, v) -> escapeHtml(v.toString()) }.toString()
        )

        val meta = Meta(request = reqMeta, response = respMeta)

        return ApiResponse(
            success = false,
            meta = meta,
            data = null,
            error = errorBody
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.error("Illegal argument: ${ex.message}", ex)
        val details = mapOf(
            "errorType" to ex.javaClass.simpleName,
            "error" to (ex.message ?: "No details available")
        )
        val response = buildApiResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Invalid request: ${escapeHtml(ex.message) ?: "Invalid argument provided"}",
            request = request,
            details = details
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val rootCause = ex.mostSpecificCause
        val (errorMessage, details) = when (rootCause) {
            is IllegalArgumentException -> {
                val message = rootCause.message ?: "Invalid request format"
                val fieldName = """\b(\w+)(?=\s*\()""".toRegex()
                    .find(message)?.groupValues?.getOrNull(1)
                
                if (fieldName != null) {
                    "Invalid request format" to mapOf(
                        "field" to fieldName,
                        "error" to message,
                        "suggestion" to "Check the provided value for '$fieldName'"
                    )
                } else {
                    "Invalid request format" to mapOf(
                        "error" to message,
                        "suggestion" to "Check the request format and required fields"
                    )
                }
            }
            else -> 
                "Invalid JSON body format" to mapOf(
                    "error" to "Request body is not a valid JSON",
                    "suggestion" to "Ensure the request body is a valid JSON document"
                )
        }
        
        logger.error("Failed to parse request: ${ex.message}", ex)
        val response = buildApiResponse(
            status = HttpStatus.BAD_REQUEST,
            message = errorMessage,
            request = request,
            details = details
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val fieldErrors = ex.bindingResult.fieldErrors.associate { error ->
            error.field to escapeHtml(error.defaultMessage ?: "Validation failed")
        }
        
        val globalErrors = ex.bindingResult.globalErrors.associate { error ->
            error.objectName to escapeHtml(error.defaultMessage ?: "Validation failed")
        }
        
        val details = mapOf(
            "fieldErrors" to fieldErrors,
            "globalErrors" to globalErrors,
            "totalErrors" to (fieldErrors.size + globalErrors.size)
        )
        
        val errorMessage = "Validation failed. ${fieldErrors.size} field(s) have errors."
        
        logger.error("Validation failed: ${ex.message}")
        val response = buildApiResponse(
            status = HttpStatus.BAD_REQUEST,
            message = errorMessage,
            request = request,
            details = details
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(
        ex: EntityNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.error("Entity not found: ${ex.message}", ex)
        val response = buildApiResponse(
            status = HttpStatus.NOT_FOUND,
            message = ex.message ?: "The requested resource was not found",
            request = request,
        )
        return ResponseEntity(response, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(EntityAlreadyExistsException::class)
    fun handleAlreadyExists(
        ex: EntityAlreadyExistsException, 
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.error("Entity already exists: ${ex.message}", ex)
        val details = mapOf(
            "errorType" to ex.javaClass.simpleName,
            "suggestion" to "Try updating the existing resource or use a different identifier"
        )
        val response = buildApiResponse(
            status = HttpStatus.CONFLICT,
            message = ex.message ?: "A resource with the same identifier already exists",
            request = request,
            details = details
        )
        return ResponseEntity(response, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(
        DuplicateKeyException::class,
        DataIntegrityViolationException::class,
    )
    fun handleDuplicateKey(ex: Throwable, request: HttpServletRequest): ResponseEntity<ApiResponse<Any>> {
        val message = ex.message ?: "Duplicate key / data integrity violation"
        val body = buildApiResponse(HttpStatus.CONFLICT, message, request)
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body)
    }

    // Handle Spring mapping/instantiation failures (e.g. when a DB row has null for a non-null constructor param)
    @ExceptionHandler(BeanInstantiationException::class)
    fun handleBeanInstantiation(
        ex: BeanInstantiationException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        val causeMessage = ex.cause?.message ?: ex.message
        val message = "Failed to instantiate projection / DTO: ${causeMessage ?: "see server logs"}"
        val body = buildApiResponse(HttpStatus.BAD_REQUEST, message, request)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(MappingInstantiationException::class)
    fun handleMappingInstantiation(
        ex: MappingInstantiationException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        val causeMessage = ex.cause?.message ?: ex.message
        val message = "Failed to map database row to projection: ${causeMessage ?: "see server logs"}"
        val body = buildApiResponse(HttpStatus.BAD_REQUEST, message, request)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(DomainException::class)
    fun handleDomain(ex: DomainException, request: HttpServletRequest): ResponseEntity<ApiResponse<Any>> {
        val body = buildApiResponse(HttpStatus.BAD_REQUEST, ex.message, request)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(Throwable::class)
    fun handleUnexpected(ex: Throwable, request: HttpServletRequest): ResponseEntity<ApiResponse<Any>> {
        val safePath = escapeHtml(request.requestURI) ?: request.requestURI
        logger.error("Unexpected error while handling request $safePath", ex)
        
        val details = mapOf(
            "errorType" to ex.javaClass.simpleName,
            "message" to (ex.message ?: "No error message available"),
            "path" to safePath,
            "timestamp" to Clock.System.now().toString(),
            "suggestion" to "Please contact support if the problem persists"
        )
        
        val body = buildApiResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = "An unexpected error occurred while processing your request",
            request = request,
            details = details
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }

    @ExceptionHandler(FailedToSaveEntityException::class)
    fun handleFailedToSaveEntity(
        ex: FailedToSaveEntityException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        val body = buildApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.message, request)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}
