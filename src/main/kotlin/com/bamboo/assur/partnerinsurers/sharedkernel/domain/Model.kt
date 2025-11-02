package com.bamboo.assur.partnerinsurers.sharedkernel.domain

import com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects.DomainEntityId
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents a base model in a domain-driven design context.
 *
 * This abstract class serves as the foundational entity model for the domain.
 * It provides a unique identifier and a timestamp of creation, ensuring
 * that all domain models maintain consistency in these aspects.
 *
 * @param ID The type of the unique identifier for the model.
 * @property id The unique identifier of the model.
 * @property createdAt The timestamp indicating when the model was created,
 *                     defaulting to the current system time.
 * @property updatedAt The timestamp indicating when the model was last updated,
 *                     defaulting to the creation time.
 */
@OptIn(ExperimentalTime::class)
abstract class Model(
    val id: DomainEntityId,
    val createdAt: Instant = Clock.System.now(),
    var updatedAt: Instant = createdAt
) {
    
    /**
     * Updates the updatedAt timestamp to the current time.
     * Should be called whenever the model state changes.
     */
    protected fun touch() {
        updatedAt = Clock.System.now()
    }
    
    /**
     * Equality check based on ID only, following DDD entity semantics.
     * Two entities are considered equal if they have the same ID.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Model) return false
        return id == other.id
    }
    
    /**
     * Hash code based on ID only, consistent with equals implementation.
     */
    override fun hashCode(): Int = id.hashCode()
    
    /**
     * String representation showing the class name and ID.
     */
    override fun toString(): String = "${this::class.simpleName}(id=$id)"
}