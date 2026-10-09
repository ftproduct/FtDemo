package com.freighttiger.driverassistant.core.network.remote

import com.freighttiger.driverassistant.core.network.dto.ApiErrorResponse
import com.freighttiger.driverassistant.core.network.dto.ContractViolation
import com.freighttiger.driverassistant.domain.backend.BackendError
import com.freighttiger.driverassistant.domain.backend.BackendResult
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.IOException

/**
 * Executes one HTTP call and maps the outcome to [BackendResult]:
 *  - 2xx with a body           → Success (mapped by [transform])
 *  - IOException               → Network (retryable)
 *  - 400/422                   → Validation(error_code) (permanent)
 *  - 401/403                   → Unauthorized
 *  - 404                       → NotFound
 *  - 409                       → Validation(IDEMPOTENCY_CONFLICT): key reused with a different payload
 *  - 429                       → RateLimited(Retry-After)
 *  - 5xx / other               → Server (retryable)
 *  - unparseable body          → MalformedResponse (retryable)
 */
internal suspend fun <T, R> apiCall(json: Json, call: suspend () -> Response<T>, transform: (T) -> R): BackendResult<R> {
    val response = try {
        call()
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        return BackendResult.Failure(BackendError.Network(e.javaClass.simpleName))
    } catch (e: SerializationException) {
        return BackendResult.Failure(BackendError.MalformedResponse(e.javaClass.simpleName))
    }
    if (response.isSuccessful) {
        val body = response.body() ?: return BackendResult.Failure(BackendError.MalformedResponse("EMPTY_BODY"))
        return try {
            BackendResult.Success(transform(body))
        } catch (e: ContractViolation) {
            BackendResult.Failure(BackendError.MalformedResponse(e.message))
        } catch (e: IllegalArgumentException) {
            BackendResult.Failure(BackendError.MalformedResponse(e.message))
        }
    }
    val errorCode = response.errorBody()?.let { body ->
        try {
            json.decodeFromString(ApiErrorResponse.serializer(), body.string()).errorCode
        } catch (e: Exception) {
            null
        }
    }
    val error = when (val code = response.code()) {
        400, 422 -> BackendError.Validation(errorCode ?: "VALIDATION_ERROR")
        401, 403 -> BackendError.Unauthorized
        404 -> BackendError.NotFound(errorCode ?: "NOT_FOUND")
        409 -> BackendError.Validation(errorCode ?: "IDEMPOTENCY_CONFLICT")
        429 -> BackendError.RateLimited(response.headers()["Retry-After"]?.toLongOrNull())
        else -> BackendError.Server(code)
    }
    return BackendResult.Failure(error)
}
