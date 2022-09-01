package com.youngsbook.common.network

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment

class NetworkProgress{ // XML에 progressbar를 생성했을때 사용하는 함수, 액티비티간 전환하면서 Progressbar를 사용하는 클래스
    fun startProgress(bindingProgressBar: ProgressBar?, window : Window)
    {
        bindingProgressBar?.visibility = View.VISIBLE
        window.setFlags( // 터치할수 없도록 하는 코드
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
    fun endProgressBar(bindingProgressBar: ProgressBar?, window : Window){
        bindingProgressBar?.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) // 터치불가능코드 회수
    }

}