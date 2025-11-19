package com.bamboo.assur.partnerinsurers.sharedkernel.domain

/**
 * Describes a single validation failure.
 *
 * This type can be used both at the domain layer and by the presentation layer
 * when aggregating validation problems into a structured error response.
 *
 * @property field Optional name of the field or property that failed validation.
 * @property message Human-readable description of the validation problem.
 */
data class ValidationError(
    val field: String?,
    val message: String,
)

