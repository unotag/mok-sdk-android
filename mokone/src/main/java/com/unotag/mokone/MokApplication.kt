package com.unotag.mokone

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.unotag.mokone.utils.MokLogger


open class MokApplication : Application(), Application.ActivityLifecycleCallbacks {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        MokLogger.log(
            MokLogger.LogLevel.INFO,
            "onActivityCreated: ${activity.javaClass.simpleName}"
        )

        val mokSDK = MokSDK.getInstance(applicationContext)
        val currentActivityName = "splash"

        if (activity.javaClass.simpleName == currentActivityName) {
            MokLogger.setLogLevel(MokLogger.LogLevel.DEBUG)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        MokLogger.log(
            MokLogger.LogLevel.INFO,
            "onActivityStarted: ${activity.javaClass.simpleName}"
        )
    }

    override fun onActivityResumed(activity: Activity) {
        MokLogger.log(
            MokLogger.LogLevel.INFO,
            "onActivityResumed: ${activity.javaClass.simpleName}"
        )
    }

    override fun onActivityPaused(activity: Activity) {
        MokLogger.log(MokLogger.LogLevel.INFO, "onActivityPaused: ${activity.javaClass.simpleName}")
    }

    override fun onActivityStopped(activity: Activity) {
        MokLogger.log(
            MokLogger.LogLevel.INFO,
            "onActivityStopped: ${activity.javaClass.simpleName}"
        )
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        MokLogger.log(
            MokLogger.LogLevel.INFO,
            "onActivitySaveInstanceState: ${activity.javaClass.simpleName}"
        )
    }

    override fun onActivityDestroyed(activity: Activity) {
        MokLogger.log(
            MokLogger.LogLevel.INFO,
            "onActivityDestroyed: ${activity.javaClass.simpleName}"
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterActivityLifecycleCallbacks(this)
    }
}
