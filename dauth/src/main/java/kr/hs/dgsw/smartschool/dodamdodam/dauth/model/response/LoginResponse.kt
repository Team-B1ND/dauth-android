package kr.hs.dgsw.smartschool.dodamdodam.dauth.model.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @field:SerializedName("location")
    val location: String
)
