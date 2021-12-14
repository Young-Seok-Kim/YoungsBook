package com.youngsbook.common

class Data {

    // SharedPreferece를 사용할때 사용할 String
    val auto_login_boolean : String = "auto_login_boolean"
    val auto_login_id : String = "auto_login_id"
    val auto_login_password : String = "auto_login_password"
    val login_id : String = "login_id"
    val login_password : String = "login_password"
    val login_name : String = "login_name"
    val login_Info : String = "login_Info"


    private object SingletoneHolder {
        val INSTANCE = Data()

    }

    companion object{
        val instance : Data
            get() = SingletoneHolder.INSTANCE
    }
}