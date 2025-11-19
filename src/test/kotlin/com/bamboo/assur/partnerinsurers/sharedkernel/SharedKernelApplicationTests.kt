package com.bamboo.assur.partnerinsurers.sharedkernel

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import com.bamboo.assur.partnerinsurers.sharedkernel.presentation.ApiResponseBodyAdvice
import com.bamboo.assur.partnerinsurers.sharedkernel.presentation.GlobalExceptionHandler
import com.bamboo.assur.partnerinsurers.sharedkernel.presentation.SharedKernelPresentationProperties

@SpringBootTest
class SharedKernelApplicationTests {

    @Autowired
    lateinit var apiResponseBodyAdvice: ApiResponseBodyAdvice

    @Autowired
    lateinit var globalExceptionHandler: GlobalExceptionHandler

    @Autowired
    lateinit var presentationProperties: SharedKernelPresentationProperties

    @Test
    fun contextLoads() {
        // Basic sanity check that the Spring Boot application context starts.
    }

    @Test
    fun `auto configuration registers presentation beans`() {
        assertNotNull(apiResponseBodyAdvice)
        assertNotNull(globalExceptionHandler)
        assertNotNull(presentationProperties)
        assertTrue(presentationProperties.apiResponse.enabled)
    }
}
