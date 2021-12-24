package com.youngsbook.common.network

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BaeminClient {
    private const val baseUrl = "https://ceo.baemin.com/"
    val gson = GsonBuilder().setLenient().create()
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val service = retrofit.create(RetrofitService::class.java)!!
}