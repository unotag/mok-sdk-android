package one.mok

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.unotag.mokone.MokSDK
import com.unotag.mokone.utils.MokLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import one.mok.databinding.FragmentDashboardBinding
import org.json.JSONObject


class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var mActivity: Activity
    private lateinit var mMokSDK: MokSDK

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Activity) {
            mActivity = context
        }
        mMokSDK = MokSDK.getInstance(mActivity.applicationContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.notificationPermissionBtn.setOnClickListener {
            mMokSDK.requestNotificationPermission(mActivity)
        }

        binding.openNotificationSettingsBtn.setOnClickListener {
            mMokSDK.openNotificationSettings(mActivity)
        }

        binding.fcmTokenValue.setOnClickListener {
            copyTextToClipboard(binding.fcmTokenValue)
        }

        binding.fetchFcmBtn.setOnClickListener {
            getFcmToken()
        }

        binding.updateFcmBtn.setOnClickListener {
            val userId = binding.userNameEt.text
            if (!userId.isNullOrBlank()) {
                updateFcmToken(userId.toString(), binding.fcmTokenValue.text.toString())
            } else {
                showToast("User ID cannot be blank")
            }
        }

        binding.updateUserIdBtn.setOnClickListener {
            val userId = binding.userNameEt.text
            if (!userId.isNullOrBlank()) {
                updateUser(userId.toString());
            } else {
                showToast("User ID cannot be blank")
            }
        }

        binding.logEventBtn.setOnClickListener {
            val userId = binding.userNameEt.text
            val eventName = binding.eventNameEt.text
            if (!userId.isNullOrBlank() && !eventName.isNullOrBlank()) {
                logEvent(userId.toString(), eventName.toString());
            } else {
                showToast("User ID or Event name cannot be blank")
            }
        }

        binding.showInAppMsgBtn.setOnClickListener {
            mMokSDK.readSavedInAppMessage()
        }

        binding.deleteInAppMsgBtn.setOnClickListener {
            mMokSDK.deleteAllInAppMessages()
        }

        binding.resetInAppMsgSeenStatusBtn.setOnClickListener {
            mMokSDK.resetIsSeenToUnSeen()
        }

        binding.fetchInAppMsgBtn.setOnClickListener {
            val userId = binding.userNameEt.text
            if (!userId.isNullOrBlank() ) {
                fetchInAppMessageData(userId.toString());
            } else {
                showToast("User ID or Event name cannot be blank")
            }
        }
    }

    private suspend fun setIAMCountToTextView() {
        val count = mMokSDK.getIAMCount()
        withContext(Dispatchers.Main) {
            binding.iamCountTv.text = count
        }
    }

    private fun copyTextToClipboard(textToCopy: TextView) {
        val text = textToCopy.text.toString()
        val clipboardManager =
            mActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Copied Text", text)
        clipboardManager.setPrimaryClip(clipData)
        showToast("Text copied to clipboard")
    }

    private fun showToast(message: String) {
        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        setNotificationPermissionStatus()
        lifecycleScope.launch {
            setIAMCountToTextView()
        }
    }


    private fun setNotificationPermissionStatus() {
        val isGranted = mMokSDK.isNotificationPermissionGranted(mActivity)
        binding.notificationSettingsStatus.text =
            if (isGranted) "Granted" else "Denied"

        val textColor =
            if (isGranted) resources.getColor(R.color.colorSuccess) else resources.getColor(R.color.colorError)
        binding.notificationSettingsStatus.setTextColor(textColor)
    }

    private fun getFcmToken() {
        val mokSDK = MokSDK.getInstance(mActivity.applicationContext)
        mokSDK.getFCMToken { token, error ->
            if (token != null) {
                binding.fcmTokenValue.text = token
            } else {
                binding.fcmTokenValue.text = error
            }
        }
    }

    private fun logEvent(userId: String, eventName: String) {
        val params = binding.eventParamsEt.text.toString()
        val mokSDK = MokSDK.getInstance(mActivity.applicationContext)

        if (params.isNotEmpty()) {
            val jsonBody = JSONObject(params)
            mokSDK.logActivity(eventName, userId, jsonBody) { success, errorMessage ->
                if (success != null) {
                    MokLogger.log(MokLogger.LogLevel.DEBUG, "update user result : $success")
                } else {
                    if (errorMessage != null) {
                        MokLogger.log(
                            MokLogger.LogLevel.ERROR,
                            "update user result : $errorMessage"
                        )
                    }
                }
            }
        } else {
            // Handle the case when params is empty (null or "")
            mokSDK.logActivity(eventName, userId) { success, errorMessage ->
                if (success != null) {
                    MokLogger.log(MokLogger.LogLevel.DEBUG, "update user result : $success")
                } else {
                    if (errorMessage != null) {
                        MokLogger.log(
                            MokLogger.LogLevel.ERROR,
                            "update user result : $errorMessage"
                        )
                    }
                }
            }
        }
    }


    private fun updateUser(userId: String) {
        val mokSDK = MokSDK.getInstance(mActivity.applicationContext)
        val jsonBody = JSONObject()
        jsonBody.put("name", userId + "_ANDROID_SKD")
        mokSDK.updateUser(userId, jsonBody) { success, errorMessage ->
            if (success != null) {
                MokLogger.log(MokLogger.LogLevel.DEBUG, "update user result : $success")
            } else {
                if (errorMessage != null) {
                    MokLogger.log(MokLogger.LogLevel.ERROR, "update user result : $errorMessage")
                }
            }
        }
    }

    private fun fetchInAppMessageData(userId: String) {
        val mokSDK = MokSDK.getInstance(mActivity.applicationContext)
        mokSDK.requestInAppMessageDataFromServer(userId) { success, errorMessage ->
            if (success != null) {
                MokLogger.log(MokLogger.LogLevel.DEBUG, "IAM fetched : $success")
            } else {
                if (errorMessage != null) {
                    MokLogger.log(MokLogger.LogLevel.ERROR, "IAM fetch error : $errorMessage")
                }
            }
        }
    }

    private fun updateFcmToken(userId: String, fcmToken: String?) {
        val mokSDK = MokSDK.getInstance(mActivity.applicationContext)
        val jsonBody = JSONObject()
        jsonBody.put("fcm_registration_token", fcmToken ?: "N/A")
        mokSDK.updateUser(userId, jsonBody) { success, errorMessage ->
            if (success != null) {
                MokLogger.log(MokLogger.LogLevel.DEBUG, "update user result : $success")
            } else {
                if (errorMessage != null) {
                    MokLogger.log(MokLogger.LogLevel.ERROR, "update user result : $errorMessage")
                }
            }
        }
    }
}
