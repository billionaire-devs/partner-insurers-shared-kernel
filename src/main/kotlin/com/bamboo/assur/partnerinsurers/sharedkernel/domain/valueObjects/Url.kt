package com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects

import kotlinx.serialization.Serializable
import java.net.URI

@JvmInline
@Serializable
value class Url(val value: String) {
    init {
        require(value.isNotBlank()) { "URL cannot be blank." }
        require(isValidUrl(value)) { "Invalid URL format." }
    }

    private fun isValidUrl(url: String) = try {
        URI.create(url).toURL()
        true
    } catch (_: Exception) {
        false
    }
}