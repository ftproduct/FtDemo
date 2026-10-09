package com.freighttiger.driverassistant.di

import javax.inject.Qualifier

/** Process-lifetime coroutine scope for background orchestration. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
