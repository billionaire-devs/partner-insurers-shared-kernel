package com.bamboo.assur.partnerinsurers.sharedkernel.presentation

import com.bamboo.assur.partnerinsurers.sharedkernel.domain.ValidationError
import com.bamboo.assur.partnerinsurers.sharedkernel.domain.ValidationException
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Path
import jakarta.validation.metadata.ConstraintDescriptor
import kotlinx.datetime.Clock
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.bind.MissingServletRequestParameterException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler(
        SharedKernelPresentationProperties(
            metaDefaults = SharedKernelPresentationProperties.MetaDefaults(version = "v1", environment = "test"),
        ),
    )

    private fun request(path: String = "/api/test", query: String? = null): MockHttpServletRequest =
        MockHttpServletRequest().apply {
            method = "GET"
            requestURI = path
            queryString = query
        }

    @Test
    fun `domain ValidationException is mapped to 400 with structured details`() {
        val errors = listOf(
            ValidationError(field = "name", message = "<b>must not be blank</b>"),
            ValidationError(field = null, message = "some global issue"),
        )
        val ex = ValidationException(errors, message = "Validation failed for request")

        val response = handler.handleDomainValidation(ex, request("/api/partners"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val body = response.body
        assertNotNull(body)
        assertFalse(body.success)

        val error = body.error
        assertNotNull(error)
        assertEquals("VALIDATION_FAILED", error.code)

        val details = error.details
        assertNotNull(details)
        // One field error, one global error and the synthetic totalErrors entry
        assertEquals("2", details["totalErrors"])
        // HTML content from the validation error is preserved as-is (no escaping)
        assertEquals("<b>must not be blank</b>", details["field.name"])
    }

    @Test
    fun `constraint violation exception is mapped to 400 with code and detailed violations`() {
        val violations: Set<ConstraintViolation<*>> = setOf(
            TestConstraintViolation("email", "must be a well-formed email address"),
            TestConstraintViolation("address.postcode", "must not be blank"),
        )
        val ex = ConstraintViolationException("violations", violations)

        val response = handler.handleConstraintViolation(ex, request("/api/partners"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val body = response.body
        assertNotNull(body)

        val error = body.error
        assertNotNull(error)
        assertEquals("CONSTRAINT_VIOLATION", error.code)
        val details = error.details
        assertNotNull(details)
        assertEquals("2", details["totalErrors"])
        assertEquals("must be a well-formed email address", details["field.email"])
        assertEquals("must not be blank", details["field.address.postcode"])
    }

    @Test
    fun `missing request parameter is mapped to 400 with descriptive message and code`() {
        val ex = MissingServletRequestParameterException("id", "UUID")

        val response = handler.handleMissingServletRequestParameter(ex, request("/api/partners"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val body = response.body
        assertNotNull(body)

        val error = body.error
        assertNotNull(error)
        assertEquals("MISSING_REQUEST_PARAMETER", error.code)
        // Message is propagated as-is without HTML escaping
        assertEquals("Missing required request parameter 'id'", error.message)

        val details = error.details
        assertNotNull(details)
        assertEquals("id", details["parameterName"])
        assertEquals("UUID", details["parameterType"])
    }

    @Test
    fun `illegal argument message and details are propagated without html escaping and metadata is populated`() {
        val ex = IllegalArgumentException("<script>alert('x')</script>")
        val req = request(path = "/api/<script>bad</script>", query = "q=<b>test</b>").apply {
            addHeader("X-Correlation-Id", "corr-456")
            setAttribute(SharedKernelRequestTimingFilter.REQUEST_START_ATTRIBUTE, Clock.System.now())
        }

        val response = handler.handleIllegalArgumentException(ex, req)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val body = response.body
        assertNotNull(body)

        val error = body.error
        assertNotNull(error)
        // Main error message is prefixed but otherwise unchanged
        assertEquals(
            "Invalid request: <script>alert('x')</script>",
            error.message,
        )

        val details = error.details
        assertNotNull(details)
        // Underlying exception message echoed in details is also unchanged
        assertEquals("<script>alert('x')</script>", details["error"])

        // Request metadata is preserved as-is and enriched
        val meta = body.meta
        assertEquals("/api/<script>bad</script>", meta.request.path)
        assertEquals("q=<b>test</b>", meta.request.query)
        assertEquals("corr-456", meta.request.correlationId)

        assertEquals(400, meta.response.status)
        assertEquals(400, meta.response.statusCode)
        assertEquals("Bad Request", meta.response.reason)
        assertNotNull(meta.response.timestamp)
        assertNotNull(meta.response.processingTimeMs)

        assertEquals("v1", meta.version)
        assertEquals("test", meta.environment)
    }
}

    private data class TestConstraintViolation(
        private val property: String,
        private val messageText: String,
    ) : ConstraintViolation<Any> {

        override fun getMessage(): String = messageText

        override fun getMessageTemplate(): String = messageText

        override fun getRootBean(): Any? = null

        @Suppress("UNCHECKED_CAST")
        override fun getRootBeanClass(): Class<Any> = Any::class.java

        override fun getLeafBean(): Any? = null

        override fun getExecutableParameters(): Array<Any?>? = null

        override fun getExecutableReturnValue(): Any? = null

        override fun getPropertyPath(): Path = object : Path {
            override fun iterator(): MutableIterator<Path.Node> = mutableListOf<Path.Node>().iterator()

            override fun toString(): String = property
        }

        override fun getInvalidValue(): Any? = null

        override fun getConstraintDescriptor(): ConstraintDescriptor<*> =
            throw UnsupportedOperationException("Not needed for tests")

        override fun <U> unwrap(type: Class<U>): U =
            throw UnsupportedOperationException("Not needed for tests")
    }
