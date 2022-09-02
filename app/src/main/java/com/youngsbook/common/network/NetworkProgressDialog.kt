package com.youngsbook.common.network

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar

object NetworkProgressDialog { // XML에 progressbar를 사용하지 않았을때 사용하는 함수

    private var dialog: Dialog? = null

    fun start(contextParam: Context) {
        val context = contextParam
//        if (context == null) context = Emaintec.activity?
        val unit = if (dialog == null || dialog?.context !== context) {
            dialog?.dismiss()
            dialog = null

            val linearLayout = LinearLayout(context)
            linearLayout.gravity = Gravity.CENTER
            linearLayout.setBackgroundColor(Color.TRANSPARENT)


            val progressBar = ProgressBar(context)
//            progressBar.indeterminateDrawable.setColorFilter(
//                ContextCompat.getColor(
//                    context,
//                    android.R.color.holo_red_light
//                ), PorterDuff.Mode.SRC_IN
//            ) // 색 지정, deprecate됨
            linearLayout.addView(
                progressBar,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            dialog = Dialog(context)
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            if (dialog?.window != null) {
                dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            }
            dialog?.setContentView(
                linearLayout,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            dialog?.setCancelable(false)
            dialog?.setOnKeyListener { dialog, keyCode, event -> keyCode == KeyEvent.KEYCODE_SEARCH && event.repeatCount == 0 }

            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) // Dialog 뒷 배경 검정색으로 나오지않게하기

            dialog?.show()
        } else if (dialog?.isShowing == false) {
            dialog?.show()
        }
        else
            dialog?.dismiss()
    }

    fun end() {
        if (dialog != null && dialog?.isShowing == true) {
            dialog?.dismiss()
            dialog = null
        }
    }
}
