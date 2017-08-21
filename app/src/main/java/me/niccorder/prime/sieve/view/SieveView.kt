package me.niccorder.prime.sieve.view

/**
 * To be implemented by the view (activity, fragment, or android view) which will be displayin the
 * current sieve algorithm.
 */
interface SieveView {

    /**
     * Called when a new prime has been added.
     */
    fun notifyPrimeAdded()

    /**
     * Clears the list of displayed primes.
     */
    fun clearPrimes()

    /**
     * Displays the initial state of the view. This should display an info-like screen to explain
     * to the user how to use the application.
     */
    fun showInitalState()

    /**
     * Shows, or hides the control button.
     *
     * @param show <code>true</code> to display the input layout, or code false otherwise.
     */
    fun showRangeInputLayout(show: Boolean)

    /**
     * Displays the correct input field error for the given throwable error.
     */
    fun showInputError(error: Throwable?)

    /**
     * Displays an error state when we have run into an unknown issue.
     */
    fun showUnknownError()

    /**
     * Shows, or hides the control button.
     *
     * @param show <code>true</code> to display the button, or <code>false</code> to hide.
     */
    fun showControlButton(show: Boolean)

    /**
     * Displays that the calculation is currently running.
     */
    fun displayCalculationRunning()

    /**
     * Displays that the calculation has been paused.
     */
    fun displayCalculationPaused()

    /**
     * Display to the user that the calculation has been completed for the given sieve range.
     *
     * @param duration the total duration it took us to calculate the sieve.
     */
    fun displayCalculationCompleted(duration: Long)

    /**
     * Displays the appropriate information given the error that had occurred during calculations.
     */
    fun displayCalculationError(error: Throwable)

    /**
     * Shows/hides the keyboard for user input.
     */
    fun showKeyboard(show: Boolean)
}