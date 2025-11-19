package com.bamboo.assur.partnerinsurers.sharedkernel.presentation

import com.bamboo.assur.partnerinsurers.sharedkernel.domain.DomainException
import com.bamboo.assur.partnerinsurers.sharedkernel.domain.EntityAlreadyExistsException
import com.bamboo.assur.partnerinsurers.sharedkernel.domain.EntityNotFoundException
import com.bamboo.assur.partnerinsurers.sharedkernel.domain.FailedToSaveEntityException
import com.bamboo.assur.partnerinsurers.sharedkernel.domain.FailedToUpdateEntityException
import com.bamboo.assur.partnerinsurers.sharedkernel.domain.ValidationError
import com.bamboo.assur.partnerinsurers.sharedkernel.domain.ValidationException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanInstantiationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mapping.model.MappingInstantiationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Consolidated exception handling for REST controllers, mapping common failures to
 * the shared [ApiResponse] envelope while logging actionable details.
 */
@Suppress("TooManyFunctions")
@RestControllerAdvice
class GlobalExceptionHandler(
    private val presentationProperties: SharedKernelPresentationProperties,
) {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Extracts a correlation identifier from well-known headers when present.
     */
    private fun extractCorrelationId(request: HttpServletRequest): String? =
        request.getHeader("X-Correlation-Id")
            ?: request.getHeader("X-Request-Id")

    /**
     * Builds a standardized [ApiResponse] error payload with metadata.
     *
     * @param status HTTP status to expose in the envelope.
     * @param message Human-readable error message.
     * @param request Current HTTP servlet request.
     * @param code Optional machine-readable error code.
     * @param details Optional key/value details that will be propagated as-is
     *   into [ErrorBody.details].
     */
    private fun buildApiResponse(
        status: HttpStatus,
        message: String?,
        request: HttpServletRequest,
        code: String? = null,
        details: Map<String, String?>? = null,
    ): ApiResponse<Any> {
        val now: Instant = Clock.System.now()

        val requestStart = request.getAttribute(SharedKernelRequestTimingFilter.REQUEST_START_ATTRIBUTE) as? Instant
        val processingTimeMs = requestStart?.let { start -> (now - start).inWholeMilliseconds }

        val reqMeta = RequestMetadata(
            method = request.method,
            path = request.requestURI,
            query = request.queryString,
            correlationId = extractCorrelationId(request),
        )

        val respMeta = ResponseMetadata(
            status = status.value(),
            statusCode = status.value(),
            reason = status.reasonPhrase,
            timestamp = now,
            processingTimeMs = processingTimeMs,
        )

        val errorBody = ErrorBody(
            message = message,
            code = code,
            details = details,
        )

        val meta = Meta(
            request = reqMeta,
            response = respMeta,
            version = presentationProperties.metaDefaults.version,
            environment = presentationProperties.metaDefaults.environment,
        )

        return ApiResponse(
            success = false,
            meta = meta,
            data = null,
            error = errorBody,
        )
    }

    /**
     * Flattens a list of [ValidationError] into a map representation for [ErrorBody.details].
     */
    private fun buildValidationDetails(errors: List<ValidationError>): Map<String, String?> =
        buildMap {
            errors.forEachIndexed { index, error ->
                val key = error.field?.takeIf { it.isNotBlank() }?.let { "field.$it" } ?: "error.$index"
                put(key, error.message)
            }
            put("totalErrors", errors.size.toString())
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
            message = "Invalid request: ${ex.message ?: "Invalid argument provided"}",
            request = request,
            details = details,
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
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        val errors = buildList<ValidationError> {
            ex.bindingResult.fieldErrors.forEach { error ->
                add(
                    ValidationError(
                        field = error.field,
                        message = error.defaultMessage ?: "Validation failed",
                    ),
                )
            }
            ex.bindingResult.globalErrors.forEach { error ->
                add(
                    ValidationError(
                        field = error.objectName,
                        message = error.defaultMessage ?: "Validation failed",
                    ),
                )
            }
        }

        val details = buildValidationDetails(errors)
        val errorMessage = "Validation failed. ${errors.size} error(s)."

        logger.error("Validation failed: ${ex.message}")
        val response = buildApiResponse(
            status = HttpStatus.BAD_REQUEST,
            message = errorMessage,
            request = request,
            code = "VALIDATION_FAILED",
            details = details,
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        val errors = ex.constraintViolations.map { violation ->
            val path = violation.propertyPath?.toString()?.takeIf { it.isNotBlank() }
            ValidationError(
                field = path,
                message = violation.message,
            )
        }

        val details = buildValidationDetails(errors)
        val errorMessage = "Validation failed. ${errors.size} constraint(s) violated."

        logger.error("Constraint violation: ${ex.message}", ex)
        val response = buildApiResponse(
            status = HttpStatus.BAD_REQUEST,
            message = errorMessage,
            request = request,
            code = "CONSTRAINT_VIOLATION",
            details = details,
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        val parameterName = ex.parameterName
        val parameterType = ex.parameterType

        val details = mapOf(
            "parameterName" to parameterName,
            "parameterType" to parameterType,
            "message" to "Required request parameter '$parameterName' of type '$parameterType' is missing",
        )

        val response = buildApiResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Missing required request parameter '$parameterName'",
            request = request,
            code = "MISSING_REQUEST_PARAMETER",
            details = details,
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
    fun handleEntityAlreadyExistsException(
        ex: EntityAlreadyExistsException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.error("Entity already exists: ${ex.message}", ex)
        val details = mapOf(
            "errorType" to ex.javaClass.simpleName,
            "message" to (ex.message ?: "A resource with the same identifier already exists"),
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

    @ExceptionHandler(ValidationException::class)
    fun handleDomainValidation(
        ex: ValidationException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        val details = buildValidationDetails(ex.errors)
        val body = buildApiResponse(
            status = HttpStatus.BAD_REQUEST,
            message = ex.message,
            request = request,
            code = "VALIDATION_FAILED",
            details = details,
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(DomainException::class)
    fun handleDomain(ex: DomainException, request: HttpServletRequest): ResponseEntity<ApiResponse<Any>> {
        val body = buildApiResponse(HttpStatus.BAD_REQUEST, ex.message, request)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        ex: IllegalStateException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.error("Illegal state encountered: ${ex.message}", ex)
        val details = mapOf(
            "errorType" to ex.javaClass.simpleName,
            "message" to (ex.message ?: "No error message available"),
            "suggestion" to "Retry the request or contact support if the issue persists"
        )
        val response = buildApiResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = ex.message ?: "The system is in an unexpected state",
            request = request,
            details = details
        )
        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(Throwable::class)
    fun handleUnexpected(ex: Throwable, request: HttpServletRequest): ResponseEntity<ApiResponse<Any>> {
        val path = request.requestURI
        logger.error("Unexpected error while handling request $path", ex)

        val details = mapOf(
            "errorType" to ex.javaClass.simpleName,
            "message" to (ex.message ?: "No error message available"),
            "path" to path,
            "timestamp" to Clock.System.now().toString(),
            "suggestion" to "Please contact support if the problem persists",
        )

        val body = buildApiResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = "An unexpected error occurred while processing your request",
            request = request,
            details = details,
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

    @ExceptionHandler(FailedToUpdateEntityException::class)
    fun handleFailedToUpdateEntity(
        ex: FailedToUpdateEntityException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        logger.error("Failed to update entity: ${ex.message}", ex)
        val body = buildApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.message, request)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(
        ex: NoSuchElementException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>> {
        logger.error("No such element: ${ex.message}", ex)
        val details = mapOf(
            "errorType" to ex.javaClass.simpleName,
            "message" to (ex.message ?: "The requested element was not found"),
            "suggestion" to "Verify that the requested resource exists and try again"
        )
        val response = buildApiResponse(
            status = HttpStatus.NOT_FOUND,
            message = ex.message ?: "The requested element was not found",
            request = request,
            details = details
        )
        return ResponseEntity(response, HttpStatus.NOT_FOUND)
    }
}
