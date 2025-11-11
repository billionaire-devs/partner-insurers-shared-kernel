package com.bamboo.assur.partnerinsurers.sharedkernel.application

/**
 * Enum representing different levels of detail for query results.
 *
 * This enum is used to specify the level of detail required for query results,
 * allowing for flexible and efficient data retrieval based on the needs of the application.
 *
 * Example usage:
 * - [SUMMARY]: Provides a high-level overview of the data, suitable for quick reference or display.
 * - [DETAILED]: Provides a more in-depth view of the data, including additional fields and relationships.
 * - [FULL]: Provides the most detailed view of the data, including all available fields and relationships.
 */
enum class QueryView {
    SUMMARY,
    DETAILED,
    FULL,
}