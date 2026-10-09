package com.freighttiger.driverassistant.di

import android.content.Context
import android.util.Log
import com.freighttiger.driverassistant.AppConfig
import com.freighttiger.driverassistant.core.database.AssistantDatabase
import com.freighttiger.driverassistant.core.database.RoomActivityLog
import com.freighttiger.driverassistant.core.database.RoomConsentRepository
import com.freighttiger.driverassistant.core.database.RoomInboundEventRepository
import com.freighttiger.driverassistant.core.database.RoomOutboxRepository
import com.freighttiger.driverassistant.core.database.RoomPromptRepository
import com.freighttiger.driverassistant.core.database.RoomSessionRepository
import com.freighttiger.driverassistant.core.database.RoomTripRepository
import com.freighttiger.driverassistant.core.network.mock.MockFreightTigerBackend
import com.freighttiger.driverassistant.core.network.push.InboundEventParser
import com.freighttiger.driverassistant.core.network.remote.NetworkFactory
import com.freighttiger.driverassistant.core.network.remote.RemoteAssistantBackend
import com.freighttiger.driverassistant.core.network.remote.RemoteAuthGateway
import com.freighttiger.driverassistant.core.network.remote.UnconfiguredBackend
import com.freighttiger.driverassistant.core.security.KeystoreTokenStore
import com.freighttiger.driverassistant.data.SettingsRepository
import com.freighttiger.driverassistant.domain.backend.AssistantBackend
import com.freighttiger.driverassistant.domain.backend.AuthGateway
import com.freighttiger.driverassistant.domain.conversation.DialogueEngine
import com.freighttiger.driverassistant.domain.onboarding.OnboardingFlow
import com.freighttiger.driverassistant.domain.ports.AccessTokenStore
import com.freighttiger.driverassistant.domain.ports.ActivityLog
import com.freighttiger.driverassistant.domain.ports.ConnectivityMonitor
import com.freighttiger.driverassistant.domain.ports.ConsentRepository
import com.freighttiger.driverassistant.domain.ports.IdGenerator
import com.freighttiger.driverassistant.domain.ports.InboundEventRepository
import com.freighttiger.driverassistant.domain.ports.OutboxRepository
import com.freighttiger.driverassistant.domain.ports.PromptRepository
import com.freighttiger.driverassistant.domain.ports.SessionRepository
import com.freighttiger.driverassistant.domain.ports.TimeSource
import com.freighttiger.driverassistant.domain.ports.TripRepository
import com.freighttiger.driverassistant.domain.sync.OutboxSyncEngine
import com.freighttiger.driverassistant.domain.voice.HindiPromptCatalog
import com.freighttiger.driverassistant.domain.voice.IntentClassifier
import com.freighttiger.driverassistant.domain.voice.PromptCatalog
import com.freighttiger.driverassistant.domain.voice.RuleBasedIntentClassifier
import com.freighttiger.driverassistant.domain.voice.SpeechInput
import com.freighttiger.driverassistant.domain.voice.SpeechOutput
import com.freighttiger.driverassistant.domain.workflow.ActivityRecorder
import com.freighttiger.driverassistant.domain.workflow.PromptScheduler
import com.freighttiger.driverassistant.domain.workflow.SyncTrigger
import com.freighttiger.driverassistant.domain.workflow.TripCoordinator
import com.freighttiger.driverassistant.domain.workflow.TripEventProcessor
import com.freighttiger.driverassistant.platform.connectivity.AndroidConnectivityMonitor
import com.freighttiger.driverassistant.platform.speech.AndroidSpeechRecognizerInput
import com.freighttiger.driverassistant.platform.speech.AndroidTextToSpeechOutput
import com.freighttiger.driverassistant.platform.sync.SyncScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun appConfig(): AppConfig = AppConfig.fromBuildConfig()

    @Provides @Singleton @ApplicationScope
    fun applicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides @Singleton
    fun timeSource(): TimeSource = TimeSource.System

    @Provides @Singleton
    fun idGenerator(): IdGenerator = IdGenerator.Uuid

    // ---------------------------------------------------------------- persistence

    @Provides @Singleton
    fun database(@ApplicationContext context: Context): AssistantDatabase = AssistantDatabase.create(context)

    @Provides @Singleton fun sessions(db: AssistantDatabase): SessionRepository = RoomSessionRepository(db.session())
    @Provides @Singleton fun trips(db: AssistantDatabase): TripRepository = RoomTripRepository(db.trips())
    @Provides @Singleton fun consents(db: AssistantDatabase): ConsentRepository = RoomConsentRepository(db.consents())
    @Provides @Singleton fun inbound(db: AssistantDatabase): InboundEventRepository = RoomInboundEventRepository(db.inboundEvents())
    @Provides @Singleton fun outbox(db: AssistantDatabase): OutboxRepository = RoomOutboxRepository(db.outbox())
    @Provides @Singleton fun prompts(db: AssistantDatabase): PromptRepository = RoomPromptRepository(db.prompts())
    @Provides @Singleton fun roomActivityLog(db: AssistantDatabase): RoomActivityLog = RoomActivityLog(db.activity())
    @Provides @Singleton fun activityLog(log: RoomActivityLog): ActivityLog = log

    @Provides @Singleton
    fun tokenStore(@ApplicationContext context: Context): AccessTokenStore = KeystoreTokenStore(context)

    // ---------------------------------------------------------------- backend

    @Provides @Singleton
    fun backendSelection(config: AppConfig, tokens: AccessTokenStore, clock: TimeSource, ids: IdGenerator): BackendSelection {
        if (config.demoMode) {
            val mock = MockFreightTigerBackend(clock, ids)
            return BackendSelection(BackendMode.DEMO_SIMULATED, mock, mock, mock)
        }
        if (config.api.isConfigured) {
            val client = NetworkFactory.okHttpClient(config.api, { tokens.current() }) { Log.d("FtdaHttp", it) }
            val api = NetworkFactory.api(config.api, client)
            return BackendSelection(BackendMode.REMOTE, RemoteAssistantBackend(api, config.api, clock), RemoteAuthGateway(api), null)
        }
        val unconfigured = UnconfiguredBackend()
        return BackendSelection(BackendMode.NOT_CONFIGURED, unconfigured, unconfigured, null)
    }

    @Provides fun backend(selection: BackendSelection): AssistantBackend = selection.backend
    @Provides fun auth(selection: BackendSelection): AuthGateway = selection.auth

    @Provides @Singleton
    fun inboundParser(clock: TimeSource): InboundEventParser = InboundEventParser(clock)

    // ---------------------------------------------------------------- domain

    @Provides @Singleton
    fun activityRecorder(log: ActivityLog, ids: IdGenerator, clock: TimeSource): ActivityRecorder = ActivityRecorder(log, ids, clock)

    @Provides @Singleton
    fun promptScheduler(repo: PromptRepository, ids: IdGenerator, clock: TimeSource, settings: SettingsRepository): PromptScheduler =
        PromptScheduler(repo, ids, clock, settings.current.toPromptPolicy())

    @Provides @Singleton
    fun tripEventProcessor(
        sessions: SessionRepository, trips: TripRepository, consents: ConsentRepository, inbound: InboundEventRepository,
        outbox: OutboxRepository, scheduler: PromptScheduler, activity: ActivityRecorder, clock: TimeSource,
    ): TripEventProcessor = TripEventProcessor(sessions, trips, consents, inbound, outbox, scheduler, activity, clock)

    @Provides @Singleton
    fun tripCoordinator(
        sessions: SessionRepository, trips: TripRepository, consents: ConsentRepository, outbox: OutboxRepository,
        scheduler: PromptScheduler, activity: ActivityRecorder, ids: IdGenerator, clock: TimeSource, syncTrigger: SyncTrigger,
    ): TripCoordinator = TripCoordinator(sessions, trips, consents, outbox, scheduler, activity, ids, clock, syncTrigger)

    @Provides @Singleton
    fun outboxSyncEngine(
        outbox: OutboxRepository, backend: AssistantBackend, connectivity: ConnectivityMonitor, trips: TripRepository,
        consents: ConsentRepository, scheduler: PromptScheduler, activity: ActivityRecorder, clock: TimeSource,
    ): OutboxSyncEngine = OutboxSyncEngine(outbox, backend, connectivity, trips, consents, scheduler, activity, clock)

    @Provides @Singleton fun promptCatalog(): PromptCatalog = HindiPromptCatalog()
    @Provides @Singleton fun intentClassifier(): IntentClassifier = RuleBasedIntentClassifier()

    @Provides @Singleton
    fun dialogueEngine(classifier: IntentClassifier, catalog: PromptCatalog): DialogueEngine = DialogueEngine(classifier, catalog)

    /** Not a singleton: each onboarding run gets a fresh state machine. */
    @Provides
    fun onboardingFlow(
        auth: AuthGateway, backend: AssistantBackend, sessions: SessionRepository, trips: TripRepository,
        tokens: AccessTokenStore, clock: TimeSource,
    ): OnboardingFlow = OnboardingFlow(auth, backend, sessions, trips, tokens, clock)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PlatformBindings {
    @Binds abstract fun speechOutput(impl: AndroidTextToSpeechOutput): SpeechOutput
    @Binds abstract fun speechInput(impl: AndroidSpeechRecognizerInput): SpeechInput
    @Binds abstract fun connectivity(impl: AndroidConnectivityMonitor): ConnectivityMonitor
    @Binds abstract fun syncTrigger(impl: SyncScheduler): SyncTrigger
}
