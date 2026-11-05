package ai.koda.mobile.core_shared.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

// Explicit platform engines avoid ServiceLoader disk reads during SDK initialization.
internal expect fun createHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient
