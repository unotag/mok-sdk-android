package com.unotag.mokone

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.unotag.mokone.utils.MokLogger

class ActivityLifecycleCallback {

    companion object {
        private var registered = false

        fun register(application: Application) {
            if (registered) {
                // If already registered, avoid redundant registrations
                return
            }

            registered = true
            application.registerActivityLifecycleCallbacks(object :
                Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
                    // Actions to perform on activity creation
                    // E.g., MokSDK.onActivityCreated(activity)
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
            })
        }
    }
}
