package com.bamboo.assur.partnerinsurers.sharedkernel.presentation

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for shared presentation components.
 *
 * These properties allow consumer services to tune the behaviour of the
 * presentation utilities (such as the automatic API response envelope).
 *
 * Configuration prefix: `sharedkernel.presentation`.
 */
@ConfigurationProperties(prefix = "sharedkernel.presentation")
data class SharedKernelPresentationProperties(
    /**
     * Configuration related to the automatic API response envelope wrapping.
     */
    val apiResponse: ApiResponse = ApiResponse(),
    /**
     * Optional default metadata values that will be included in the [Meta]
     * section of every [ApiResponse].
     */
    val metaDefaults: MetaDefaults = MetaDefaults(),
) {

    /**
     * API response envelope related configuration.
     */
    data class ApiResponse(
        /**
         * Enables or disables the automatic wrapping of controller responses
         * into the [ApiResponse] envelope.
         *
         * Property: `sharedkernel.presentation.api-response.enabled`.
         * Default: `true`.
         */
        val enabled: Boolean = true,
    )

    /**
     * Default metadata values to be propagated into [Meta].
     *
     * These are useful for adding observability information such as logical
     * API version or deployment environment labels.
     */
    data class MetaDefaults(
        /**
         * Optional logical API version string (for example `v1`, `2024-11-19`).
         *
         * Property: `sharedkernel.presentation.meta-defaults.version`.
         */
        val version: String? = null,
        /**
         * Optional environment label (for example `dev`, `staging`, `prod`).
         *
         * Property: `sharedkernel.presentation.meta-defaults.environment`.
         */
        val environment: String? = null,
    )
}

