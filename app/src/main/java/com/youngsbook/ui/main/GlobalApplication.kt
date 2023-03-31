package com.youngsbook.ui.main
import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "418199908136a18cc519fe9dfbc48335")
    }
}