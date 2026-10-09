package com.freighttiger.driverassistant.di

import com.freighttiger.driverassistant.core.network.mock.MockFreightTigerBackend
import com.freighttiger.driverassistant.domain.backend.AssistantBackend
import com.freighttiger.driverassistant.domain.backend.AuthGateway

enum class BackendMode { DEMO_SIMULATED, REMOTE, NOT_CONFIGURED }

/**
 * Which backend this build talks to. Decided once at start-up from build configuration:
 *  - demo mode            → simulated backend (everything labelled as demo)
 *  - base URL configured  → real HTTP backend
 *  - otherwise            → "not configured" backend that fails every call (no fake success)
 */
class BackendSelection(
    val mode: BackendMode,
    val backend: AssistantBackend,
    val auth: AuthGateway,
    val mock: MockFreightTigerBackend?,
)
