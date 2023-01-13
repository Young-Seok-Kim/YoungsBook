package com.youngsbook.common

import android.text.InputFilter
import java.util.regex.Pattern

object Define {
    /*
        10.0.2.2 = localhost
        192.168.0.15:8080 = 집 컴퓨터 IP
    */

    val BASE_URL_HTTP_DEBUG : String = "http://192.168.0.15:8080/YoungsBook/"
    val BASE_URL_HTTPS_RELEASE : String = "https://awsyoungsbook.duckdns.org/"

    val NAVER_CLIENT_ID : String = "iUyBdHAwybdSHw37QhVo"
    val NAVER_CLIENT_SECRETE : String = "jtbUv8SooO"

    const val STATUS_INSERT : String = "I"
    const val STATUS_UPDATE : String = "U"

    var NOW_LOGIN_USER_ID : String = ""

    var whenLogin : Boolean = true
}