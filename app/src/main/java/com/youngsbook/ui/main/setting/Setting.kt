package com.youngsbook.ui.main.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.youngsbook.R
import com.youngsbook.common.network.NetworkProgress
import com.youngsbook.databinding.SettingBinding
import com.youngsbook.ui.main.setting.resign.Resign


class Setting : Fragment() {

    lateinit var binding: SettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SettingBinding.inflate(layoutInflater)
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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }
}