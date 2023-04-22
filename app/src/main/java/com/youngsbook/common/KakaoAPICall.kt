package com.youngsbook.common

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User

object KakaoAPICall {


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

    fun getKakaoUserInfo(context : Context) : User?{

        var mUser  : User? = null

        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error != null) {
                    if (error is KakaoSdkError && error.isInvalidTokenError()) {
                        //로그인 필요
                        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context))
                            UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
                        else
                            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                    }
                    else {
                        //기타 에러
                    }
                }
                else {
                    //토큰 유효성 체크 성공(필요 시 토큰 갱신됨)
                    UserApiClient.instance.me { user, error ->

                        if (error != null) {
                            Log.e("TAG", "사용자 정보 요청 실패", error)
                        } else if (user != null) {

                            mUser = user

                        }
                    }
                }
            }
        }
        else {
            //로그인 필요

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context))
                UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
            else
                UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }


        return mUser
    }
}