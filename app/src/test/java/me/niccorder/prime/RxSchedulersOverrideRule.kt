package me.niccorder.prime

import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.functions.Function
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.Callable

class RxSchedulersOverrideRule : TestRule {

    private val mRxJavaImmediateScheduler: Function<Callable<Scheduler>, Scheduler>
            = Function { TestScheduler() }

    override fun apply(base: Statement?, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                RxAndroidPlugins.reset()
                RxAndroidPlugins.onMainThreadScheduler(Schedulers.trampoline())
                RxAndroidPlugins.setInitMainThreadSchedulerHandler(mRxJavaImmediateScheduler)

                RxJavaPlugins.reset()
                RxJavaPlugins.setInitIoSchedulerHandler(mRxJavaImmediateScheduler)
                RxJavaPlugins.setInitComputationSchedulerHandler(mRxJavaImmediateScheduler)
                RxJavaPlugins.setInitNewThreadSchedulerHandler(mRxJavaImmediateScheduler)

                base?.evaluate()

                RxAndroidPlugins.reset()
                RxJavaPlugins.reset()
            }
        }
    }

}