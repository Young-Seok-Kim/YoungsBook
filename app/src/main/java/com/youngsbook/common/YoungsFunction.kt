package com.youngsbook.common

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import org.json.JSONArray
import org.json.JSONObject
import com.google.gson.Gson
import com.youngsbook.R
import java.text.SimpleDateFormat
import java.util.*


object YoungsFunction {
    fun stringToJson( jsonString : String ) : JSONArray
    {
        val jsonObject = JSONObject(jsonString)

        val resultJson : JSONArray = jsonObject.get("RESULT_LIST") as JSONArray

        if(jsonObject.get("RESULT_LIST").toString() == "[]")
            resultJson.put("")

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
        messageBox.setTitle(title)
        messageBox.setMessage(Message)
        messageBox.setPositiveButton("확인") {
                dialogInterface : DialogInterface, i : Int ->
        }
        messageBox.show()
    }
    fun messageBoxOKAction(context: Context, title : String, Message : String, OKAction : () -> Unit){
        val messageBox = AlertDialog.Builder(context)
        messageBox.setTitle(title)
        .setMessage(Message)
        .setPositiveButton("확인") {
                dialogInterface : DialogInterface, i : Int ->
            OKAction()
        }
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
}