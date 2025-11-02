package com.bamboo.assur.partnerinsurers.sharedkernel.domain

import com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects.DomainEntityId
import kotlin.uuid.ExperimentalUuidApi


/**
 * Represents an aggregate root in a domain-driven design context.
 *
 * An aggregate root is the entry point to an aggregate, which is a cluster
 * of related objects that are treated as a single unit for the purpose of
 * data changes. The aggregate root ensures the integrity and consistency
 * of the aggregate, and all interactions with the aggregate go through it.
 *
 * @param ID The type of the identifier for the aggregate root.
 * @property id The unique identifier for this aggregate root.
 */
@OptIn(ExperimentalUuidApi::class)
abstract class AggregateRoot(id: DomainEntityId): Model(id = id) {
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
