package com.youngsbook.common.network

import android.util.Log
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkConnect {
    val gson = GsonBuilder().setLenient().create()
    public fun setRetrofitInit(){
        var mRetrofit = Retrofit.Builder().baseUrl("http://10.0.2.2:8080/YoungsBook/").addConverterFactory(GsonConverterFactory.create(
            gson
        )).build()

        val server : RetrofitService = mRetrofit.create(RetrofitService::class.java)

        //post2
// 호출하는 곳에서 매개변수를 HashMap 형태로 보내는 방식

        var params:HashMap<String, Any> = HashMap<String, Any>()
        params.put("id", "hello")
        params.put("pw", 123123)

        server.testRequest("connectTest.do", params).enqueue(object : Callback<ResponseDTO>{
            override fun onFailure(call: Call<ResponseDTO>?, t: Throwable?) {
                Log.e("Retrofit", t.toString())
            }

            override fun onResponse(call: Call<ResponseDTO>?, response: Response<ResponseDTO>?) {
                var res: ResponseDTO? = response?.body()
//                Log.d("Retrofit", res?.result)
            }
        })

        //post1
        // 매개변수를 미리 정해두는 방식
//        server.postRequest("userid","asd1234").enqueue(object : Callback<ResponseDTO> {
//            override fun onFailure(call: Call<ResponseDTO>?, t: Throwable?) {
//                Log.e("retrofit", t.toString())
//            }
//
//            override fun onResponse(call: Call<ResponseDTO>?, response: Response<ResponseDTO>?) {
//                Log.d("retrofit", response?.body().toString())
//            }
//        })


//        System.out.println(mRetrofit)
//        Log.d("mRetrofit 확인", mRetrofit.toString())
    }
}