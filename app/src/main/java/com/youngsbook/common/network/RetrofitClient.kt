package com.youngsbook.common.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.youngsbook.common.Define
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// HTTPS 통신용
class RetrofitClient {
    private fun getInstance() : Retrofit {
        val gson : Gson = GsonBuilder()
            .setLenient()
            .create();
        return Retrofit.Builder()
            .baseUrl("https://reqres.in/") // Define.BASE_URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    }
}