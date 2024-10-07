package ai.koda.mobile.sdk.core

sealed class CallResponse<T> {
    class Success<T>(val value: T) : CallResponse<T>()
    class Error<T>(val exception: Exception) : CallResponse<T>()
    class Timeout<T> : CallResponse<T>()
}