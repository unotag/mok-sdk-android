package one.mok

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.unotag.mokone.MokSDK
import one.mok.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    private lateinit var mActivity: Activity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Activity) {
            mActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val bundle: Bundle? = MokSDK.getApiKeyFromManifest(mActivity)
//        if (bundle != null) {
//            val readKey = bundle.getString("MOK_READ_KEY")
//            val writeKey = bundle.getString("MOK_WRITE_KEY")
//            if (readKey == null || writeKey == null) {
//                MokLogger.log(MokLogger.LogLevel.ERROR, "READ/WRITE key is missing")
//            } else {
//                binding.readKeyEt.setText(readKey)
//                binding.writeKeyEt.setText(writeKey)
//            }
//        } else {
//            MokLogger.log(MokLogger.LogLevel.ERROR, "Manifest meta is null")
//        }


        binding.hostUrlTv.text = "Host URL: " + MokSDK.getHostUrl()


        binding.changeEnvTb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                MokSDK.updateEnv(isChecked)

            } else {
                MokSDK.updateEnv(isChecked)
            }
            binding.hostUrlTv.text = "Host URL: " + MokSDK.getHostUrl()
        }

        binding.updateKeysBtn.setOnClickListener {
            var readKey = binding.readKeyEt.text.toString()
            var writeKey = binding.writeKeyEt.text.toString()
            if (readKey.isBlank() || writeKey.isBlank()) {
                showToast("read or write keys cannot be empty")
            } else {
                MokSDK.updateApiKeys(readKey, writeKey)
                showToast("keys updated successfully")

            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show()
    }


}