package me.niccorder.prime.sieve.presenter

import com.google.common.annotations.VisibleForTesting
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.niccorder.prime.internal.clock.Clock
import me.niccorder.prime.sieve.presenter.SievePresenter.CalculationState.*
import me.niccorder.prime.sieve.view.SieveView
import timber.log.Timber

/**
 * @see SievePresenter
 */
class SievePresenterImpl(
        var clock: Clock
) : SievePresenter {

    /**
     * The primes that we have calculated.
     */
    @VisibleForTesting
    var primes: ArrayList<Long> = ArrayList()

    /**
     * Our view that we are presenting.
     */
    @VisibleForTesting
    var view: SieveView? = null

    /**
     * An enum which keeps track of what state we are in the calculation.
     */
    @VisibleForTesting
    var calculationState: SievePresenter.CalculationState = READY

    /**
     * The current calculations end value.
     */
    var currentEndValue: Long = -1

    /**
     * The most recent prime which has been calculated.
     */
    @VisibleForTesting
    var currentPrime: Long = -1

    /**
     * The disposable for the current calculation, or null if there is no current calculation.
     */
    var calculationDisposable: Disposable? = null

    /**
     * The time at which the current calculation has began.
     */
    var calculationStart: Long = 0

    /**
     * The duration of the most recent calculation.
     */
    var calculationDuration: Long = 0

    override fun bind(view: SieveView) {
        this.view = view

        when (calculationState) {
            READY -> view.showInitalState()
            RUNNING -> view.displayCalculationRunning()
//            PAUSED -> view.displayCalculationPaused()
            COMPLETED -> view.displayCalculationCompleted(calculationDuration)
        }
    }

    override fun unbind() {
        this.view = null
    }

    override fun getNthPrime(n: Int): Long? {
        return primes[n]
    }

    override fun onCalculateSieveTo(endValue: Long): Flowable<Long> {
        return onCalculateSieveTo(2, endValue)
    }

    /**
     * Calculates and emits primes between the start and end values.
     *
     * @throws IllegalArgumentException
     */
    private fun onCalculateSieveTo(startValue: Long, endValue: Long): Flowable<Long> {
        if (startValue < 0 || endValue < 0 || endValue <= startValue)
            return Flowable.error(IllegalArgumentException())

        if (endValue == 1L) return Flowable.empty()
        if (calculationState != READY) return Flowable.empty()

        calculationState = RUNNING
        currentEndValue = endValue
        return Flowable.create({
            // Setup the map according to the Sieve algorithm.
            // For faster setup, this is a negated map (i.e. all primes = false)
            val notPrimeMap = BooleanArray(((endValue - startValue) + 1).toInt())

            // Iterate through the range of values
            (startValue..endValue).map { i ->

                // if the current endValue is a prime number, emit it
                val adjustedIndex = (i - startValue).toInt()
                if (!notPrimeMap[adjustedIndex]) {
                    currentPrime = i

                    // Iterate through all multiples between (prime, endValue]
                    // and mark them as non-prime
                    var j = 1
                    while (++j * currentPrime < endValue) {
                        notPrimeMap[((j * currentPrime) - startValue).toInt()] = true
                    }

                    it.onNext(currentPrime)
                }
            }

            // Notify the subscriber we have completed.
            it.onComplete()
        }, BackpressureStrategy.BUFFER)
    }

    override fun primeCount(): Int {
        return primes.size
    }


    override fun onInputChanged(input: String?) {
        if (input == null) return

        try {
            val num = input.toLong()
            if (num <= 0) throw IllegalArgumentException()

            view?.showInputError(null)
        } catch (e: NumberFormatException) {
            view?.showInputError(e)
        } catch (e: Throwable) {
            view?.showUnknownError()
        }
    }

    override fun onScrollStarted() {
        if (calculationState != RUNNING) {
            view?.showControlButton(false)
        }
    }

    override fun onScrollStopped() {
        if (calculationState != RUNNING) {
            view?.showControlButton(true)
        }
    }

    override fun onSubtitleClicked() {
        if (calculationState == READY) {
            view?.showKeyboard(true)
        }
    }

    override fun onControlButtonClicked(input: String) {
        when (calculationState) {
            READY -> calculationDisposable = handleReadyClick(input).subscribe({}, Timber::e)
//            PAUSED -> onResumeCalculation()
//            RUNNING -> onPauseCalculation()
            COMPLETED -> onResetCalculation()
        }
    }

    @VisibleForTesting
    fun handleReadyClick(input: String): Flowable<Long> {
        try {
            val longInput = input.toLong()
            if (longInput < 0) throw IllegalArgumentException()
        } catch (t: Throwable) {
            when (t) {
                is NumberFormatException,
                is IllegalArgumentException -> view?.showInputError(t)
                else -> view?.showUnknownError()
            }

            return Flowable.error(t)
        }

        // Hide the keyboard
        view?.showKeyboard(false)
        view?.displayCalculationRunning()
        view?.showControlButton(false)

        // Begin timing algorithm.
        calculationStart = clock.currentTimeNanos()
        return onCalculateSieveTo(input.toLong())
                .filter { !primes.contains(it) }
                .doOnNext { primes.add(it) }
                .subscribeOn(Schedulers.computation())
                .doOnNext { view?.notifyPrimeAdded() }
                .doOnError { view?.displayCalculationError(it) }
                .doOnComplete { handleSieveCompleted(calculationStart) }
    }


    @VisibleForTesting
    fun handleSieveCompleted(start: Long) {
        calculationDuration += clock.currentTimeNanos() - start

        view?.showControlButton(true)
        view?.displayCalculationCompleted(calculationDuration)
    }

    /**
     * Not completed. WIP
     */
    override fun onPauseCalculation() {
        if (calculationState == RUNNING) {
            Timber.d("onPauseCalculation()")

            calculationDuration = clock.currentTimeNanos() - calculationStart
            if (calculationState == RUNNING) {
                calculationState = PAUSED

                if (calculationDisposable?.isDisposed != true) {
                    calculationDisposable?.dispose()
                }
            }
        }
    }

    /**
     * Not completed. WIP
     */
    override fun onResumeCalculation() {
        if (calculationState == PAUSED) {
            Timber.d("onResumeCalculation()")
            calculationState = RUNNING

            view?.displayCalculationRunning()
            calculationStart = clock.currentTimeNanos()
            onCalculateSieveTo(currentPrime, currentEndValue)
                    .doOnNext { primes.add(it) }
                    .subscribeOn(Schedulers.computation())
                    .subscribe(
                            { view?.notifyPrimeAdded() },
                            { view?.showUnknownError() },
                            { handleSieveCompleted(calculationStart) }
                    )
        }
    }

    private fun onResetCalculation() {
        if (calculationState == COMPLETED) {
            Timber.d("onResetCalculation()")

            primes = ArrayList()
            view?.clearPrimes()
            calculationState = READY

            view?.showInitalState()
        }
    }
}