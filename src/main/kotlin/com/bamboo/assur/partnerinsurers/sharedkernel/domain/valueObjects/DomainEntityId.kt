package com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects

import kotlinx.serialization.Contextual
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import java.util.UUID

@OptIn(ExperimentalUuidApi::class)
@Serializable
@JvmInline
value class DomainEntityId(@Contextual val value: UUID) {
    companion object {
        fun random(): DomainEntityId = DomainEntityId(UUID.randomUUID())

        fun fromString(uuidString: String): DomainEntityId {
            require(uuidString.isNotEmpty()) { "UUID string cannot be empty" }
            return DomainEntityId(UUID.fromString(uuidString))
        }
    }
}