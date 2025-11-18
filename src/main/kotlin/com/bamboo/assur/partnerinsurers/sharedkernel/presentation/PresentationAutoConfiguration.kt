package com.bamboo.assur.partnerinsurers.sharedkernel.presentation

import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Spring Boot auto-configuration that registers shared presentation components
 * (response envelope advice and global exception handler) when Spring MVC is present.
 *
 * This ensures the beans are created even if consumer services do not component-scan this library.
 */
@AutoConfiguration
@ConditionalOnClass(HttpServletRequest::class)
class PresentationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun apiResponseBodyAdvice(servletRequest: HttpServletRequest): ApiResponseBodyAdvice =
        ApiResponseBodyAdvice(servletRequest)

    @Bean
    @ConditionalOnMissingBean
    fun globalExceptionHandler(): GlobalExceptionHandler = GlobalExceptionHandler()
}
