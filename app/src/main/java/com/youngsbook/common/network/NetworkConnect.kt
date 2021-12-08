package com.youngsbook.common.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.youngsbook.YoungsFunction
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object NetworkConnect {

    fun connectNetwork(path : String, param : JsonObject
                              , contextForConnect : Context // 실패했을때 토스트메시지를 띄워주기 위한 컨텍스트
                              , onSuccess : () -> Unit // 성공했을때 실행할 함수(이벤트)

    ){
        // 호출하는 곳에서 매개변수를 HashMap 형태로 보내는 방식
        RetrofitInstance.SERVER.connectRequest(path, param).enqueue(object : Callback<ResponseDTO>{
            override fun onResponse(call: Call<ResponseDTO>?, response: Response<ResponseDTO>?) {

                Log.d("Retrofit", "$path 요청성공")

                if(response!!.isSuccessful) {
                    onSuccess()
                }
                else {
                    Toast.makeText(contextForConnect, "서버와 연결을 시도했으나 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResponseDTO>?, t: Throwable?) {
                Log.d("Retrofit", "$path 요청실패")
            }
        })
    }
}