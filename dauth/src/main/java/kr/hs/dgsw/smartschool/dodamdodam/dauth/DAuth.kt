package kr.hs.dgsw.smartschool.dodamdodam.dauth

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import kr.hs.dgsw.smartschool.dodamdodam.dauth.request.LoginRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object DAuth {
    private const val DODAM_PACKAGE = "kr.hs.dgsw.smartschool.dodamdodam"
    private const val DAUTH_URL = "https://dauth.b1nd.com/api/"
    private const val BASE_URL = "https://dodam.b1nd.com/api/"

    private var isInstalled = false

    private val okHttpClient = OkHttpClient().newBuilder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()


    private val dAuthRetrofit = Retrofit.Builder()
        .baseUrl(DAUTH_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()

    private val dAuth = dAuthRetrofit.create(DAuthService::class.java)

    private fun login(loginRequest: LoginRequest) = kotlin.runCatching {
        CoroutineScope(Dispatchers.IO).async {
            dAuth.login(loginRequest)
        }
    }

    fun ComponentActivity.settingForDodam(
        clientId: String,
        clientSecret: String,
        redirectUrl: String,
    ): ActivityResultLauncher<Intent> {
        val intent = packageManager.getLaunchIntentForPackage(DODAM_PACKAGE)

        isInstalled = intent != null

        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == 200) {
                val id = result.data?.getStringExtra("id") ?: ""
                val pw = result.data?.getStringExtra("pw") ?: ""

                CoroutineScope(Dispatchers.IO).launch {
                    login(LoginRequest(id, pw, clientId, clientSecret))
                        .onSuccess {
                            it.await().data.location
                        }
                        .onFailure {

                        }
                }
            }
        }
    }

    fun Context.loginForDodam(
        register: ActivityResultLauncher<Intent>,
//        onSuccess: (TokenResponse) -> Unit,
//        onFailure: (Throwable) -> Unit,
    ) {

    }

}
