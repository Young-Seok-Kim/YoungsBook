package com.youngsbook.main

import retrofit2.Call
import retrofit2.http.*

interface RetrofitService {
    //post1
    // 매개변수를 미리 정해두는 방식
    @FormUrlEncoded
    @POST("/")
    fun postRequest(
        @Field("id") id: String,
        @Field("pw") pw: String
    ): Call<ResponseDTO>

    //post2
    // 호출하는 곳에서 매개변수를 HashMap 형태로 보내는 방식
    @POST("{path}")
    fun testRequest(
        @Path("path")path: String,
        @Body parameters: HashMap<String, Any>
    ): Call<ResponseDTO>
}