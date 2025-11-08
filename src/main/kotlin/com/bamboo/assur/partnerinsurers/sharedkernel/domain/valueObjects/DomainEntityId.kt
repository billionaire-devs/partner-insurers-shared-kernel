package com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import java.util.UUID

/**
 * Value object wrapping a `UUID` identifier used across aggregates and entities.
 *
 * Using a dedicated type prevents accidental mix-ups between unrelated identifiers
 * and provides helper factories for consistent creation/parsing.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
@JvmInline
value class DomainEntityId(@Contextual val value: UUID) {
    companion object {
        /**
         * Generates a new random identifier using `UUID.randomUUID()`.
         */
        fun random(): DomainEntityId = DomainEntityId(UUID.randomUUID())

        /**
         * Creates a domain identifier from its string representation.
         *
         * @throws IllegalArgumentException when `uuidString` is blank or invalid.
         */
        fun fromString(uuidString: String): DomainEntityId {
            require(uuidString.isNotEmpty()) { "UUID string cannot be empty" }
            return DomainEntityId(UUID.fromString(uuidString))
        }
    }
}