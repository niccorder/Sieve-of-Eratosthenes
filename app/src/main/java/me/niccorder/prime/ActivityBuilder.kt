package me.niccorder.prime

import android.app.Activity
import dagger.Binds
import dagger.Module
import dagger.android.ActivityKey
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import me.niccorder.prime.sieve.view.SieveActivity
import me.niccorder.prime.sieve.view.SieveComponent

@Module
abstract class ActivityBuilder {

    @Binds
    @IntoMap
    @ActivityKey(SieveActivity::class)
    abstract fun bindMainActivity(
            builder: SieveComponent.Builder
    ): AndroidInjector.Factory<out Activity>
}