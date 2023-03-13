package kr.hs.dgsw.smartschool.dodamdodam.dauth.request

import com.google.gson.annotations.SerializedName

data class TokenRequest(
    @field:SerializedName("code")
    val code: String,
    @field:SerializedName("client_id")
    val clientId: String,
    @field:SerializedName("client_secret")
    val clientSecret: String
)
