package me.niccorder.prime

import timber.log.Timber

class UnitTestLoggingTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        if (t != null) {
            t.printStackTrace()
        } else {
            System.out.println(String.format("%s: %s", tag, message))
        }
    }
}