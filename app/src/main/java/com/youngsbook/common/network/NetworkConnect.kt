package com.youngsbook.common.network

import android.util.Log
import com.google.gson.GsonBuilder
import com.youngsbook.data.Result
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkConnect {
    val gson = GsonBuilder().setLenient().create()
    public fun connectNetwork(path : String, param : HashMap<String, Any>
                              , onSuccess : () -> Unit // 성공했을때 실행할 함수(이벤트)
                              , onFail : () -> Unit // 실패했을때 실행할 함수(이벤트)
    ){
        var mRetrofit = Retrofit.Builder().baseUrl("http://10.0.2.2:8080/YoungsBook/").addConverterFactory(GsonConverterFactory.create(gson)).build()

        val server : RetrofitService = mRetrofit.create(RetrofitService::class.java)

        //post2
// 호출하는 곳에서 매개변수를 HashMap 형태로 보내는 방식

//        var params:HashMap<String, Any> = HashMap<String, Any>()
//        params.put("id", "hello")
//        params.put("pw", 123123)

        server.connectRequest(path, param).enqueue(object : Callback<ResponseDTO>{
            override fun onResponse(call: Call<ResponseDTO>?, response: Response<ResponseDTO>?) {
                onSuccess()
                Log.d("Retrofit", "$path 연결성공")
            }
            override fun onFailure(call: Call<ResponseDTO>?, t: Throwable?) {
                onFail()
                Log.e("Retrofit", "$path 연결실패")
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

    }
}