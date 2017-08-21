package me.niccorder.prime.internal.clock

/**
 * An interface used to get around bringing in the android dependency into our presenter. This also
 * allows us to easily mock the class for testing in the chance we need to test time intervals.
 */
interface Clock {

    /**
     * The amount of time we have been running.
     */
    fun uptimeMillis(): Long

    /**
     * @return the current time of the clock in milliseconds.
     */
    fun currentTimeMillis(): Long

    /**
     * @return the current time of the clock in nanoseconds.
     */
    fun currentTimeNanos(): Long
}