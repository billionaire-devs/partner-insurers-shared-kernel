package com.bamboo.assur.partnerinsurers.sharedkernel.application

/**
 * Marker interface for all commands in the domain-driven architecture.
 * 
 * Commands represent intentions to change the state of the system.
 * They are imperative in nature and describe what should be done.
 * Commands are typically processed by command handlers that contain
 * the business logic to execute the requested operation.
 * 
 * In the context of CQRS (Command Query Responsibility Segregation),
 * commands are used to modify state, while queries are used to read state.
 * 
 * Example implementations might include:
 * - CreatePartnerInsurerCommand
 * - UpdatePartnerInsurerCommand
 * - DeletePartnerInsurerCommand
 */
interface Command

