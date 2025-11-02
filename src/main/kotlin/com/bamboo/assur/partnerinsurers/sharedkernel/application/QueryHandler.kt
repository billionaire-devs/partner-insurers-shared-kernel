package com.bamboo.assur.partnerinsurers.sharedkernel.application

/**
 * Interface for handling queries in the CQRS architecture.
 * 
 * Query handlers are responsible for processing specific queries and retrieving
 * the requested data. Each query handler should handle exactly one type of query,
 * following the Single Responsibility Principle.
 * 
 * The handler receives a query of type Q and produces a result of type R.
 * Query handlers should be optimized for read operations and can use
 * different data sources or projections than the command side.
 * 
 * @param Q The type of query this handler processes, must extend Query
 * @param R The type of result this handler returns
 * 
 * Example implementations:
 * - GetPartnerInsurerByIdQueryHandler : QueryHandler<GetPartnerInsurerByIdQuery, PartnerInsurerDto?>
 * - GetAllPartnerInsurersQueryHandler : QueryHandler<GetAllPartnerInsurersQuery, List<PartnerInsurerDto>>
 * - SearchPartnerInsurersQueryHandler : QueryHandler<SearchPartnerInsurersQuery, PagedResult<PartnerInsurerDto>>
 */
interface QueryHandler<in Q : Query, out R> {
    /**
     * Handles the given query and returns the result.
     * 
     * This method contains the logic for retrieving the requested data.
     * It should process the query parameters, fetch the data from the appropriate
     * data source, and return the result in the expected format.
     * 
     * Query handlers should be optimized for read performance and can use
     * denormalized views, caching, or specialized read databases.
     * 
     * @param query The query to be handled
     * @return The result of processing the query
     * @throws Exception if the query cannot be processed or data cannot be retrieved
     */
    suspend operator fun invoke(query: Q): R
}
