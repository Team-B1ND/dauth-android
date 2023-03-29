package kr.hs.dgsw.smartschool.dodamdodam.dauth_android

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kr.hs.dgsw.smartschool.dodamdodam.dauth.DAuth.getCode
import kr.hs.dgsw.smartschool.dodamdodam.dauth.DAuth.getUserInfo
import kr.hs.dgsw.smartschool.dodamdodam.dauth.DAuth.loginWithDodam
import kr.hs.dgsw.smartschool.dodamdodam.dauth.DAuth.settingDAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val clientId = "39bc523458c14eb987b7b16175426a31a9f105b7f5814f1f9eca7d454bd23c73"
        val redirectUrl = "http://localhost:3000/callback"
        val clientSecret = "e90b070b437f420eb788fad746e97a507984328ddf9142f481397ca6e7afda0e"

        settingDAuth(clientId, clientSecret, redirectUrl)

        findViewById<Button>(R.id.btn_login).setOnClickListener {
            loginWithDodam(this, {
                Toast.makeText(this, it.accessToken, Toast.LENGTH_SHORT).show()
            }, {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            })
        }
    }
}
