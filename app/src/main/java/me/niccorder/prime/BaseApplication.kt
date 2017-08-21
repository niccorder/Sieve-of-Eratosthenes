package me.niccorder.prime

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.crashlytics.android.Crashlytics
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.android.support.DaggerApplication
import io.fabric.sdk.android.Fabric
import me.niccorder.prime.internal.AppScope
import me.niccorder.prime.internal.CrashlyticsLoggingTree
import me.niccorder.prime.internal.clock.AndroidClock
import me.niccorder.prime.internal.clock.Clock
import me.niccorder.prime.sieve.presenter.SievePresenter
import me.niccorder.prime.sieve.presenter.SievePresenterImpl
import me.niccorder.prime.sieve.view.SieveComponent
import timber.log.Timber
import javax.inject.Named


class BaseApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder()
                .application(this)
                .create(this)
    }

    override fun onCreate() {
        super.onCreate()

        Fabric.with(this, Crashlytics())
        Timber.plant(when (BuildConfig.DEBUG) {
            true -> Timber.DebugTree()
            false -> CrashlyticsLoggingTree()
        })
    }
}

@Module(subcomponents = arrayOf(
        SieveComponent::class
))
class AppModule {

    @AppScope
    @Provides
    fun provideApplicationContext(baseApp: BaseApplication): Context = baseApp

    @AppScope
    @Provides
    fun provideMainHandler(): Handler = Handler(Looper.getMainLooper())

    @AppScope
    @Provides
    @Named("android_clock")
    fun provideAndroidClock(): Clock = AndroidClock()

    @AppScope
    @Provides
    fun provideSievePresenter(
            @Named("android_clock") clock: Clock
    ): SievePresenter = SievePresenterImpl(clock)
}

@AppScope
@Component(modules = arrayOf(
        AndroidSupportInjectionModule::class,
        AppModule::class,
        ActivityBuilder::class
))
interface AppComponent : AndroidInjector<BaseApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<BaseApplication>() {

        @BindsInstance
        abstract fun application(app: Application): Builder
    }
}