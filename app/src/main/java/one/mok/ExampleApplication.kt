package one.mok

import android.app.Application
import com.unotag.mokone.MokSDK

class ExampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val mokSDK = MokSDK.getInstance(applicationContext)
        mokSDK.initMokSDK()

        MokLogger.setLogLevel(MokLogger.LogLevel.DEBUG)
    }
}
