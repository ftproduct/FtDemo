package com.freighttiger.driverassistant.domain.workflow

import com.freighttiger.driverassistant.core.model.ActivityEntry
import com.freighttiger.driverassistant.core.model.ActivityKind
import com.freighttiger.driverassistant.domain.ports.ActivityLog
import com.freighttiger.driverassistant.domain.ports.IdGenerator
import com.freighttiger.driverassistant.domain.ports.TimeSource

/** Writes driver-visible history. Callers must pass codes/statuses only — never transcripts or phone numbers. */
class ActivityRecorder(
    private val log: ActivityLog,
    private val ids: IdGenerator,
    private val clock: TimeSource,
) {
    suspend fun record(kind: ActivityKind, detail: String, tripId: String? = null, simulated: Boolean = false) {
        log.append(ActivityEntry(ids.newId(), clock.now(), kind, detail, tripId, simulated))
    }
}
