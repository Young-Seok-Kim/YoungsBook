package com.youngsbook.common.network

import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar

class NetworkProgress {

    fun startProgress(bindingProgressBar: ProgressBar?) // 서버와 통신하는동안 터치할수 없도록 하는 코드
    {
        bindingProgressBar?.visibility = View.VISIBLE
    }
    fun endProgressBar(bindingProgressBar: ProgressBar?){
        bindingProgressBar?.visibility = View.GONE
    }
    fun notTouchable(window: Window){

        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    fun touchable(window: Window)
    {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}