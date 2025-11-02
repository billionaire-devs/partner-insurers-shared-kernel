package com.bamboo.assur.partnerinsurers.sharedkernel.domain

/**
 * Represents the result of an operation that can either succeed or fail.
 * 
 * This pattern helps avoid throwing exceptions for expected failures
 * and makes error handling more explicit and functional.
 * 
 * @param T The type of the success value
 */
sealed class Result<out T> {
    
    /**
     * Represents a successful result with a value.
     */
    data class Success<out T>(val value: T) : Result<T>()
    
    /**
     * Represents a failed result with an error message and optional cause.
     */
    data class Failure(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    
    /**
     * Returns true if the result is successful.
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * Returns true if the result is a failure.
     */
    fun isFailure(): Boolean = this is Failure
    
    /**
     * Returns the value if successful, null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }
    
    /**
     * Returns the value if successful, or the default value if failed.
     */
    fun getOrElse(default: @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> default
    }
    
    /**
     * Transforms the value if successful.
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }
    
    /**
     * Flat maps the result if successful.
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }
    
    companion object {
        /**
         * Creates a successful result.
         */
        fun <T> success(value: T): Result<T> = Success(value)
        
        /**
         * Creates a failed result.
         */
        fun <T> failure(message: String, cause: Throwable? = null): Result<T> = Failure(message, cause)
        
        /**
         * Wraps a potentially throwing operation in a Result.
         */
        inline fun <T> of(operation: () -> T): Result<T> = try {
            success(operation())
        } catch (e: Exception) {
            failure(e.message ?: "Unknown error", e)
        }
    }
}
