package me.niccorder.prime.sieve.presenter

import io.reactivex.Flowable
import me.niccorder.prime.sieve.view.SieveView

/**
 * Our presenter in the MVP pattern. This contains all business logic, and is the most foundational
 * item to be tested. It is vital that we not pass in any android.* package dependencies into our
 * presenter so we can effectively test it using JUnit4 without robo-electric.
 */
interface SievePresenter {

    /**
     * Binds the given view to this presenter. You must call unbind in the respective lifecycle's
     * cleanup call which you bound this in.
     * <p />
     * For example, if this was bound in onResume, you must call `unbind()` in onPause. Otherwise,
     * you will potentially leak the view and all references which is contains.
     *
     * @see #unbind()
     */
    fun bind(view: SieveView): Unit

    /**
     * Releases the view from this presenter.
     */
    fun unbind(): Unit

    /**
     * Returns the n-th prime, or null if we do not know the N-th prime.
     *
     * @param n an int value of the prime to be retrieved.
     */
    fun getNthPrime(n: Int): Long?

    /**
     * @return the number of calculated primes.
     */
    fun primeCount(): Int

    /**
     * To be called when the view's input text has changed.
     */
    fun onInputChanged(input: String?)

    /**
     * To be called when the user has began scrolling through the prime list.
     */
    fun onScrollStarted()

    /**
     * To be called when the user has stopped scrolling through the prime list.
     */
    fun onScrollStopped()

    /**
     * To be called when the subtitle has been clicked.
     */
    fun onSubtitleClicked()

    /**
     * To be called when the control button for the Sieve has been clicked.
     */
    fun onControlButtonClicked(input: String)

    /**
     * To be called when we want to calculate the sieve to the given number.
     */
    fun onCalculateSieveTo(int: Long): Flowable<Long>

    /**
     * To be called when the current calculation has been requested to pause.
     */
    fun onPauseCalculation(): Unit

    /**
     * To be called when we are requested to resume the given calculation.
     */
    fun onResumeCalculation(): Unit


    /**
     * An enum of the states we could be in while calculating the sieve.
     */
    enum class CalculationState(name: String) {
        READY("ready"),
        RUNNING("running"),
        PAUSED("paused"),
        COMPLETED("completed");

        companion object {
            fun from(
                    string: String?
            ): CalculationState = when (string) {
                null, READY.name -> READY
                RUNNING.name -> RUNNING
                PAUSED.name -> PAUSED
                COMPLETED.name -> COMPLETED
                else -> throw IllegalArgumentException("Must pass a String that represents a CalculationState.")
            }
        }

        override fun toString(): String {
            return name
        }
    }
}