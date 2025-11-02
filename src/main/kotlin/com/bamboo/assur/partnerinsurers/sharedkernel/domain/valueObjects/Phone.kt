package com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Phone(val value: String) {
    init {
        require(value.isNotBlank()) { "Phone cannot be blank" }
        require(value.matches(PHONE_REGEX.toRegex())) { "Invalid phone number format" }
    }

    companion object { const val PHONE_REGEX = "^\\+?[0-9]{10,15}$" }
} 