package com.youngsbook.ui.main.setting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.youngsbook.R
import com.youngsbook.common.Define
import com.youngsbook.common.YoungsFunction
import com.youngsbook.databinding.SettingMenuBinding
import com.youngsbook.ui.login.LoginActivity
import com.youngsbook.ui.main.setting.resign.Resign


class Setting : Fragment() {

    lateinit var binding: SettingMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SettingMenuBinding.inflate(layoutInflater)
        initLinearLayoutTapEvent()
    }

    private fun initLinearLayoutTapEvent() {
        binding.linearLayoutResign.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
                Resign().let {
                    it.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialogTheme)
                    it.dialog?.window?.setWindowAnimations(android.R.style.Animation_Dialog)
                    it.showNow(childFragmentManager,"")
                }
            }
        })

        binding.linearLayoutLogout.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
                YoungsFunction.messageBoxOKCancelAction(requireContext(),"주의!","정말 로그아웃 하시겠습니까?",
                OKAction = {
                    val intent = Intent(context, LoginActivity::class.java)
                    Define.whenLogin = false
                    startActivity(intent)
                    requireActivity().finish()
                },
                cancelAction = {

                })
            }
        })

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }
}