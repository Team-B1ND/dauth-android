package kr.hs.dgsw.smartschool.dodamdodam.dauth.model.request

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(
    @field:SerializedName("refreshToken")
    val refreshToken: String,
    @field:SerializedName("clientId")
    val clientId: String,
)
