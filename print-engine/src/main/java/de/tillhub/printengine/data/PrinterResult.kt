package de.tillhub.printengine.data

/**
 * Object for passing values as well as errors around.
 */
sealed class PrinterResult<out T> {
    data class Success<out T>(val value: T) : PrinterResult<T>()
    sealed class Error : PrinterResult<Nothing>() {
        data object PrinterNotConnected : Error()
        data class WithException(val error: Throwable) : Error()
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