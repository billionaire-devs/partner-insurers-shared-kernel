package com.bamboo.assur.partnerinsurers.sharedkernel.domain

import com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects.DomainEntityId
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

/**
 * Represents a domain event in a domain-driven design context.
 *
 * A domain event encapsulates a significant occurrence or change in the state
 * of a domain model. It typically includes metadata to describe the event
 * and support tracking, such as a unique identifier and timestamp.
 *
 * @param eventId A unique identifier for the domain event, defaulting to a randomly generated UUID.
 * @param aggregateId The identifier of the aggregate root instance associated with the event.
 * @param aggregateType The type of aggregate root that the event is linked to.
 * @param eventType A descriptive type or name for the event; used to categorize and identify the event.
 * @param occurredOn The timestamp indicating when the event occurred, defaulting to the current system time.
 */
@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
@Serializable
abstract class DomainEvent(
    @Contextual
    val eventId: DomainEntityId = DomainEntityId(UUID.randomUUID()),
    @Contextual
    val aggregateId: DomainEntityId,
    val aggregateType: String,
    val eventType: String,
    @Contextual
    val occurredOn: Instant = Clock.System.now(),
) {
    companion object {
        /**
         * Returns the simple name of the domain event class, removing the "Event" suffix,
         * or a default value if the name is null.
         *
         * @param T The type parameter representing the domain event class.
         * @param default The default string value to return if the class name is null.
         * @return The simple name of the domain event class, or the default value if the name is null.
         */
        inline fun <reified T: DomainEvent> getEventTypeNameOrDefault(default: String = "Event"): String {
            return T::class.simpleName?.removeSuffix("Event") ?: default
        }
    }
}
