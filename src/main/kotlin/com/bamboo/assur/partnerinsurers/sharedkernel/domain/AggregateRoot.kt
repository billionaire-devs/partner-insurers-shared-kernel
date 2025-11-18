package com.bamboo.assur.partnerinsurers.sharedkernel.domain

import com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects.DomainEntityId
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// Note: Do not cache "now" at class-load time. Each instance should capture its own timestamps.

/**
 * Aggregate root represents the central entity in a domain-driven design context
 * that serves as an entry point to a specific aggregate. It extends the `Model` class
 * and provides additional functionality for managing domain events.
 *
 * An aggregate root encapsulates domain logic, ensures consistency within the aggregate,
 * and manages the aggregation lifecycle. It is responsible for modeling the boundary
 * of a transaction, ensuring that changes to the aggregate occur in a controlled manner.
 *
 * This class includes functionality for tracking and managing domain events, which are
 * used to represent meaningful changes or actions occurring within the bounded context.
 *
 * @constructor Creates a new aggregate root instance.
 * @param id The unique identifier for the aggregate root.
 * @param createdAt The timestamp indicating when the aggregate was created. Defaults to the current system time.
 * @param updatedAt The timestamp indicating when the aggregate was last updated. Defaults to `createdAt`.
 * @param deletedAt The optional timestamp indicating when the aggregate was soft deleted. Defaults to null.
 * @param deletedBy The identifier of the user who soft deleted the aggregate. Defaults to null.
 */
@OptIn(ExperimentalTime::class)
abstract class AggregateRoot(
    id: DomainEntityId,
    createdAt: Instant = Clock.System.now(),
    updatedAt: Instant = createdAt,
    deletedAt: Instant? = null,
    deletedBy: DomainEntityId? = null,
): Model(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    deletedBy = deletedBy
) {
    /**
     * Holds the list of domain events associated with this aggregate root.
     *
     * Domain events represent significant occurrences within the domain
     * that might affect the state of the aggregate or require further
     * processing by the system.
     *
     * This list is used to temporarily store domain events until they are
     * processed or persisted. The list can be manipulated through methods
     * like `addDomainEvent`, `removeDomainEvent`, `clearDomainEvents`, and
     * accessed in a read-only manner using `getDomainEvents`.
     *
     * Ensures unique domain events are added by checking their `eventId`.
     */
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    /**
     * Adds a unique domain event to the collection of domain events.
     *
     * This method ensures that a domain event is only added if it is not
     * already present in the collection, based on its unique `eventId`.
     * Domain events capture changes or significant occurrences in the
     * state of the associated aggregate root.
     *
     * @param event The domain event to be added. It encapsulates metadata
     *              such as `eventId`, `aggregateId`, `aggregateType`, and
     *              `occurredOn` to describe the event and its linkage to
     *              the aggregate root.
     */
    protected fun addDomainEvent(event: DomainEvent) {
        if (domainEvents.none { it.eventId.value == event.eventId.value }) {
            domainEvents.add(event)
        }
    }

    /**
     * Retrieves the list of recorded domain events.
     *
     * This method returns an immutable copy of the currently stored domain events.
     * Domain events represent significant changes or actions that have occurred
     * in the domain model, and they are typically used for event-driven or
     * external communication between bounded contexts.
     *
     * @return A list containing the current domain events.
     */
    fun getDomainEvents(): List<DomainEvent> = domainEvents.toList()

    /**
     * Checks if there are any pending domain events for the aggregate root.
     *
     * @return true if there are pending events, false otherwise
     */
    fun hasPendingEvents(): Boolean = domainEvents.isNotEmpty()

    /**
     * Clears all pending domain events associated with the aggregate root.
     *
     * This method is typically used to reset the state of domain events
     * after they have been processed or dispatched.
     *
     * After calling this method, the list of domain events will be empty,
     * and {@link hasPendingEvents} will return false.
     */
    fun clearDomainEvents() {
        domainEvents.clear()
    }

    /**
     * Removes a domain event from the list of domain events associated with the aggregate root.
     *
     * @param event The domain event to be removed.
     */
    protected fun removeDomainEvent(event: DomainEvent) {
        domainEvents.remove(event)
    }
}
