package com.youngsbook.common.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.GET
import java.util.*


interface RetrofitService {
    //post1
    // 매개변수를 미리 정해두는 방식
    @FormUrlEncoded
    @POST("/test")
    fun postRequest(
        @Field("id") id: String,
        @Field("pw") pw: String
    ): Call<ResponseDTO>

    //post2
    // 호출하는 곳에서 매개변수를 HashMap 형태로 보내는 방식
    @POST("{path}")
    fun connectRequest(
        @Path("path") retrofitPath: String,
        @Body parameters: JsonObject
    ): Call<ResponseDTO>

//    @GET("/api/users?page=2")
//    fun getTest(): Call<Any>

    @GET("server/status/json?__ts=1640318537873") //
    fun loadNotice(@Query("page") page: String): Call<Baemin>

}