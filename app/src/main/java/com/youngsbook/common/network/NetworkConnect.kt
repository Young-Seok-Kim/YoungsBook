package com.youngsbook.common.network

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object NetworkConnect {
    private var progressDialog: ProgressDialog? = null

    suspend fun connectNetwork(path : String, param : JsonObject
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

    fun startProgress(context: Context) // 서버와 통신하는동안 터치할수 없도록 하는 코드
    {
        progressDialog = ProgressDialog(context)


        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setMessage("서버와 연결중")

        progressDialog!!.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        progressDialog!!.show()

    }

    fun endProgress() // 터치 할수 없도록 한 세팅을 다시 터치가능하도록 세팅
    {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            progressDialog!!.dismiss()
            progressDialog = null
        }
    }
}