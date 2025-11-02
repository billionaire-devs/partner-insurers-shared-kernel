package com.bamboo.assur.partnerinsurers.sharedkernel.domain.utils


/**
 * Returns the simple name of the aggregate root class, or an empty string if the name is null.
 *
 * @param T The type parameter representing the aggregate root class.
 * @return The simple name of the aggregate root class, or an empty string if the name is null.
 */
inline fun <reified T> getAggregateTypeOrEmpty(): String {
    return T::class.simpleName.orEmpty()
}
