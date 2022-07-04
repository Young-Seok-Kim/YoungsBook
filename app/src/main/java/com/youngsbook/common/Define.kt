package com.youngsbook.common

import android.text.InputFilter
import java.util.regex.Pattern

object Define {
    //10.0.2.2 = localhost

    val BASE_URL : String = "http://192.168.0.15:8080/YoungsBook/"
    val BASE_URL_HTTPS_DEBUG : String = "http://10.0.2.2:8080/YoungsBook/" // youngsbook.duckdns.org
    val BASE_URL_HTTPS_RELEASE : String = "https://awsyoungsbook.duckdns.org/"
    val HTTPS_TEST : String = "https://jsonplaceholder.typicode.com/posts/1/"
    val KYOBO_SEARCH_URL : String = "https://search.kyobobook.co.kr/web/search?vPstrKeyWord="

    val cantInputSpecialText =
        /*
        한글만 입력되도록 하는 변수, 사용법 : binding.userid?.filters = cantInputSpecialText
        특수문자를 입력하면 공백("")이 입력된다.
         */
        arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            val ps: Pattern = Pattern.compile("^[a-zA-Z0-9]+$")
            if (!ps.matcher(source).matches()) {
                ""
            } else null
        })
}