package com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val street: String,
    val city: String,
    val country: String,
    val zipCode: String?,
) {
    init {
        require(street.isNotBlank()) { "Address street cannot be blank" }
        require(city.isNotBlank()) { "Address city cannot be blank" }
        require(country.isNotBlank()) { "Address country cannot be blank" }
    }
}