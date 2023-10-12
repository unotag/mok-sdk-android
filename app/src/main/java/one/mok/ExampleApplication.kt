package one.mok

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.unotag.mokone.MokSDK
import com.unotag.mokone.utils.MokLogger

class ExampleApplication : Application(), Application.ActivityLifecycleCallbacks {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // This is called when an activity is created. You can put your initialization code here.
        if (activity is MainActivity) { // Change to the appropriate activity class
            val mokSDK = MokSDK.getInstance(applicationContext)
            mokSDK.initMokSDK(isProductionEvn = false)
            MokLogger.setLogLevel(MokLogger.LogLevel.DEBUG)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        MokLogger.log(MokLogger.LogLevel.INFO, "onActivityStarted: ${activity.javaClass.simpleName}")
    }

    override fun onActivityResumed(activity: Activity) {
        MokLogger.log(MokLogger.LogLevel.INFO, "onActivityResumed: ${activity.javaClass.simpleName}")
    }

    override fun onActivityPaused(activity: Activity) {
        MokLogger.log(MokLogger.LogLevel.INFO, "onActivityPaused: ${activity.javaClass.simpleName}")
    }

    override fun onActivityStopped(activity: Activity) {
        MokLogger.log(MokLogger.LogLevel.INFO, "onActivityStopped: ${activity.javaClass.simpleName}")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        MokLogger.log(MokLogger.LogLevel.INFO, "onActivitySaveInstanceState: ${activity.javaClass.simpleName}")
    }

    override fun onActivityDestroyed(activity: Activity) {
        MokLogger.log(MokLogger.LogLevel.INFO, "onActivityDestroyed: ${activity.javaClass.simpleName}")
    }

    // Implement other ActivityLifecycleCallbacks methods if needed

    override fun onTerminate() {
        super.onTerminate()
        unregisterActivityLifecycleCallbacks(this)
    }
}

