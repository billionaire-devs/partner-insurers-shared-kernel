package com.bamboo.assur.partnerinsurers.sharedkernel.domain

/**
 * Marker interface for domain services.
 *
 * Domain services encapsulate domain logic that doesn't naturally fit within
 * a single aggregate root or entity. They are stateless and contain domain logic
 * that involves multiple aggregates or requires external services.
 *
 * Domain services should:
 * - Be stateless
 * - Contain domain logic that doesn't belong to a single aggregate
 * - Coordinate between multiple aggregates
 * - Implement complex business rules that span multiple entities
 *
 * Examples:
 * - PolicyCalculationService
 * - RiskAssessmentService
 * - PremiumCalculationService
 */
interface DomainService