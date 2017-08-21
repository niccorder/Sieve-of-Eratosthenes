package me.niccorder.prime.sieve.presenter

import com.google.common.truth.Truth.assertThat
import io.reactivex.subscribers.TestSubscriber
import me.niccorder.prime.RxSchedulersOverrideRule
import me.niccorder.prime.UnitTestLoggingTree
import me.niccorder.prime.internal.clock.Clock
import me.niccorder.prime.sieve.presenter.SievePresenter.CalculationState.COMPLETED
import me.niccorder.prime.sieve.presenter.SievePresenter.CalculationState.READY
import me.niccorder.prime.sieve.view.SieveView
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import timber.log.Timber

@RunWith(BlockJUnit4ClassRunner::class)
class SievePresenterTest {

    /**
     * Runs our observables sequentially.
     */
    @get:Rule public val exceptionRule = ExpectedException.none()

    /**
     * Failes on any exception.
     */
    @get:Rule public val rxRule = RxSchedulersOverrideRule()

    /**
     * Our mocked view. We will verify interactions on this.
     */
    @Mock lateinit var mockView: SieveView

    /**
     * Our mocked clock. This allows us to provide correct times.
     */
    @Mock lateinit var mockClock: Clock

    /**
     * Our test subject.
     */
    lateinit var testPresenter: SievePresenterImpl

    @Before
    fun setUp() {
        Timber.plant(UnitTestLoggingTree())
        MockitoAnnotations.initMocks(this)

        testPresenter = SievePresenterImpl(mockClock)
        testPresenter.clock = mockClock
        testPresenter.bind(mockView)
    }

    /**
     * Tests the normal case for the Sieve of Era. algorithm. Ensure we are returning all the
     * primes for a normal range.
     */
    @Test
    fun testSieve_normal() {
        val testSubject = TestSubscriber.create<Long>()

        testPresenter.onCalculateSieveTo(11).subscribe(testSubject)

        testSubject.assertValues(2L, 3L, 5L, 7L, 11L)
        testSubject.assertNoErrors()
        testSubject.assertComplete()
    }

    /**
     * Tests to ensure we emit the correct amount of primes for a large input.
     * <p />
     * Please reference: https://primes.utm.edu/howmany.html
     */
    @Test
    fun testSieve_normal_largeInput() {
        val testSubject = TestSubscriber.create<Long>()

        testPresenter.onCalculateSieveTo(1_000_000).subscribe(testSubject)

        testSubject.assertValueCount(78499)
        testSubject.assertNoErrors()
        testSubject.assertComplete()
    }

    /**
     * Tests that we emit an error when a negative value has been supplied to the sieve algorithm.
     */
    @Test
    fun testSieve_error_negativeInput() {
        val testSubject = TestSubscriber.create<Long>()

        testPresenter.onCalculateSieveTo(-1).subscribe(testSubject)

        testSubject.assertError(IllegalArgumentException::class.java)
    }

    /**
     * Tests the normal interaction when the user clicks on the control button with valid data.
     */
    @Test
    fun testClick_ready_normal() {
        // Setting up mocks to return values we will later validate.
        val startTime = 10L
        val endTime = 200L
        `when`(mockClock.currentTimeNanos()).thenReturn(startTime, endTime)

        val testSubscriber = TestSubscriber.create<Long>()

        testPresenter.calculationState = READY
        testPresenter.handleReadyClick("1000000").subscribe(testSubscriber)

        verify(mockView).showKeyboard(false)
        verify(mockView).displayCalculationRunning()
    }

    /**
     * Tests the control button click while in the ready state, but the data is invalid.
     */
    @Test
    fun testClick_ready_invalidData() {
        testPresenter.calculationState = READY
        testPresenter.onControlButtonClicked("-1")

        verify(mockView, never()).notifyPrimeAdded()

        verify(mockView).showInputError(any(IllegalArgumentException::class.java))
        assertThat(testPresenter.calculationState).isEqualTo(READY)
    }


    /**
     * Tests that we cleared our view, and reset our state upon a click while we are in the
     * `completed` state.
     */
    @Test
    fun testClick_completed() {
        testPresenter.calculationState = COMPLETED
        testPresenter.onControlButtonClicked("200")

        verify(mockView).clearPrimes()
        verify(mockView, atLeastOnce()).showInitalState()

        assertThat(testPresenter.calculationState).isEqualTo(READY)
    }
}