package com.youngsbook.common.network

import com.Define
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val RETROFIT = Retrofit.Builder().baseUrl(Define.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build() // BASE_URL에 접근하기 위한 변수
    val SERVER : RetrofitService = RetrofitInstance.RETROFIT.create(RetrofitService::class.java) // RetrofitService에 만든 서비스를 사용하기 위한 변수
}