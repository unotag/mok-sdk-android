package one.mok

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.unotag.mokone.utils.MokLogger
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
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

        getFcmToken()
        pushNotificationPermissionHandler = PushNotificationPermissionHandler(
            applicationContext, this
        )

        binding.notificationPermissionBtn.setOnClickListener {
            pushNotificationPermissionHandler.requestPermission()
        }

        binding.openNotificationSettingsBtn.setOnClickListener {
            pushNotificationPermissionHandler.openNotificationSettings()
        }

        binding.fcmTokenValue.setOnClickListener {
            copyTextToClipboard(binding.fcmTokenValue)
        }
    }

    private fun copyTextToClipboard(textToCopy: TextView) {
        val text = textToCopy.text.toString()
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Copied Text", text)
        clipboardManager.setPrimaryClip(clipData)
        showToast("Text copied to clipboard")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        setNotificationPermissionStatus()
    }


    private fun setNotificationPermissionStatus() {
        var isGranted = pushNotificationPermissionHandler.isNotificationPermissionGranted()
        binding.notificationSettingsStatus.text =
            if (isGranted) "Granted" else "Denied"

        var textColor =
            if (isGranted) resources.getColor(R.color.colorSuccess) else resources.getColor(R.color.colorError)

        binding.notificationSettingsStatus.setTextColor(textColor)
    }

    private fun getFcmToken() {
        val mokSDK = MokSDK.getInstance(applicationContext)
        mokSDK.getFCMToken { token, error ->
            if (token != null) {
                binding.fcmTokenValue.text = token

                // mokSDK.triggerWorkflow("63185f8b-5390-4091-a4ef-a31fab165dae", jsonBody)
                //-   updateUser(token)
            } else {
                binding.fcmTokenValue.text = error

            }
        }
    }

    private fun updateUser(fcmToken: String?) {
        val mokSDK = MokSDK.getInstance(applicationContext)
        val jsonBody = JSONObject()
        jsonBody.put("name", "rra_test_user")
        jsonBody.put("fcm_registration_token", fcmToken ?: "N/A")
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