package one.mok

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.unotag.mokone.MokSDK
import com.unotag.mokone.pushNotification.fcm.PushNotificationPermissionCallback
import com.unotag.mokone.pushNotification.fcm.PushNotificationPermissionHandler
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    lateinit var mFcmToken : String

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {}

        // Example usage of requestPermission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PushNotificationPermissionHandler.requestPermission(
                this,
                object : PushNotificationPermissionCallback {
                    override fun onPermissionGranted() {
                        // Permission granted logic
                        // Handle the scenario when permission is granted
                    }

                    override fun onPermissionDenied() {
                        // Permission denied logic
                        // Handle the scenario when permission is denied
                    }
                },
                requestPermissionLauncher
            )
        }

        val mokSDK = MokSDK.getInstance(applicationContext)




        mokSDK.getFCMToken { token ->
            // Use the token here
            if (token != null) {
                // Token retrieval successful, do something with the token
                mFcmToken = token
                MokLogger.log(MokLogger.LogLevel.DEBUG, "token: $token")

                val jsonBody = JSONObject()
                jsonBody.put("name", "")
                jsonBody.put("fcm", mFcmToken)

                mokSDK.triggerWorkflow("63185f8b-5390-4091-a4ef-a31fab165dae", jsonBody)
            } else {
                // Token retrieval failed
                // Handle the failure case
            }
        }




    }
}