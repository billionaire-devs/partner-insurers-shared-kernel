package com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects

import kotlinx.serialization.Serializable

/**
 * Immutable representation of a postal address in the domain.
 *
 * The value object validates the minimum set of attributes required to identify
 * a location. Optional elements (such as `zipCode`) are preserved as-is to allow
 * downstream services to apply their own validation rules.
 */
@Serializable
data class Address(
    /**
     * Street line (e.g. number, street name).
     */
    val street: String,
    /**
     * City or locality component of the address.
     */
    val city: String,
    /**
     * Country name in human-readable form.
     */
    val country: String,
    /**
     * Optional postal or ZIP code when available.
     */
    val zipCode: String?,
) {
    init {
        require(street.isNotBlank()) { "Address street cannot be blank" }
        require(city.isNotBlank()) { "Address city cannot be blank" }
        require(country.isNotBlank()) { "Address country cannot be blank" }
    }
}