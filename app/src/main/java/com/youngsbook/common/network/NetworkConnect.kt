package com.youngsbook.common.network

import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
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
    private var progressDialog: ProgressDialog? = null
    var resultString : String = ""
    var connectURL : String = Define.BASE_URL_HTTPS_DEBUG

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun connectHTTPS(path : String, param : JsonObject
                             , context : Context // 실패했을때 토스트메시지를 띄워주기 위한 컨텍스트
                             , onSuccess : () -> Unit // 성공했을때 실행할 함수(이벤트)
    ){
        /*
            디버그 모드일때는 로컬에 연결되도록 youngsbook.duckdns.org에 연결하고,
            릴리즈 모드일때는 AWS에 연결되도록 awsyoungsbook.duckdns.org에 연결하도록 한다.
            해당 코드를 수정하고 싶다면 SelfSigningHelper.kt 코드에있는 동일한 부분또한 수정해야한다.
         */
        if (BuildConfig.DEBUG)
            connectURL = Define.BASE_URL_HTTPS_DEBUG
        else
            connectURL = Define.BASE_URL_HTTPS_RELEASE

        val okHttpClient = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build() // n초동안 기다리도록 만드는 변수
        val selfSign = SelfSigningHelper(context).setSSLOkHttp(okHttpClient.newBuilder()).build()
        val RETROFIT = Retrofit.Builder().baseUrl(connectURL).client(selfSign).addConverterFactory(GsonConverterFactory.create()).build()
        val SERVER : RetrofitService = RETROFIT.create(RetrofitService::class.java) // RetrofitService에 만든 서비스를 사용하기 위한 변수

        SERVER.connectRequest(path, param).enqueue(object : Callback<ResponseDTO>{
            override fun onResponse(call: Call<ResponseDTO>?, response: Response<ResponseDTO>?) {

                Log.d("HTTPS", response?.code().toString())

                if(response?.isSuccessful ?: false) {
                    resultString = response?.body()?.returnValue.toString()
                    onSuccess()

                }
                else {
                    Toast.makeText(context, "서버와 연결을 시도했으나 실패했습니다.", Toast.LENGTH_SHORT).show()
                }

                NetworkConnect.endProgress()

            }
            override fun onFailure(call: Call<ResponseDTO>?, t: Throwable?) {
                NetworkConnect.endProgress()
                Toast.makeText(context, "인터넷 연결을 확인하여주십시오.", Toast.LENGTH_SHORT).show()
                Log.d("HTTPS", t?.message.toString())

            }
        })
    }

    fun startProgress(context: Context) // 서버와 통신하는동안 터치할수 없도록 하는 코드
    {
        if(!(progressDialog?.isShowing == false)) {
            progressDialog = ProgressDialog(context)


            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog!!.setMessage("서버와 연결중")

            progressDialog?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            progressDialog!!.show()
        }

    }

    fun endProgress()
    {
        progressDialog?.dismiss()
    }



    //    suspend fun connectHTTP(path : String, param : JsonObject
//                            , context : Context // 실패했을때 토스트메시지를 띄워주기 위한 컨텍스트
//                            , onSuccess : () -> Unit // 성공했을때 실행할 함수(이벤트)
//    ){
//        // 호출하는 곳에서 매개변수를 HashMap 형태로 보내는 방식
//        // 서버에서 보낸 JSON의 Key값을 가져옴
//        RetrofitInstance.SERVER_HTTP.connectRequest(path, param).enqueue(object : Callback<ResponseDTO>{
//            override fun onResponse(call: Call<ResponseDTO>?, response: Response<ResponseDTO>?) {
//
//                Log.d("Retrofit", "$path 요청성공")
//
//                if(response!!.isSuccessful) {
//                    resultString = response.body()?.returnValue.toString()
//                    onSuccess()
//
//                }
//                else {
//                    Toast.makeText(context, "서버와 연결을 시도했으나 실패했습니다.", Toast.LENGTH_SHORT).show()
//                }
//
//                NetworkConnect.endProgress()
//
//            }
//            override fun onFailure(call: Call<ResponseDTO>?, t: Throwable?) {
//                NetworkConnect.endProgress()
//                Toast.makeText(context, "인터넷 연결을 확인하여주십시오.", Toast.LENGTH_SHORT).show()
//                Log.d("Retrofit", "$path 요청실패")
//            }
//        })
//    }

}