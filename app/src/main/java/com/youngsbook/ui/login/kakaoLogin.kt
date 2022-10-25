import android.content.Context
import android.util.Log
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User

class KakaoLogin
{
    interface IKLoginResult
    {
        fun onKakaoLoginResult(user: User?)
    }
    var user:User? = null
    var listener:IKLoginResult? = null

    // 로그인 callback 구성
    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->

        if (error != null) {
            Log.e("TAG", "로그인 실패", error)
            if(listener != null)
                listener!!.onKakaoLoginResult(null)
        }
        else if (token != null)
        {
            Log.i("TAG", "로그인 성공 ${token.accessToken}")

            UserApiClient.instance.me { user, error ->

                if (error != null)
                {
                    Log.e("TAG", "사용자 정보 요청 실패", error)
                }
                else if (user != null)
                {
                    Log.i("TAG", "사용자 정보 요청 성공" +
                            "\n회원번호: ${user.id}" +
                            "\n이메일: ${user.kakaoAccount?.email}" +
                            "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                            "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}")

                    this.user = user

                    if(listener != null)
                        listener!!.onKakaoLoginResult(user)
                }
            }
        }
    }
    fun login(context: Context)
    {
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context))
            UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
        else
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
    }

    //로그아웃
    fun onLink() {
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Log.e("TAG", "연결 끊기 실패", error)
            }
            else {
                Log.i("TAG", "연결 끊기 성공. SDK에서 토큰 삭제 됨")

            }
        }
    }

    companion object {
        @JvmStatic
        val instance by lazy { KakaoLogin() }
    }
}