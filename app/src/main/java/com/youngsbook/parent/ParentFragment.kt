package com.youngsbook.parent

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.youngsbook.common.Define
import com.youngsbook.common.YoungsFunction
import com.youngsbook.ui.login.LoginActivity

open class ParentFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if(Define.NOW_LOGIN_USER_ID.isBlank())
        {
            YoungsFunction.messageBoxOKAction(requireContext(),
                "경고!",
                "로그인 정보가 소멸되었습니다. 다시 로그인해주세요.",
                OKAction = {
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                })
        }
    }
}