package com.bamboo.assur.partnerinsurers.sharedkernel.infrastructure

import com.bamboo.assur.partnerinsurers.sharedkernel.domain.DomainEvent

/**
 * Contract for publishing domain events to the outbox.
 */
interface IEventPublisher {
    /**
     * Publishes multiple events to the outbox.
     */
    suspend fun publish(events: List<DomainEvent>)

    /**
     * Publishes a single event to the outbox.
     *
     * @throws EventPublishingException when serialization or persistence fails.
     */
    suspend fun publish(event: DomainEvent)
}

/**
 * Exception thrown when event publishing fails.
 */
class EventPublishingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
