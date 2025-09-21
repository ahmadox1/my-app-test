package ai.screentalk.common

sealed class AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>()
    data class Error(val throwable: Throwable) : AppResult<Nothing>()

    inline fun <R> fold(onSuccess: (T) -> R, onError: (Throwable) -> R): R = when (this) {
        is Success -> onSuccess(value)
        is Error -> onError(throwable)
    }
}

inline fun <T> runCatchingResult(block: () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (t: Throwable) {
        AppResult.Error(t)
    }
