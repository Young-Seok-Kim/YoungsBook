package com.youngsbook.parent

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.youngsbook.common.Define
import com.youngsbook.common.SharedPreference

open class ParentFragment : Fragment() {
    public lateinit var sharedPreferences : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(SharedPreference.SAVE_LOGIN_INFO,AppCompatActivity.MODE_PRIVATE)
    }

    override fun onResume() {
        super.onResume()
        if(Define.NOW_LOGIN_USER_ID.isBlank())
        {
            Define.NOW_LOGIN_USER_ID = sharedPreferences.getString(SharedPreference.NOW_LOGIN_USER_ID,"").toString()
//            YoungsFunction.messageBoxOKAction(requireContext(),
//                "경고!",
//                "로그인 정보가 소멸되었습니다. 다시 로그인해주세요.",
//                OKAction = {
//                    val intent = Intent(requireContext(), LoginActivity::class.java)
//                    startActivity(intent)
//                    requireActivity().finish()
//                })
        }

        if (Define.NOW_LOGIN_USER_NAME.isBlank())
        {
            Define.NOW_LOGIN_USER_NAME = sharedPreferences.getString(SharedPreference.NOW_LOGIN_USER_NAME,"").toString()
        }
    }
}