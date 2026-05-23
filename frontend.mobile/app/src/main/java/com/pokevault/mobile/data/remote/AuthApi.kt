package com.pokevault.mobile.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/google")
    suspend fun loginWithGoogle(@Body payload: GoogleLoginRequestDto): AuthResponseDto

    @POST("auth/logout")
    suspend fun logout()

    @GET("users/me")
    suspend fun me(): UserDto
}
