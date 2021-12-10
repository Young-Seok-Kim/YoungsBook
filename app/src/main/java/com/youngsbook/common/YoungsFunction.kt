package com.youngsbook.common

import org.json.JSONArray
import org.json.JSONObject
import com.google.gson.Gson

object YoungsFunction {
    fun stringToJson( jsonString : String ) : JSONArray
    {
        val jsonObject = JSONObject(jsonString)

        val resultJson : JSONArray = jsonObject.get("RESULT_LIST") as JSONArray

        if(jsonObject.get("RESULT_LIST").toString() == "[]")
            resultJson.put("")

        return resultJson
    }
}