package com.bamboo.assur.partnerinsurers.sharedkernel.domain

import com.bamboo.assur.partnerinsurers.sharedkernel.domain.valueObjects.DomainEntityId
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents a base model in a domain-driven design context.
 *
 * This abstract class serves as the foundational entity model for the domain.
 * It provides a unique identifier, timestamps for creation and updates, and
 * soft delete capabilities, ensuring that all domain models maintain
 * consistency in these aspects.
 *
 * @param ID The type of the unique identifier for the model.
 * @property id The unique identifier of the model.
 * @property createdAt The timestamp indicating when the model was created,
 *                     defaulting to the current system time.
 * @property updatedAt The timestamp indicating when the model was last updated,
 *                     defaulting to the creation time.
 * @property deletedAt The timestamp indicating when the model was soft deleted,
 *                     null if the model is not deleted.
 * @property deletedBy The identifier of the user who soft deleted the model,
 *                     null if the model is not deleted.
 */
@OptIn(ExperimentalTime::class)
abstract class Model(
    val id: DomainEntityId,
    val createdAt: Instant = Clock.System.now(),
    var updatedAt: Instant = createdAt,
    var deletedAt: Instant? = null,
    var deletedBy: DomainEntityId? = null,
) {
    
    /**
     * Updates the updatedAt timestamp to the current time.
     * Should be called whenever the model state changes.
     */
    protected fun touch() {
        updatedAt = Clock.System.now()
    }
    
    /**
     * Soft deletes the model by setting deletedAt to current time and deletedBy to the specified user.
     *
     * @param userId The identifier of the user performing the delete operation.
     */
    protected fun softDelete(userId: DomainEntityId) {
        deletedAt = Clock.System.now()
        deletedBy = userId
        touch()
    }

    /**
     * Restores a soft-deleted model by clearing deletedAt and deletedBy.
     */
    protected fun restore() {
        deletedAt = null
        deletedBy = null
        touch()
    }

    /**
     * Checks if the model is soft deleted.
     *
     * @return true if the model is soft deleted, false otherwise.
     */
    fun isDeleted(): Boolean = deletedAt != null

    /**
     * Checks if the model is not soft deleted.
     *
     * @return true if the model is not soft deleted, false otherwise.
     */
    fun isNotDeleted(): Boolean = deletedAt == null

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