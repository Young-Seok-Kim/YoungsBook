package com.youngsbook.ui.main.goal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.youngsbook.R
import com.youngsbook.common.network.NetworkProgress
import com.youngsbook.databinding.GoalSettingBinding


class GoalSetting : DialogFragment() {

    lateinit var binding: GoalSettingBinding
    val youngsProgress = NetworkProgress()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GoalSettingBinding.inflate(layoutInflater)

        initSpinner()
    }

    private fun initSpinner() {
        val list : ArrayList<String> = ArrayList<String>()

        list.run {
            add(0,"연")
            add(1,"월")
        }

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, list)

        binding.spinnerGoalUnit.adapter = spinnerAdapter
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }


}