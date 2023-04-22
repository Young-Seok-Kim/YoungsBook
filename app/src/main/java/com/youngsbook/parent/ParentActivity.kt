package com.youngsbook.parent

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.youngsbook.common.Define
import com.youngsbook.common.SharedPreference

open class ParentActivity : AppCompatActivity() {

    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->

        if (error != null) {
            Log.d("TAG", "로그인 실패", error)


            if (error.toString().contains("statusCode=302")) {
                Log.d("에러","302에러")
            }
        } else if (token != null) {
            Log.i("TAG", "로그인 성공 ${token.accessToken}")

            UserApiClient.instance.me { user, error ->

                if (error != null) {
                    Log.e("TAG", "사용자 정보 요청 실패", error)
                } else if (user != null) {
                    Log.i(
                        "TAG", "사용자 정보 요청 성공" +
                                "\n회원번호: ${user.id}" +
                                "\n이메일: ${user.kakaoAccount?.email}" +
                                "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                                "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}"
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
    }


}