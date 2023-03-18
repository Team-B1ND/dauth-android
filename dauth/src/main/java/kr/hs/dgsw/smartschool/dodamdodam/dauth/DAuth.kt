package kr.hs.dgsw.smartschool.dodamdodam.dauth

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.hs.dgsw.smartschool.dodamdodam.dauth.request.LoginRequest
import kr.hs.dgsw.smartschool.dodamdodam.dauth.request.TokenRequest
import kr.hs.dgsw.smartschool.dodamdodam.dauth.response.TokenResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import java.util.concurrent.TimeUnit

object DAuth {
    private const val DODAM_PACKAGE = "kr.hs.dgsw.smartschool.dodamdodam"
    private const val ACTIVITY_URL =
        "kr.hs.dgsw.smartschool.dodamdodam.features.dauth.DAuthActivity"
    private const val DAUTH_URL = "https://dauth.b1nd.com/api/"
    private const val BASE_URL = "https://dodam.b1nd.com/api/"

    private var isInstalled = true
    private val tokenData = MutableLiveData<TokenResponse>()
    private val error = MutableLiveData<Throwable>()

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

    private suspend fun login(loginRequest: LoginRequest) =
        withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            kotlin.runCatching {
                dAuth.login(loginRequest)
            }
        }

    private suspend fun getToken(tokenRequest: TokenRequest) =
        withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            kotlin.runCatching {
                dAuth.getToken(tokenRequest)
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
                    login(LoginRequest(id, pw, clientId, redirectUrl))
                        .onSuccess {
                            val code = it.data.location.split("=", "&")[1]
                            getToken(TokenRequest(code, clientId, clientSecret))
                                .onSuccess { token ->
                                    tokenData.value = token.data
                                }.onFailure { tokenError ->
                                    error.value = Throwable(tokenError.message)
                                }
                        }.onFailure {
                            error.value = Throwable(it.message)
                        }
                }
            }
        }
    }

    fun Context.loginForDodam(
        register: ActivityResultLauncher<Intent>,
        onSuccess: (TokenResponse) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val component = ComponentName(DODAM_PACKAGE, ACTIVITY_URL)
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.component = component

        if (isInstalled) register.launch(intent)
        else {
            error.value = Throwable("도담도담을 설치해주세요")

            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$DODAM_PACKAGE")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$DODAM_PACKAGE")
                    )
                )
            }
        }

        tokenData.observe(this as LifecycleOwner) {
            onSuccess(it)
        }
        error.observe(this as LifecycleOwner) {
            onFailure(it)
        }
    }

}
