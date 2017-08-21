package me.niccorder.prime.internal

import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber

/**
 * An implementation of jake wharton's Timber which wraps logcat. This tree should be planted when
 * producing a release build. It will log to crashlytics based on the rules below.
 */
class CrashlyticsLoggingTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        when (priority) {
            Log.ERROR, Log.WARN -> {
                if (t == null) {
                    Crashlytics.log(message)
                } else {
                    Crashlytics.logException(t)
                }
            }
        }
    }
}