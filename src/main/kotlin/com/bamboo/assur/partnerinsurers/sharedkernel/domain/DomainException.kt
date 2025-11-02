package com.bamboo.assur.partnerinsurers.sharedkernel.domain

/**
 * Base class for all domain-specific exceptions.
 *
 * Domain exceptions represent business rule violations or invalid states
 * within the domain model. They should be used to communicate domain-specific
 * errors that occur during business operations.
 */
abstract class DomainException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when a business rule is violated.
 */
class BusinessRuleViolationException(
    message: String,
    val ruleName: String? = null
) : DomainException(message)

/**
 * Exception thrown when an aggregate or entity is not found.
 */
class EntityNotFoundException(
    entityType: String,
    entityId: Any
) : DomainException("$entityType with ID '$entityId' was not found")

/**
 * Exception thrown when trying to create an entity that already exists.
 */
class EntityAlreadyExistsException(
    entityType: String,
    entityIdentifier: Any,
    entityIdentifierName: String? = null
) : DomainException("$entityType with identifier: ${entityIdentifierName ?: "ID"} '$entityIdentifier' already exists")

/**
 * Exception thrown when an invalid operation is attempted on an entity.
 */
class InvalidOperationException(
    message: String
): DomainException(message)

/**
 * Exception thrown when an entity fails to be saved.
 */
class FailedToSaveEntityException(
    entityType: String,
    entityId: Any
): DomainException("Failed to save $entityType with ID '$entityId'")

/**
 * Exception thrown when an entity fails to be saved.
 */
class FailedToUpdateEntityException(
    entityType: String,
    entityId: Any
): DomainException("Failed to update $entityType with ID '$entityId'")
