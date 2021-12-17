package com.youngsbook.common

import org.json.JSONArray
import org.json.JSONObject
import com.google.gson.Gson
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
}