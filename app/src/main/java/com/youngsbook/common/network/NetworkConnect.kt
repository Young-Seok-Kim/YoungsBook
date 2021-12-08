package com.youngsbook.common.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.Define
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkConnect {
    val gson = GsonBuilder().setLenient().create()
    public fun connectNetwork(path : String, param : HashMap<String, Any>
                              , onSuccess : () -> Unit // 성공했을때 실행할 함수(이벤트)
                              , contextForConnect : Context // 실패했을때 토스트메시지를 띄워주기 위한 컨텍스트
    ){
        var mRetrofit = Retrofit.Builder().baseUrl(Define.URL_DEFAULT).addConverterFactory(GsonConverterFactory.create(gson)).build()

        val server : RetrofitService = mRetrofit.create(RetrofitService::class.java)

        // 호출하는 곳에서 매개변수를 HashMap 형태로 보내는 방식

        server.connectRequest(path, param).enqueue(object : Callback<ResponseDTO>{
            override fun onResponse(call: Call<ResponseDTO>?, response: Response<ResponseDTO>?) {
                onSuccess()
                Log.d("Retrofit", "$path 연결성공")
            }
            override fun onFailure(call: Call<ResponseDTO>?, t: Throwable?) {
//                onFail()
                 Toast.makeText(contextForConnect,"서버와 연결을 시도했으나 실패했습니다.",Toast.LENGTH_SHORT).show()

                Log.e("Retrofit", "$path 연결실패")
            }
        })

    }
}