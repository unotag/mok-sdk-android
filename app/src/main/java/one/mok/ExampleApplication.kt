package one.mok

import android.app.Application
import com.unotag.mokone.MokSDK
import com.unotag.mokone.utils.MokLogger

class ExampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val mokSDK = MokSDK.getInstance(applicationContext)
        mokSDK.initMokSDK(isProductionEvn = false)
        MokLogger.setLogLevel(MokLogger.LogLevel.DEBUG)
    }
}
