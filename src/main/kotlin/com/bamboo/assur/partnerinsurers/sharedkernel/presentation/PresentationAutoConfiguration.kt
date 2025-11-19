package com.bamboo.assur.partnerinsurers.sharedkernel.presentation

import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Spring Boot auto-configuration that registers shared presentation components
 * (response envelope advice and global exception handler) when Spring MVC is present.
 *
 * This ensures the beans are created even if consumer services do not component-scan this library.
 */
@AutoConfiguration
@ConditionalOnClass(HttpServletRequest::class)
@EnableConfigurationProperties(SharedKernelPresentationProperties::class)
class PresentationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun apiResponseBodyAdvice(
        servletRequest: HttpServletRequest,
        sharedKernelPresentationProperties: SharedKernelPresentationProperties,
    ): ApiResponseBodyAdvice =
        ApiResponseBodyAdvice(servletRequest, sharedKernelPresentationProperties)

    @Bean
    @ConditionalOnMissingBean
    fun globalExceptionHandler(
        sharedKernelPresentationProperties: SharedKernelPresentationProperties,
    ): GlobalExceptionHandler = GlobalExceptionHandler(sharedKernelPresentationProperties)

    @Bean
    @ConditionalOnMissingBean
    fun sharedKernelRequestTimingFilter(): SharedKernelRequestTimingFilter = SharedKernelRequestTimingFilter()
}
