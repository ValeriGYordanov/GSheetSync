package com.vlr.gsheetsync.feature.sheets.engine.data

import kotlinx.serialization.json.JsonPrimitive

/**
 * A generic result container that represents three possible states:
 * - Loading (operation in progress)
 * - Success (operation completed with value)
 * - Error (operation failed with exception)
 *
 * This is designed for Kotlin Multiplatform projects where sealed classes might not be ideal,
 * providing clear state management for asynchronous operations.
 *
 * @param T The type of the successful result value
 * @property value The successful result value (null if not successful)
 * @property exception The exception that occurred (null if no error)
 *
 * @sample SSResult.success
 * @sample SSResult.error
 */
class SSResult<out T> constructor(
    private val value: T?,
    private val exception: Throwable?,
    private val action: (() -> T)?
) {
    /**
     * True if the operation completed successfully and we have a value.
     * This will be false when [isError] is true.
     */
    val isSuccess: Boolean get() = value != null

    /**
     * True if the operation failed with an exception.
     * This will be false when [isSuccess] is true.
     */
    val isError: Boolean get() = exception != null

    /**
     * Returns the success value if available, or null otherwise.
     * @return The success value or null if not successful
     */
    fun getOrNull(): T? = if (isSuccess) value else null

    /**
     * Returns the exception if available, or null otherwise.
     * @return The exception or null if no error occurred
     */
    fun exceptionOrNull(): Throwable? = if (isError) exception else null

    companion object {
        /**
         * Creates a success result with the given value.
         * @param value The successful result value
         * @return SSResult in success state containing the value
         */
        fun <T> success(value: T): SSResult<T> = SSResult(value, null, null)

        /**
         * Creates an error result with the given exception.
         * @param exception The exception that caused the failure
         * @return SSResult in error state containing the exception
         */
        fun <T> error(exception: Throwable): SSResult<T> = SSResult(null, exception, null)

        fun <T> action(action: () -> T): T = action.invoke()
    }
}