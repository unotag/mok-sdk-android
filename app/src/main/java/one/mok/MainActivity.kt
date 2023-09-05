package one.mok

import com.unotag.mokone.utils.MokLogger
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.unotag.mokone.MokSDK
import com.unotag.mokone.pushNotification.fcm.PushNotificationPermissionHandler
import one.mok.databinding.ActivityMainBinding
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pushNotificationPermissionHandler: PushNotificationPermissionHandler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        pushNotificationPermissionHandler = PushNotificationPermissionHandler(this)

        binding.notificationPermissionBtn.setOnClickListener {
            // Request the permission using the initialized requestPermissionLauncher
            if (Build.VERSION.SDK_INT >= 33) {
                pushNotificationPermissionHandler.requestPermission()
            } else {

            }
        }

//        binding.notificationPermissionBtn.setOnClickListener {
//        }

    }

    private fun getFcmToken(){
        val mokSDK = MokSDK.getInstance(applicationContext)
        mokSDK.getFCMToken { token ->
            // Use the token here
            if (token != null) {
                // Token retrieval successful, do something with the token
              //  return token

                // mokSDK.triggerWorkflow("63185f8b-5390-4091-a4ef-a31fab165dae", jsonBody)

            } else {
                // Token retrieval failed
                // Handle the failure case
            }
        }
    }

    private fun updateUser() {
        val mokSDK = MokSDK.getInstance(applicationContext)
        val jsonBody = JSONObject()
        jsonBody.put("name", "rra_test_user")
        jsonBody.put("fcm", "N/A")
        mokSDK.updateUser("TEST_RR_KABLE_001", jsonBody) { success, errorMessage ->
            if (success != null) {
                // API call was successful, do something
                MokLogger.log(MokLogger.LogLevel.DEBUG, "update user result : $success")
            } else {
                // API call failed, handle the error
                if (errorMessage != null) {
                    // Display or log the error message
                    MokLogger.log(MokLogger.LogLevel.ERROR, "update user result : $errorMessage")
                }
            }
        }
    }
}