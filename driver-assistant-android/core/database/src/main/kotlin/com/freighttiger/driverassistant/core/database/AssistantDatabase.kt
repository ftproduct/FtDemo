package com.freighttiger.driverassistant.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TripEntity::class,
        ConsentEntity::class,
        InboundEventEntity::class,
        OutboxEntity::class,
        PromptEntity::class,
        ActivityEntity::class,
        SessionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AssistantDatabase : RoomDatabase() {
    abstract fun trips(): TripDao
    abstract fun consents(): ConsentDao
    abstract fun inboundEvents(): InboundEventDao
    abstract fun outbox(): OutboxDao
    abstract fun prompts(): PromptDao
    abstract fun activity(): ActivityDao
    abstract fun session(): SessionDao

    companion object {
        const val NAME = "driver_assistant.db"

        fun create(context: Context): AssistantDatabase =
            Room.databaseBuilder(context.applicationContext, AssistantDatabase::class.java, NAME)
                // MVP: no migrations yet. Must be replaced by real migrations before v2 of the schema.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()

        fun inMemory(context: Context): AssistantDatabase =
            Room.inMemoryDatabaseBuilder(context.applicationContext, AssistantDatabase::class.java).build()
    }
}
