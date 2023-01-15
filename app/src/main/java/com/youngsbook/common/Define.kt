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

    var whenLogin : Boolean = true // 자동로그인이 되게하는 변수, 해당변수가 없으면 로그아웃을 해도 자동로그인이 체크되어있을때 바로 로그인이 된다.
    var firstOpen : Boolean = true // 책 목록을 계속 불러오는건 비효율적이라 한번만 불러오도록 하는 변수
}