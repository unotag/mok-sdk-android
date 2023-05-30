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

class MainActivity : AppCompatActivity() {

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
        mokSDK.updateUser()
    }


}