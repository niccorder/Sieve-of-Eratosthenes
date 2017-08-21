package me.niccorder.prime.internal.clock

import android.os.SystemClock

/**
 * An implementation of our clock interface that uses the AndroidClick.
 *
 * @see Clock
 */
class AndroidClock : Clock {

    override fun uptimeMillis(): Long {
        return SystemClock.uptimeMillis()
    }

    override fun currentTimeMillis(): Long {
        return SystemClock.elapsedRealtime()
    }

    override fun currentTimeNanos(): Long {
        return SystemClock.elapsedRealtimeNanos()
    }
}