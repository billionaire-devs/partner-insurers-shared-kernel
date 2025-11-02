package com.bamboo.assur.partnerinsurers.sharedkernel.infrastructure.serialization

import com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects.Url
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
object UrlSerializer : KSerializer<Url> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "Url",
        PrimitiveKind.STRING
    )
    override fun serialize(encoder: Encoder, value: Url) = encoder.encodeString(value.value)
    override fun deserialize(decoder: Decoder): Url = Url(decoder.decodeString())
}