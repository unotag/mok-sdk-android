package one.mok

import com.unotag.mokone.MokApplication
import com.unotag.mokone.MokSDK
import com.unotag.mokone.utils.MokLogger

class ExampleApplication : MokApplication() {

    override fun onCreate() {
        registerActivityLifecycleCallbacks(this)
        super.onCreate()
        val mokSDK = MokSDK.getInstance(applicationContext)
        mokSDK.initMokSDK(isProdEnv = false)
        MokLogger.setLogLevel(MokLogger.LogLevel.DEBUG)
    }
}

