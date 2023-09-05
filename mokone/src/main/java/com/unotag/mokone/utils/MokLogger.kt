package com.unotag.mokone.utils
import android.util.Log

class MokLogger {
    enum class LogLevel {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        VERBOSE
    }

    companion object {
        private const val TAG = "MokLogger" // Shortened the tag
        private var logLevel = LogLevel.DEBUG

        fun setLogLevel(level: LogLevel) {
            logLevel = level
        }

        fun log(level: LogLevel, message: String, throwable: Throwable? = null) {
            if (level >= logLevel) {
                val logTag = "${TAG}_${level.name}" // Include the log level in the tag
                when (level) {
                    LogLevel.DEBUG -> Log.d(logTag, message)
                    LogLevel.INFO -> Log.i(logTag, message)
                    LogLevel.WARNING -> Log.w(logTag, message)
                    LogLevel.ERROR -> {
                        if (throwable != null) {
                            Log.e(logTag, message, throwable)
                        } else {
                            Log.e(logTag, message)
                        }
                    }
                    LogLevel.VERBOSE -> Log.v(logTag, message)
                }
            }
        }
    }
}




//
///**
// * Helper class to facilitate logging. To enable debug logging in production run `adb shell setprop
// * log.tag.FIAM.Headless DEBUG`
// *
// * @hide
// */
//object Logging {
//    @VisibleForTesting
//    val TAG = "FIAM.Headless"
//
//    /** Log a message if in debug mode or debug is loggable.  */
//    fun logd(message: String?) {
//        if (BuildConfig.DEBUG || Log.isLoggable(
//                com.google.firebase.inappmessaging.internal.Logging.TAG,
//                Log.DEBUG
//            )
//        ) {
//            Log.d(com.google.firebase.inappmessaging.internal.Logging.TAG, message!!)
//        }
//    }
//
//    /** Log info messages if they are loggable.  */
//    fun logi(message: String?) {
//        if (Log.isLoggable(com.google.firebase.inappmessaging.internal.Logging.TAG, Log.INFO)) {
//            Log.i(com.google.firebase.inappmessaging.internal.Logging.TAG, message!!)
//        }
//    }
//
//    /** Log error messages normally but add a consistent TAG  */
//    fun loge(message: String?) {
//        Log.e(com.google.firebase.inappmessaging.internal.Logging.TAG, message!!)
//    }
//
//    /** Log warning messages normally but add a consistent TAG  */
//    fun logw(message: String?) {
//        Log.w(com.google.firebase.inappmessaging.internal.Logging.TAG, message!!)
//    }
//}
