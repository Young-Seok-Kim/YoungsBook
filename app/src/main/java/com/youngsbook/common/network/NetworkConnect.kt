package com.youngsbook.common.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.JsonObject
import com.youngsbook.BuildConfig
import com.youngsbook.common.Define
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkConnect {
    var resultString : String = ""
    private var connectURL : String = Define.BASE_URL_HTTP_DEBUG

    /*
        값을 가져올때는 returnValue 를 한번 가져오고, 그후에 RESULT_LIST 안에 있는 값을 가져온다
        이렇게 만든 이유는 레트로핏으로 값을 별도의 변환 없이 가져오고, RESULT_LIST 안에 있는 select 한 값을 사용하기 위함이다.
     */

    fun connectHTTPS(path : String, param : JsonObject
                             , context : Context // 실패했을때 토스트메시지를 띄워주기 위한 컨텍스트
                             , onSuccess : () -> Unit // 성공했을때 실행할 함수(이벤트)
                             , onFailure : () -> Unit // 실패했을때 실행할 함수(이벤트)
    ){
        /*
            디버그 모드일때는 localhost 에 연결하고 (http),
            릴리즈 모드일때는 AWS 에 연결되도록 [awsyoungsbook.duckdns.org]에 연결하도록 한다.
            해당 코드를 수정하고 싶다면 SelfSigningHelper.kt 코드에있는 동일한 부분또한 수정해야한다.
         */

        connectURL = if (BuildConfig.DEBUG)
            Define.BASE_URL_HTTP_DEBUG
        else
            Define.BASE_URL_HTTPS_RELEASE

        val okHttpClient = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30,TimeUnit.SECONDS).writeTimeout(30,TimeUnit.SECONDS).build()

        val retrofit = if (BuildConfig.DEBUG) { // connectURL 에 접근하기 위한 변수
            Retrofit.Builder().baseUrl(connectURL).client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).build()
        } else{
            val selfSign = SelfSigningHelper(context).setSSLOkHttp(okHttpClient.newBuilder()).build()
            Retrofit.Builder().baseUrl(connectURL).client(selfSign).addConverterFactory(GsonConverterFactory.create()).build()
        }


        val retrofitService : RetrofitService = retrofit.create(RetrofitService::class.java) // RetrofitService 에 만든 서비스를 사용하기 위한 변수

        retrofitService.connectRequest(path, param).enqueue(object : Callback<ResponseDTO>{
            override fun onResponse(call: Call<ResponseDTO>?, response: Response<ResponseDTO>?) {

                Log.d("서버 연결 성공", response?.code().toString())
                Log.d("디버그 || 릴리즈", if(BuildConfig.DEBUG) "Debug" else "Release")

                if(response?.isSuccessful == true) {
                    resultString = response.body()?.returnValue.toString()
                    onSuccess()
                }
                else {
                    onFailure()
                    Toast.makeText(context, "서버와 연결을 시도했으나 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResponseDTO>?, t: Throwable?) {
                onFailure()
                Toast.makeText(context, "인터넷 연결을 확인하여주십시오.", Toast.LENGTH_SHORT).show()
                Log.d("인터넷 연결", t?.message.toString())
            }
        })
    }
}