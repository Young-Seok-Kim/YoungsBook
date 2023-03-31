package com.youngsbook.common

object SharedPreference {

    val SAVE_LOGIN_INFO_BOOLEAN : String = "save_login_info_boolean" // SAVE_LOGIN_INFO_** -> 로그인할때 로그인정보 저장을 클릭시 저장되는 변수
    val SAVE_LOGIN_INFO_ID : String = "save_login_info_id"
    val SAVE_LOGIN_INFO_PASSWORD : String = "save_login_info_password"
    val AUTO_LOGIN_BOOLEAN : String = "auto_login_boolean"

    val NOW_LOGIN_USER_ID : String = "login_id" // 현재 로그인한 아이디, 로그인 정보를 저장하지 않았을때를 대비 해당 변수를 사용
    val NOW_LOGIN_USER_NAME : String = "login_name"
    val SAVE_LOGIN_INFO : String = "login_Info" // 현재 로그인한 정보
}