import com.unotag.mokone.core.MokSDKConstants
import com.unotag.mokone.utils.MokLogger
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MokApiCallTask() {

//    sealed class ApiResult {
//        data class Success(val response: JSONObject) : ApiResult()
//        data class Error(val exception: Exception) : ApiResult()
//    }

    abstract class ApiResult {
        class Success(val response: JSONObject) : ApiResult()
        class Error(val exception: Exception) : ApiResult()
    }

    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    enum class HttpMethod {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH
    }

    enum class MokRequestMethod {
        READ,
        WRITE
    }

    fun performApiCall(
        urlString: String,
        httpMethod: HttpMethod,
        mokRequestMethod: MokRequestMethod,
        requestBody: JSONObject? = null,
        callback: (ApiResult) -> Unit
    ) {
        executorService.submit {
            MokLogger.log(
                MokLogger.LogLevel.DEBUG,
                "IS_PRODUCTION_ENV : ${MokSDKConstants.IS_PRODUCTION_ENV}"
            )
            MokLogger.log(MokLogger.LogLevel.DEBUG, "url : $urlString")
            MokLogger.log(MokLogger.LogLevel.DEBUG, "httpMethod : $httpMethod")
            MokLogger.log(MokLogger.LogLevel.DEBUG, "mokRequestMethod type : $mokRequestMethod")
            MokLogger.log(MokLogger.LogLevel.DEBUG, "requestBody : ${requestBody.toString()}")
            try {
                val response = makeApiCall(urlString, httpMethod, mokRequestMethod, requestBody)
                MokLogger.log(MokLogger.LogLevel.DEBUG, "response : $response")
                callback(ApiResult.Success(response))
            } catch (e: Exception) {
                MokLogger.log(MokLogger.LogLevel.ERROR, "performApiCall error : $e")
                callback(ApiResult.Error(e))
            }
        }
    }

    private fun makeApiCall(
        urlString: String,
        httpMethod: HttpMethod,
        mokRequestMethod: MokRequestMethod,
        requestBody: JSONObject?
    ): JSONObject {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = httpMethod.name
        connection.setRequestProperty("Content-Type", "application/json")

        MokLogger.log(MokLogger.LogLevel.DEBUG, "Read key value: ${MokSDKConstants.READ_KEY}")
        MokLogger.log(MokLogger.LogLevel.DEBUG, "Write key value: ${MokSDKConstants.WRITE_KEY}")

        if (mokRequestMethod == MokRequestMethod.READ) {
            connection.setRequestProperty("Authorization", MokSDKConstants.READ_KEY)
        } else {
            connection.setRequestProperty("Authorization", MokSDKConstants.WRITE_KEY)
        }

        if (requestBody != null) {
            val requestBodyBytes = requestBody.toString().toByteArray(StandardCharsets.UTF_8)
            connection.doOutput = true
            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.write(requestBodyBytes)
            outputStream.flush()
            outputStream.close()
        }

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readLine() // Modify this based on your response parsing logic
            reader.close()
            return JSONObject(response)
        } else {
            throw Exception("API call failed with response code: $responseCode")
        }
    }
}
