package kr.hs.dgsw.smartschool.dodamdodam.dauth.model.response

import com.google.gson.annotations.SerializedName
import kr.hs.dgsw.smartschool.dodamdodam.dauth.model.Role

data class UserResponse(
    @field:SerializedName("uniqueId")
    val uniqueId: String,
    @field:SerializedName("grade")
    val grade: Int,
    @field:SerializedName("room")
    val room: Int,
    @field:SerializedName("number")
    val number: Int,
    @field:SerializedName("profileImage")
    val profileImage: String,
    @field:SerializedName("role")
    val role: Role,
    @field:SerializedName("email")
    val email: String,
)
