package kr.hs.dgsw.smartschool.dodamdodam.dauth.response

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @field:SerializedName("access_token")
    val accessToken: String,
    @field:SerializedName("refresh_token")
    val refreshToken: String
)
