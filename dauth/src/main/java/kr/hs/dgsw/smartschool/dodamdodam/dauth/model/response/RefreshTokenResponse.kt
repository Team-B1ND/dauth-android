package kr.hs.dgsw.smartschool.dodamdodam.dauth.model.response

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @field:SerializedName("access_token")
    val accessToken: String,
    @field:SerializedName("token_type")
    val tokenType: String,
    @field:SerializedName("expries_in")
    val expiresIn: String,
)
