package kr.hs.dgsw.smartschool.dodamdodam.dauth.request

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @field:SerializedName("id")
    val id: String,
    @field:SerializedName("pw")
    val pw: String,
    @field:SerializedName("clientId")
    val clientId: String,
    @field:SerializedName("redirectUrl")
    val redirectUrl: String
)
