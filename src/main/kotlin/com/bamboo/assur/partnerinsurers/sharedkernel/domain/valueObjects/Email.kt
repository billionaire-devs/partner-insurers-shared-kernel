package com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects

import kotlinx.serialization.Serializable

/**
 * Value object representing an email address.
 *
 * The constructor enforces a lightweight validation using a conservative
 * regex to ensure the value looks like a standard email address before it
 * propagates deeper into the domain.
 */
@Serializable
@JvmInline
value class Email(val value: String) {

    init {
        require(value.isNotBlank()) { "Email cannot be blank" }
        require(value.matches(EMAIL_REGEX)) {
            "Invalid email format"
        }
    }

    companion object {
        /** Simple pattern matching the `local@domain.tld` email format. */
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    }
}