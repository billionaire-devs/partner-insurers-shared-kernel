package com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects

import kotlinx.serialization.Serializable

/**
 * Value object representing an international phone number.
 *
 * The phone number is stored as-is after passing a simple validation rule.
 * Services with stricter requirements can re-validate the value downstream.
 */
@Serializable
@JvmInline
value class Phone(val value: String) {
    init {
        require(value.isNotBlank()) { "Phone cannot be blank" }
        require(value.matches(PHONE_REGEX.toRegex())) { "Invalid phone number format" }
    }

    companion object {
        /**
         * Basic E.164-like regex accepting optional leading `+` and 10-15 digits.
         */
        const val PHONE_REGEX = "^\\+?[0-9]{10,15}$"
    }
}