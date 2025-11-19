package com.bamboo.assur.partnerinsurers.sharedkernel.presentation

import kotlinx.datetime.Clock
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApiResponseBodyAdviceTest {

    private fun createAdvice(enabled: Boolean = true): ApiResponseBodyAdvice {
        val servletRequest = MockHttpServletRequest().apply {
            method = "GET"
            requestURI = "/api/test"
        }
        val properties = SharedKernelPresentationProperties(
            apiResponse = SharedKernelPresentationProperties.ApiResponse(enabled = enabled),
            metaDefaults = SharedKernelPresentationProperties.MetaDefaults(version = "v1", environment = "test"),
        )
        return ApiResponseBodyAdvice(servletRequest, properties)
    }

    private fun dummyMethodParameter(): MethodParameter {
        class TestController {
            @Suppress("unused")
            fun endpoint(): String = "ok"
        }

        val method = TestController::class.java.getDeclaredMethod("endpoint")
        return MethodParameter(method, -1)
    }

    @Test
    fun `wraps json responses when enabled and populates metadata`() {
        val advice = createAdvice(enabled = true)
        val methodParameter = dummyMethodParameter()
        val servletRequest = MockHttpServletRequest("GET", "/api/test").apply {
            addHeader("X-Correlation-Id", "corr-123")
            setAttribute(SharedKernelRequestTimingFilter.REQUEST_START_ATTRIBUTE, Clock.System.now())
        }
        val servletResponse = MockHttpServletResponse()
        val request = ServletServerHttpRequest(servletRequest)
        val response = ServletServerHttpResponse(servletResponse)

        val result = advice.beforeBodyWrite(
            "payload",
            methodParameter,
            MediaType.APPLICATION_JSON,
            StringHttpMessageConverter::class.java,
            request,
            response,
        )

        assertTrue(result is ApiResponse<*>)
        val apiResponse = result as ApiResponse<*>
        assertTrue(apiResponse.success)
        assertEquals("payload", apiResponse.data)

        val meta = apiResponse.meta
        assertEquals("GET", meta.request.method)
        assertEquals("/api/test", meta.request.path)
        assertEquals(null, meta.request.query)
        assertEquals("corr-123", meta.request.correlationId)

        assertEquals(200, meta.response.status)
        assertEquals(200, meta.response.statusCode)
        assertEquals("OK", meta.response.reason)
        assertNotNull(meta.response.timestamp)
        assertNotNull(meta.response.processingTimeMs)

        assertEquals("v1", meta.version)
        assertEquals("test", meta.environment)
    }

    @Test
    fun `does not wrap non json responses`() {
        val advice = createAdvice(enabled = true)
        val methodParameter = dummyMethodParameter()
        val servletRequest = MockHttpServletRequest("GET", "/api/test")
        val servletResponse = MockHttpServletResponse()
        val request = ServletServerHttpRequest(servletRequest)
        val response = ServletServerHttpResponse(servletResponse)

        val payload = "raw-bytes"
        val result = advice.beforeBodyWrite(
            payload,
            methodParameter,
            MediaType.TEXT_PLAIN,
            StringHttpMessageConverter::class.java,
            request,
            response,
        )

        assertEquals(payload, result)
    }

    @Test
    fun `does not wrap when disabled`() {
        val advice = createAdvice(enabled = false)
        val methodParameter = dummyMethodParameter()
        val servletRequest = MockHttpServletRequest("GET", "/api/test")
        val servletResponse = MockHttpServletResponse()
        val request = ServletServerHttpRequest(servletRequest)
        val response = ServletServerHttpResponse(servletResponse)

        val payload = "payload"
        val result = advice.beforeBodyWrite(
            payload,
            methodParameter,
            MediaType.APPLICATION_JSON,
            StringHttpMessageConverter::class.java,
            request,
            response,
        )

        assertEquals(payload, result)
    }

    @Test
    fun `supports reflects configuration property`() {
        val enabledAdvice = createAdvice(enabled = true)
        val disabledAdvice = createAdvice(enabled = false)
        val methodParameter = dummyMethodParameter()

        assertTrue(enabledAdvice.supports(methodParameter, StringHttpMessageConverter::class.java))
        assertFalse(disabledAdvice.supports(methodParameter, StringHttpMessageConverter::class.java))
    }
}

