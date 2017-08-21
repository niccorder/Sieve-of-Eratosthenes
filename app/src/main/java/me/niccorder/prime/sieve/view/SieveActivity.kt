package me.niccorder.prime.sieve.view

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import me.niccorder.prime.R
import me.niccorder.prime.internal.SieveScope
import me.niccorder.prime.internal.clock.Clock
import me.niccorder.prime.internal.widget.MarginItemDecoration
import me.niccorder.prime.sieve.presenter.SievePresenter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named


@SieveScope
@Subcomponent(modules = arrayOf(
        SieveModule::class
))
open interface SieveComponent : AndroidInjector<SieveActivity> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<SieveActivity>()
}

@Module
class SieveModule {

    @SieveScope
    @Provides
    fun provideAndroidClock(@Named("android_clock") clock: Clock): Clock = clock

    @SieveScope
    @Provides
    fun provideInputMethodManager(
            sieveActivity: SieveActivity
    ): InputMethodManager =
            sieveActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
}

class SieveActivity : DaggerAppCompatActivity(), SieveView {

    companion object {
        private const val NOTIFY_DELAY = 40L
    }

    private lateinit var inputDisposable: Disposable
    private lateinit var clickDisposable: Disposable
    private lateinit var scrollDisposable: Disposable

    @Inject lateinit var clock: Clock
    @Inject lateinit var inputMethodManager: InputMethodManager
    @Inject lateinit var handler: Handler
    @Inject lateinit var presenter: SievePresenter

    private lateinit var root: ViewGroup
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var inputView: EditText
    private lateinit var controlFab: FloatingActionButton

    private lateinit var primeRecycler: RecyclerView
    private lateinit var adapter: PrimeAdapter

    private var lastNotifiedAdapter: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        root = findViewById(R.id.root)
        controlFab = findViewById(R.id.fab)
        clickDisposable = RxView.clicks(controlFab)
                .subscribe({ presenter.onControlButtonClicked(inputView.text.toString()) })

        title = findViewById(R.id.title)
        subtitle = findViewById(R.id.subtitle)
        subtitle.setOnClickListener { presenter.onSubtitleClicked() }
        inputView = findViewById(R.id.input_et)
        inputDisposable = RxTextView.afterTextChangeEvents(inputView)
                .skipInitialValue()
                .debounce(60L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ presenter.onInputChanged(it.editable()?.toString()) })

        primeRecycler = findViewById(R.id.recycler)
        initRecycler()

        presenter.bind(this)
    }

    private fun initRecycler() {
        adapter = PrimeAdapter({ presenter.primeCount() }, { presenter.getNthPrime(it)!! })

        primeRecycler.adapter = adapter
        primeRecycler.layoutManager = GridLayoutManager(this, 4)
        primeRecycler.itemAnimator = null
        primeRecycler.addItemDecoration(MarginItemDecoration(this, R.dimen.grid_item_margin))

        // In the case we recreate the activity.
        if (presenter.primeCount() > 0) {
            adapter.notifyDataSetChanged()
        }

        scrollDisposable = RxRecyclerView.scrollStateChanges(primeRecycler)
                .debounce(100L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it == RecyclerView.SCROLL_STATE_IDLE) {
                        presenter.onScrollStopped()
                    } else if (it == RecyclerView.SCROLL_STATE_DRAGGING) {
                        presenter.onScrollStarted()
                    }
                })
    }

    override fun onDestroy() {
        super.onDestroy()

        // Releases disposables.
        inputDisposable.dispose()
        clickDisposable.dispose()
        scrollDisposable.dispose()

        // Releases this activities reference so that it can be GC'd as appropriate.
        presenter.unbind()
    }

    override fun notifyPrimeAdded() {
        handler.postAtTime({
            adapter.notifyPrimeAdded()
        }, getNextItemAddedDelay())
    }

    /**
     * To ensure we don't overwork the adapter with calls to notify the dataset changed,
     * we go ahead and add a basic delay to when the items will be displayed.
     */
    fun getNextItemAddedDelay(): Long {
        val timeSinceLastNotified = clock.uptimeMillis() - lastNotifiedAdapter

        if (timeSinceLastNotified > NOTIFY_DELAY) {
            lastNotifiedAdapter = clock.uptimeMillis()
        } else {
            lastNotifiedAdapter += NOTIFY_DELAY
        }

        return lastNotifiedAdapter
    }

    override fun clearPrimes() {
        handler.removeCallbacksAndMessages(null)
        adapter.clearAdapter()
    }

    override fun showInitalState() {
        displayControlIcon(R.drawable.ic_play_arrow_black_24dp)

        title.setText(R.string.title_welcome)
        subtitle.setText(R.string.subtitle_about)
        inputView.text = null

        title.visibility = View.VISIBLE
        subtitle.visibility = View.VISIBLE
        inputView.visibility = View.VISIBLE

        showKeyboard(true)
    }

    override fun showRangeInputLayout(show: Boolean) {
        val visibility = when (show) {
            true -> View.VISIBLE
            false -> View.GONE
        }

        title.visibility = visibility
        inputView.visibility = visibility
    }

    override fun showInputError(error: Throwable?) {
        if (error == null) {
            ContextCompat.getColor(this, R.color.red)
        } else {
            ContextCompat.getColor(this, R.color.black_70)
        }

        @ColorInt val color = when (error) {
            is IllegalArgumentException -> {
                R.color.red
            }
            null -> {
                R.color.black_70
            }
            else -> throw error
        }

        title.setTextColor(color)
        inputView.setTextColor(color)
    }

    override fun showUnknownError() {
        handler.post {
            Snackbar.make(root, getString(R.string.unknown_error_prompt), Snackbar.LENGTH_SHORT)
                    .show()
        }
    }

    override fun showControlButton(show: Boolean) {
        controlFab.visibility = when (show) {
            true -> View.VISIBLE
            false -> View.GONE
        }
    }

    override fun displayCalculationRunning() {
        handler.post {
            inputView.visibility = View.GONE
            subtitle.visibility = View.GONE
            title.visibility = View.VISIBLE
            title.setText(R.string.title_calculating)

            recycler.visibility = View.VISIBLE
        }
    }

    override fun displayCalculationPaused() {
        handler.post {
            displayControlIcon(R.drawable.ic_play_arrow_black_24dp)

            title.visibility = View.VISIBLE
            title.setText(R.string.title_paused)

            subtitle.visibility = View.VISIBLE
            subtitle.setText(R.string.subtitle_complete)
        }
    }

    override fun displayCalculationCompleted(duration: Long) {
        handler.post({
            displayControlIcon(R.drawable.ic_replay_black_24dp)

            subtitle.visibility = View.VISIBLE
            val durationInSeconds: Double = duration / 1000000000.0
            subtitle.text = getString(R.string.subtitle_complete, durationInSeconds.toString())

            title.visibility = View.VISIBLE
            title.setText(R.string.title_complete)

            inputView.visibility = View.GONE
            recycler.visibility = View.VISIBLE
        })
    }

    override fun showKeyboard(show: Boolean) {
        if (show) {
            inputView.requestFocus()
        } else {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    override fun displayCalculationError(error: Throwable) {
        when (error) {
            is IllegalArgumentException -> Snackbar.make(root, getString(R.string.error_invalid_argument), Snackbar.LENGTH_SHORT).show()
            is NumberFormatException -> Snackbar.make(root, getString(R.string.error_not_a_number), Snackbar.LENGTH_SHORT).show()
            else -> showUnknownError()
        }
    }

    private fun displayControlIcon(@DrawableRes iconRes: Int) {
        val icon = VectorDrawableCompat.create(resources, iconRes, theme)
        controlFab.setImageDrawable(icon)

        icon!!.setTint(ContextCompat.getColor(this, R.color.white))
    }
}