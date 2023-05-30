package one.mok

import android.app.Application
import com.unotag.mokone.MokSDK

class ExampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MokLogger.setLogLevel(MokLogger.LogLevel.DEBUG)
    }
}
