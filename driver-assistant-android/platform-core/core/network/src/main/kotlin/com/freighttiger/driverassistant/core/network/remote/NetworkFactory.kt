package com.freighttiger.driverassistant.core.network.remote

import com.freighttiger.driverassistant.core.network.ApiConfig
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

/** Supplies the current access token (from Keystore-backed storage). Never logged. */
fun interface AccessTokenProvider {
    fun currentToken(): String?
}

/** Logs request metadata only (method, path, status, duration). Never bodies, tokens or transcripts. */
fun interface NetworkLogger {
    fun log(message: String)
}

class AuthHeaderInterceptor(
    private val tokens: AccessTokenProvider,
    private val config: ApiConfig,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
            .header("Accept", "application/json")
            .header("Accept-Language", "hi-IN")
            .header("X-Client-Platform", "android")
            .header("X-Client-Version", config.clientVersion)
            .header("X-Schema-Version", ApiConfig.SCHEMA_VERSION)
            .header("X-Request-Id", UUID.randomUUID().toString())
        tokens.currentToken()?.let { builder.header("Authorization", "Bearer $it") }
        return chain.proceed(builder.build())
    }
}

class MetadataLoggingInterceptor(private val logger: NetworkLogger) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val started = System.nanoTime()
        val response = chain.proceed(request)
        val ms = (System.nanoTime() - started) / 1_000_000
        logger.log("${request.method} ${request.url.encodedPath} -> ${response.code} (${ms}ms)")
        return response
    }
}

object NetworkFactory {
    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = true
        encodeDefaults = true
    }

    fun okHttpClient(
        config: ApiConfig,
        tokens: AccessTokenProvider,
        logger: NetworkLogger? = null,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
        .writeTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
        // Retries are owned by the outbox (bounded backoff + idempotency), not by OkHttp.
        .retryOnConnectionFailure(false)
        .addInterceptor(AuthHeaderInterceptor(tokens, config))
        .apply { if (logger != null) addInterceptor(MetadataLoggingInterceptor(logger)) }
        .build()

    fun api(config: ApiConfig, client: OkHttpClient): FreightTigerAssistantApi {
        require(config.isConfigured) { "API base URL is not configured for environment '${config.environment}'" }
        return Retrofit.Builder()
            .baseUrl(config.normalizedBaseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FreightTigerAssistantApi::class.java)
    }
}
