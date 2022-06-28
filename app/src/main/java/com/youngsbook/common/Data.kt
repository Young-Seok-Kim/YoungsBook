package com.youngsbook.common

class Data {

    // SharedPreferece를 사용할때 사용할 String
    val AUTO_LOGIN_BOOLEAN : String = "auto_login_boolean"
    val AUTO_LOGIN_ID : String = "auto_login_id"
    val AUTO_LOGIN_PASSWORD : String = "auto_login_password"
    val LOGIN_ID : String = "login_id"
    val LOGIN_PASSWORD : String = "login_password"
    val LOGIN_NAME : String = "login_name"
    val LOGIN_INFO : String = "login_Info"
    val STATUS_INSERT : String = "I"
    val STATUS_UPDATE : String = "U"


    private object SingletoneHolder {
        val INSTANCE = Data()

    }

    companion object{
        val instance : Data
            get() = SingletoneHolder.INSTANCE
    }
}