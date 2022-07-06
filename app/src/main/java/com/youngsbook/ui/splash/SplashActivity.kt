package com.youngsbook.ui.splash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.youngsbook.ui.login.LoginActivity


class SplashActivity : Activity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}