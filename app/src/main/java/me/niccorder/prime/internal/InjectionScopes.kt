package me.niccorder.prime.internal

import javax.inject.Scope

/**
 * Dagger2 scope for objects that live the entirety of the application (singletons)
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope

/**
 * Dagger2 scope for objects that live inside of the SieveActivity
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class SieveScope