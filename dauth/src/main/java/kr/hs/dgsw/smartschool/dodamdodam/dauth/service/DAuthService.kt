package kr.hs.dgsw.smartschool.dodamdodam.dauth.service

import kr.hs.dgsw.smartschool.dodamdodam.dauth.model.request.LoginRequest
import kr.hs.dgsw.smartschool.dodamdodam.dauth.model.request.RefreshTokenRequest
import kr.hs.dgsw.smartschool.dodamdodam.dauth.model.request.TokenRequest
import kr.hs.dgsw.smartschool.dodamdodam.dauth.model.response.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface DAuthService {
    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<BaseResponse<LoginResponse>>

    @POST("token")
    suspend fun getToken(
        @Body tokenRequest: TokenRequest
    ): Response<TokenResponse>

    @POST("token/refresh")
    suspend fun getRefreshToken(
        @Body refreshTokenRequest: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    @GET("user")
    suspend fun getUserInfo(
        @Header("Authorization") token: String
    ): Response<BaseResponse<UserResponse>>
}
