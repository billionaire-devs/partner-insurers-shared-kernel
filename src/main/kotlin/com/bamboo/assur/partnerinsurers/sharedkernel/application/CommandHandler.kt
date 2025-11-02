package com.bamboo.assur.partnerinsurers.sharedkernel.application

/**
 * Interface for handling commands in the CQRS architecture.
 * 
 * Command handlers are responsible for processing specific commands and executing
 * the corresponding business logic. Each command handler should handle exactly one
 * type of command, following the Single Responsibility Principle.
 * 
 * The handler receives a command of type C and produces a result of type R.
 * The result type can be Unit for commands that don't return a value,
 * or a specific type for commands that need to return data (like an ID of a created entity).
 * 
 * @param C The type of command this handler processes, must extend Command
 * @param R The type of result this handler returns
 * 
 * Example implementations:
 * - CreatePartnerInsurerCommandHandler : CommandHandler<CreatePartnerInsurerCommand, PartnerId>
 * - UpdatePartnerInsurerCommandHandler : CommandHandler<UpdatePartnerInsurerCommand, Unit>
 * - DeletePartnerInsurerCommandHandler : CommandHandler<DeletePartnerInsurerCommand, Unit>
 */
interface CommandHandler<in C : Command, out R> {
    /**
     * Handles the given command and returns the result.
     * 
     * This method contains the core business logic for processing the command.
     * It should validate the command, perform the necessary operations,
     * and return the appropriate result.
     * 
     * @param command The command to be handled
     * @return The result of processing the command
     * @throws Exception if the command cannot be processed or validation fails
     */
    suspend operator fun invoke(command: C): R
}
