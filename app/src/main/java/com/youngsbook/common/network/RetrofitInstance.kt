package com.youngsbook.common.network

import com.youngsbook.common.Define
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    val gson = GsonBuilder().setLenient().create()
    val okHttpClient = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30,TimeUnit.SECONDS).writeTimeout(30,TimeUnit.SECONDS).build() // n초동안 기다리도록 만드는 변수
    val RETROFIT = Retrofit.Builder().baseUrl(Define.BASE_URL).client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).build() // BASE_URL에 접근하기 위한 변수
    val SERVER_HTTP : RetrofitService = RetrofitInstance.RETROFIT.create(RetrofitService::class.java) // RetrofitService에 만든 서비스를 사용하기 위한 변수

//    private val RETROFIT_HTTPS = Retrofit.Builder()
//        .baseUrl(Define.BASE_URL_HTTPS)
//        .addConverterFactory(GsonConverterFactory.create(gson))
//        .build()
//
//    val SERVER_HTTPS = RETROFIT_HTTPS.create(RetrofitService::class.java)

}