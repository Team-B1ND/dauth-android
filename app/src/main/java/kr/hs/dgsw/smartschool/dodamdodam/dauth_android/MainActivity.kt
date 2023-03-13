package kr.hs.dgsw.smartschool.dodamdodam.dauth_android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import kr.hs.dgsw.smartschool.dodamdodam.dauth.DAuth.loginForDodam
import kr.hs.dgsw.smartschool.dodamdodam.dauth.DAuth.settingForDodam

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val clientId = "4e569073e96543aa8f1a9d8c681d30662467e67fbe4e4bbfa28fddfc28daf1b7"
        val redirectUrl = "http://localhost:8000/redirect"
        val clientSecret = "17912364762a4a1bb9d557d19b914f90cca92c4722ea45538f84f84ab95f91ba"
        val register = settingForDodam(clientId, clientSecret, redirectUrl)

        findViewById<Button>(R.id.btn_login).setOnClickListener {
            loginForDodam(register, {
                Toast.makeText(this, it.accessToken, Toast.LENGTH_SHORT).show()
            }, {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            })
        }
    }
}