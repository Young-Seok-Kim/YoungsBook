package com.youngsbook.common.network

import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.gson.JsonObject
import com.youngsbook.common.YoungsFunction
import com.youngsbook.ui.main.MainActivity
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object NetworkConnect {
    private var progressDialog: ProgressDialog? = null
    var resultString : String = ""

    suspend fun connectHTTP(path : String, param : JsonObject
                            , context : Context // 실패했을때 토스트메시지를 띄워주기 위한 컨텍스트
                            , onSuccess : () -> Unit // 성공했을때 실행할 함수(이벤트)
    ){
        // 호출하는 곳에서 매개변수를 HashMap 형태로 보내는 방식
        // 서버에서 보낸 JSON의 Key값을 가져옴
        RetrofitInstance.SERVER_HTTP.connectRequest(path, param).enqueue(object : Callback<ResponseDTO>{
            override fun onResponse(call: Call<ResponseDTO>?, response: Response<ResponseDTO>?) {

                Log.d("Retrofit", "$path 요청성공")

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
                Log.d("Retrofit", "$path 요청실패")
            }
        })
    }

    suspend fun connectHTTPS(path : String, param : JsonObject
                             , context : Context // 실패했을때 토스트메시지를 띄워주기 위한 컨텍스트
                             , onSuccess : () -> Unit // 성공했을때 실행할 함수(이벤트)
    ){
        RetrofitInstance.SERVER_HTTPS.connectRequest(path, param).enqueue(object : Callback<ResponseDTO>{
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
    // HTTP 통신
    ///////////////////////////////////////////////
    // HTTPS 통신

    fun loadBaeminNotice(page: Int) {
        val call = BaeminClient.service.loadNotice(page.toString())

        call.enqueue(object : Callback<Baemin> {
            override fun onResponse( // 통신에 성공한 경우
                call: Call<Baemin>,
                response: Response<Baemin>
            ) {
                if(response.isSuccessful()){ // 응답을 잘 받은 경우
                    Log.d("배민",response.code().toString())
                } else {
                    Log.d("배민",response.code().toString())
                    // 통신은 성공했지만 응답에 문제가 있는 경우
                }
            }

            override fun onFailure(call: Call<Baemin>, t: Throwable) {
                Log.d("배민",t.message.toString())
                // 통신에 실패한 경우
            }
        })
    }


}