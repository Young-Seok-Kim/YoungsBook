package com.youngsbook.ui.main.goal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.youngsbook.R
import com.youngsbook.common.network.NetworkProgress
import com.youngsbook.databinding.GoalBinding
import com.youngsbook.parent.ParentFragment
class Goal : ParentFragment() {
    // 해당 페이지는 기능을 구현하려고 했으나 구현하지 않음
    lateinit var binding: GoalBinding
    val youngsProgress = NetworkProgress()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GoalBinding.inflate(layoutInflater)

        initButton()
    }

    private fun initButton() {
        binding.textviewGoalSetting.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
                GoalSetting().let {
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