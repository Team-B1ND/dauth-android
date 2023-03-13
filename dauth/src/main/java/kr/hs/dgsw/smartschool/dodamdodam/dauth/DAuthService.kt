package kr.hs.dgsw.smartschool.dodamdodam.dauth

import kr.hs.dgsw.smartschool.dodamdodam.dauth.request.LoginRequest
import kr.hs.dgsw.smartschool.dodamdodam.dauth.response.LoginResponse
import kr.hs.dgsw.smartschool.dodamdodam.dauth.response.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DAuthService {
    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>
}
