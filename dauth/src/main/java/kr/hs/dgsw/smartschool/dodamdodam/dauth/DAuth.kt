package kr.hs.dgsw.smartschool.dodamdodam.dauth

import MutableEventFlow
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kr.hs.dgsw.smartschool.dodamdodam.dauth.request.LoginRequest
import kr.hs.dgsw.smartschool.dodamdodam.dauth.request.TokenRequest
import kr.hs.dgsw.smartschool.dodamdodam.dauth.response.TokenResponse
import kr.hs.dgsw.smartschool.dodamdodam.dauth.util.Constants
import kr.hs.dgsw.smartschool.dodamdodam.dauth.util.repeatOnStarted
import org.json.JSONObject
import retrofit2.Response

object DAuth {

    private var isInstalled: Boolean = false
    private val intent =
        Intent().setComponent(ComponentName(Constants.DODAM_PACKAGE, Constants.ACTIVITY_URL))

    private val eventFlow = MutableEventFlow<Event>()

//    private var account = MutableSharedFlow<Pair<String, String>>()

    //    private val token = MutableLiveData<TokenResponse>()
//    private val code = MutableLiveData<String>()
    private val error = MutableLiveData<Throwable>()

    private lateinit var clientId: String
    private lateinit var clientSecret: String
    private lateinit var redirectUrl: String

    private lateinit var register: ActivityResultLauncher<Intent>

    private fun <T> checkError(response: Response<T>): T {
        if (response.isSuccessful.not()) {
            val errorBody = JSONObject(response.errorBody()!!.string())
            throw Throwable(errorBody.getString("message"))
        }
        return response.body()!!
    }

    private fun login(loginRequest: LoginRequest) = CoroutineScope(Dispatchers.IO).launch {
        kotlin.runCatching {
            checkError(RetrofitClient.dAuth.login(loginRequest))
        }.onSuccess {
            event(Event.SuccessLoginEvent(it.data.location.split("=", "&")[1]))
        }.onFailure {
            event(Event.FailureEvent(it))
        }
    }

    private fun getToken(tokenRequest: TokenRequest) = CoroutineScope(Dispatchers.IO).launch {
        kotlin.runCatching {
            checkError(RetrofitClient.dAuth.getToken(tokenRequest))
        }.onSuccess {
            event(Event.SuccessTokenEvent(it))
        }.onFailure {
            event(Event.FailureEvent(it))
        }
    }

//    private suspend fun getRefreshToken(refreshTokenRequest: RefreshTokenRequest) = kotlin.runCatching {
//        checkError(dAuth.getRefreshToken(refreshTokenRequest))
//    }

    private fun lunch(
        context: Context,
        action: () -> Unit,
    ) {
        if (isInstalled) {
            register.launch(intent)
            action.invoke()
        } else {
            error.postValue(Throwable("도담도담을 설치해주세요"))

            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${Constants.DODAM_PACKAGE}")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${Constants.DODAM_PACKAGE}")
                    )
                )
            }
        }
    }

    fun ComponentActivity.settingDAuth(
        clientId: String,
        clientSecret: String,
        redirectUrl: String,
    ) {
        this@DAuth.clientId = clientId
        this@DAuth.clientSecret = clientSecret
        this@DAuth.redirectUrl = redirectUrl

        isInstalled = packageManager.getLaunchIntentForPackage(Constants.DODAM_PACKAGE) != null

        register =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    event(
                        Event.SuccessAccountEvent(
                            data?.getStringExtra("id") ?: "",
                            data?.getStringExtra("pw") ?: ""
                        )
                    )
                }
            }
    }

    fun loginWithDodam(
        context: Context,
        onSuccess: (TokenResponse) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = lunch(context) {
        (context as LifecycleOwner).repeatOnStarted {
            eventFlow.collect { event ->
                when (event) {
                    is Event.SuccessAccountEvent -> login(LoginRequest(event.id, event.pw, clientId, redirectUrl))
                    is Event.SuccessLoginEvent -> getToken(TokenRequest(event.code, clientId, clientSecret))
                    is Event.SuccessTokenEvent -> onSuccess(event.token)
                    is Event.FailureEvent -> onFailure(event.exception)
                }
            }
        }
    }

    fun getCode(
        context: Context,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = lunch(context) {
        (context as LifecycleOwner).repeatOnStarted {
            eventFlow.collect { event ->
                when (event) {
                    is Event.SuccessAccountEvent -> login(LoginRequest(event.id, event.pw, clientId, redirectUrl))
                    is Event.SuccessLoginEvent -> onSuccess(event.code)
                    is Event.FailureEvent -> onFailure(event.exception)
                    else -> {}
                }
            }
        }
    }

    private fun event(event: Event) {
        CoroutineScope(Dispatchers.IO).launch {
            eventFlow.emit(event)
        }
    }

    sealed class Event {
        data class SuccessAccountEvent(val id: String, val pw: String) : Event()
        data class SuccessLoginEvent(val code: String) : Event()
        data class SuccessTokenEvent(val token: TokenResponse) : Event()
        data class FailureEvent(val exception: Throwable) : Event()
    }
}