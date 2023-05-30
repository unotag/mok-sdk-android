import android.util.Log

class MokLogger {
    enum class LogLevel {
        DEBUG,
        ERROR
    }

    companion object {
        private const val TAG = "MokLogger"
        private var logLevel = LogLevel.DEBUG

        fun setLogLevel(level: LogLevel) {
            logLevel = level
        }

        fun log(level: LogLevel, message: String, throwable: Throwable? = null) {
            if (level >= logLevel) {
                when (level) {
                    LogLevel.DEBUG -> Log.d(TAG, message)
                    LogLevel.ERROR -> {
                        if (throwable != null) {
                            Log.e(TAG, message, throwable)
                        } else {
                            Log.e(TAG, message)
                        }
                    }
                }
            }
        }
    }
}
