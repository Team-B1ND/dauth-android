package kr.hs.dgsw.smartschool.dodamdodam.dauth

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.hs.dgsw.smartschool.dodamdodam.dauth.DAuth.checkError
import kr.hs.dgsw.smartschool.dodamdodam.dauth.request.LoginRequest
import kr.hs.dgsw.smartschool.dodamdodam.dauth.request.TokenRequest
import kr.hs.dgsw.smartschool.dodamdodam.dauth.response.TokenResponse
import kr.hs.dgsw.smartschool.dodamdodam.dauth.service.DAuthService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object DAuth {
    private const val DODAM_PACKAGE = "kr.hs.dgsw.smartschool.dodamdodam"
    private const val ACTIVITY_URL =
        "kr.hs.dgsw.smartschool.dodamdodam.features.dauth.DAuthActivity"
    private const val DAUTH_URL = "https://dauth.b1nd.com/api/"
    private const val BASE_URL = "https://dodam.b1nd.com/api/"

    private var isInstalled = false
    private val token = MutableLiveData<TokenResponse>()
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

    private fun  <T> checkError(response: Response<T>): T {
        if (response.isSuccessful.not()) {
            val errorBody = JSONObject(response.errorBody()!!.string())
            throw Throwable(errorBody.getString("message"))
        }
        return response.body()!!
    }

    private suspend fun login(loginRequest: LoginRequest) = kotlin.runCatching {
        checkError(dAuth.login(loginRequest))
    }

    private suspend fun getToken(tokenRequest: TokenRequest) = kotlin.runCatching {
        checkError(dAuth.getToken(tokenRequest))
    }

    fun ComponentActivity.settingForDodam(
        clientId: String,
        clientSecret: String,
        redirectUrl: String,
    ): ActivityResultLauncher<Intent> {
        val intent = packageManager.getLaunchIntentForPackage(DODAM_PACKAGE)

        isInstalled = intent != null

        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val id = result.data?.getStringExtra("id") ?: ""
                val pw = result.data?.getStringExtra("pw") ?: ""

                CoroutineScope(Dispatchers.IO).launch {
                    login(LoginRequest(id, pw, clientId, redirectUrl))
                        .onSuccess {
                            val code = it.data.location.split("=", "&")[1]

                            getToken(TokenRequest(code, clientId, clientSecret))
                                .onSuccess { tokenValue ->
                                    token.postValue(tokenValue)
                                }.onFailure { tokenError ->
                                    error.postValue(tokenError)
                                }
                        }.onFailure {
                            error.postValue(Throwable(it))
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
        val intent = Intent()
        intent.component = ComponentName(DODAM_PACKAGE, ACTIVITY_URL)

        if (isInstalled) register.launch(intent)
        else {
            error.postValue(Throwable("도담도담을 설치해주세요"))

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

        token.observe(this as LifecycleOwner) {
            onSuccess(it)
        }
        error.observe(this as LifecycleOwner) {
            onFailure(Throwable(it.message!!.split(':').last()))
        }
    }
}
