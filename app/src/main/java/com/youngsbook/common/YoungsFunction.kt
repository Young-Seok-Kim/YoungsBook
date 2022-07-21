package com.youngsbook.common

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*


object YoungsFunction {
    fun stringArrayToJson(jsonString : String ) : JSONArray
    {
        val jsonObject = JSONObject(jsonString)

        val resultJson : JSONArray = jsonObject.get("RESULT_LIST") as JSONArray

        if(jsonObject.get("RESULT_LIST").toString() == "[]")
            resultJson.put("")

        return resultJson
    }

    fun stringIntToJson(jsonString : String ) : Int
    {
        val jsonObject = JSONObject(jsonString)

        val resultJson : Int = jsonObject.get("RESULT_LIST").toString().toInt()

        return resultJson
    }

    /**
     * 현재 날짜 및 시간을 리턴한다.
     * param ex "yyyyMMdd"  "yyyy-MM-dd" "HH:mm" ...
     */
    fun getNowDate(format: String? = null): String {
        return SimpleDateFormat(if (format.isNullOrEmpty()) "yyyy-MM-dd" else format).format(Date())
    }

    /**
     * Date형식을 입력하면 yyyy-MM-dd 형식의 String으로 반환해준다.
     */
    fun getDate(date: Date): String {
        val simple : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val afterFormat = simple.format(date)

        return afterFormat
    }

    fun messageBoxOK(context: Context, title : String, Message : String){
        val messageBox = AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(Message)
        .setPositiveButton("확인") {
                dialogInterface : DialogInterface, i : Int ->
        }
        .setCancelable(false)
        .show()
    }
    fun messageBoxOKAction(context: Context, title : String, Message : String, OKAction : () -> Unit){
        val messageBox = AlertDialog.Builder(context)
        messageBox.setTitle(title)
        .setMessage(Message)
        .setPositiveButton("확인") {
                dialogInterface : DialogInterface, i : Int ->
            OKAction()
        }
        .setCancelable(false)
        .show()
    }
    fun messageBoxOKCancelAction(context: Context, title : String, Message : String, OKAction : () -> Unit, cancelAction : () -> Unit){
        val messageBox = AlertDialog.Builder(context)
        messageBox.setTitle(title)
        .setMessage(Message)
        .setPositiveButton("확인") {
                dialogInterface : DialogInterface, i : Int ->
            OKAction()
        }
        .setNegativeButton("취소"){
                dialogInterface : DialogInterface, i : Int ->
            cancelAction()
        }
        .setCancelable(false)
        .show()
    }

    /*
     01012345678과 같은 형식으로 파라미터를 넣으면 국제번호로 값을 return 해준다.
     */
    fun phoneNumber82(msg : String) : String{

        val firstNumber : String = msg.substring(0,3)
        var phoneEdit = msg.substring(3)

        when(firstNumber){
            "010" -> phoneEdit = "+8210$phoneEdit"
            "011" -> phoneEdit = "+8211$phoneEdit"
            "016" -> phoneEdit = "+8216$phoneEdit"
            "017" -> phoneEdit = "+8217$phoneEdit"
            "018" -> phoneEdit = "+8218$phoneEdit"
            "019" -> phoneEdit = "+8219$phoneEdit"
            "106" -> phoneEdit = "+82106$phoneEdit"
        }
        Log.d("국가코드로 변경된 번호 ",phoneEdit)
        return phoneEdit
    }

    fun getStringFromJson(jsonObject: JSONObject, columnName: String, defaultValue: String?): String? {
        if (jsonObject.has(columnName)) {
            var string = jsonObject.getString(columnName)
            if (string.equals("null", ignoreCase = true)) {
                string = ""
            }
            return string
        } else {
            return defaultValue
        }
    }
    fun getIntFromJson(jsonObject: JSONObject, columnName: String, defaultValue: Int?): Int? {
        return if (jsonObject.has(columnName)) {
            jsonObject.getInt(columnName)
        } else {
            defaultValue
        }
    }

    fun bookSearch(searchWord : String) : JSONObject{
        val clientId = Define.NAVER_CLIENT_ID

        val clientSecret = Define.NAVER_CLIENT_SECRETE
        var bookJsonObject : JSONObject = JSONObject()

        try {
            runBlocking {
                CoroutineScope(Dispatchers.IO).launch {
                    val text: String = URLEncoder.encode(searchWord, "UTF-8")
                    val apiURL = "https://openapi.naver.com/v1/search/book.json" + "?query=" + text + "&display=20" // json 결과
                    val url = URL(apiURL)
                    val con: HttpURLConnection = url.openConnection() as HttpURLConnection
                    con.setRequestMethod("GET")
                    con.setRequestProperty("X-Naver-Client-Id", clientId)
                    con.setRequestProperty("X-Naver-Client-Secret", clientSecret)
                    Log.d("네이버 url", apiURL)
                    Log.d("con.responseCode", con.responseCode.toString())

                    val responseCode: Int = con.getResponseCode()
                    val br: BufferedReader
                    if (responseCode == 200) { // 정상 호출
                        br = BufferedReader(InputStreamReader(con.getInputStream(), "UTF-8"))
                    } else {  // 에러 발생
                        br = BufferedReader(InputStreamReader(con.getErrorStream()))
                    }
                    var inputLine: String?
                    val response = StringBuffer()
                    while (br.readLine().also { inputLine = it } != null) {
                        response.append(inputLine)
                        response.append("\n")
                    }
                    br.close()
                    val naverHtml: String = response.toString()
                    Log.d("con.responseCode", response.toString())
                    val bun = Bundle()
                    bun.putString("NAVER_HTML", naverHtml)


                    bookJsonObject = JSONObject(response.toString())

                }.join()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bookJsonObject
    }
}