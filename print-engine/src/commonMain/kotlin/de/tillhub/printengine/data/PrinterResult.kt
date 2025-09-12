package de.tillhub.printengine.data

import de.tillhub.printengine.helpers.HashHelper

/**
 * Object for passing values as well as errors around.
 */
sealed class PrinterResult<out T> {
    class Success<out T>(val value: T) : PrinterResult<T>() {
        override fun toString() = "PrinterResult.Success(" +
                "value=$value" +
                ")"

        override fun equals(other: Any?) = other is Success<*> &&
                value == other.value

        override fun hashCode() = HashHelper.hash(value)
    }
    sealed class Error : PrinterResult<Nothing>() {
        data object PrinterNotConnected : Error()
        class WithException(val error: Throwable) : Error() {
            override fun toString() = "PrinterResult.Error.WithException(" +
                    "error=$error" +
                    ")"

            override fun equals(other: Any?) = other is WithException &&
                    error == other.error

            override fun hashCode() = HashHelper.hash(error)
        }
    }
}

inline fun <T> PrinterResult<T>.doOnError(body: (PrinterResult.Error) -> Unit): PrinterResult<T> = apply {
    if (this is PrinterResult.Error) body(this)
}

inline fun <R, T> PrinterResult<T>.fold(
    onSuccess: (value: T) -> R,
    onFailure: (exception: Throwable?) -> R
): R {
    return when (this) {
        is PrinterResult.Success -> onSuccess(value)
        is PrinterResult.Error.WithException -> onFailure(error)
        is PrinterResult.Error -> onFailure(null)
    }
}
