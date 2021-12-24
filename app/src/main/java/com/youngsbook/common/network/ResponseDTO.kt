package com.youngsbook.common.network

import androidx.annotation.Keep
import com.google.gson.JsonObject
import java.lang.reflect.GenericArrayType
import java.util.*
import kotlin.collections.HashMap

@Keep
data class ResponseDTO (var returnValue : String)


// 테스트용 데이터 클래스

data class Baemin(val timestamp: Long, val statusCode: String, val statusMessage: String, val data: Data)

data class Data(val content: ArrayList<Content>)

data class Content(val contentId: Long, val contentSeq: Long, val title: String, val created: String)