package com.bamboo.assur.partnerinsurers.sharedkernel.application

/**
 * Marker interface for all queries in the domain-driven architecture.
 * 
 * Queries represent requests for information from the system.
 * They are read-only operations that do not modify the state of the system.
 * Queries are typically processed by query handlers that retrieve and return data.
 * 
 * In the context of CQRS (Command Query Responsibility Segregation),
 * queries are used to read state, while commands are used to modify state.
 * This separation allows for optimized read and write models.
 * 
 * Queries should be immutable and contain all the information needed
 * to retrieve the requested data, such as filters, sorting criteria,
 * and pagination parameters.
 * 
 * Example implementations might include:
 * - GetPartnerInsurerByIdQuery
 * - GetAllPartnerInsurersQuery
 * - SearchPartnerInsurersQuery
 * - GetPartnerInsurersByStatusQuery
 */
interface Query
