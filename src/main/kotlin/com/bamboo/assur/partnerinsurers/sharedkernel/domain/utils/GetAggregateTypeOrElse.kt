package com.bamboo.assur.partnerinsurers.sharedkernel.domain.utils


/**
 * Returns the simple name of the class, or an empty string if the name is null.
 *
 * @param T The type parameter representing the class.
 * @return The simple name of the class, or an empty string if the name is null.
 * @deprecated Use getClassNameOrEmpty() instead.
 */
@Deprecated(
    "Use getClassNameOrEmpty() instead.",
    replaceWith = ReplaceWith("getClassNameOrEmpty<T>()"),
    level = DeprecationLevel.WARNING
)
inline fun <reified T> getAggregateTypeOrEmpty(): String {
    return T::class.simpleName.orEmpty()
}

/**
 * Returns the simple name of the class, or an empty string if the name is null.
 *
 * @param T The type parameter representing the class.
 * @return The simple name of the class, or an empty string if the name is null.
 */
inline fun <reified T> getClassNameOrEmpty(): String {
    return T::class.simpleName.orEmpty()
}