package com.bamboo.assur.partnerinsurers.sharedkernel.infrastructure.serialization

import com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects.DomainEntityId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.uuid.ExperimentalUuidApi

/**
 * Serializer for DomainEntityId value class.
 */
@OptIn(ExperimentalUuidApi::class)
object DomainEntityIdSerializer : KSerializer<DomainEntityId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "DomainEntityId",
        PrimitiveKind.STRING
    )
    override fun serialize(encoder: Encoder, value: DomainEntityId) = encoder.encodeString(value.value.toString())
    override fun deserialize(decoder: Decoder): DomainEntityId = DomainEntityId.fromString(decoder.decodeString())
}