package com.youngsbook.common.network

import android.content.Context
import android.view.*
import android.widget.ProgressBar

class NetworkProgress {

    public fun startProgress(context: Context, bindingProgressBar: ProgressBar?) // 서버와 통신하는동안 터치할수 없도록 하는 코드
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

//    private var dialog: Dialog? = null
//
//    fun start(contextParam: Context? = null) {
//        var context = contextParam
//        if (context == null) {
//            Toast.makeText(context,"context 없음",Toast.LENGTH_SHORT).show()
////            context = activity //Emaintec.activity!!
//            return
//        }
//        if (dialog == null || dialog!!.context !== context) {
//            dialog?.dismiss()
//            dialog = null
//
//            val linearLayout = LinearLayout(context)
//            linearLayout.gravity = Gravity.CENTER
//            linearLayout.setBackgroundColor(Color.TRANSPARENT)
//
//            val progressBar = ProgressBar(context)
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//                val wrapDrawable = DrawableCompat.wrap(progressBar.indeterminateDrawable)
//                DrawableCompat.setTint(
//                    wrapDrawable,
//                    ContextCompat.getColor(context, android.R.color.holo_red_light)
//                )
//                progressBar.indeterminateDrawable = DrawableCompat.unwrap(wrapDrawable)
//            } else {
//                progressBar.indeterminateDrawable.setColorFilter(
//                    ContextCompat.getColor(
//                        context,
//                        android.R.color.holo_red_light
//                    ), PorterDuff.Mode.SRC_IN
//                )
//            }
//            linearLayout.addView(
//                progressBar,
//                ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT
//                )
//            )
//
//            dialog = Dialog(context)
//            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
//            if (dialog!!.window != null) {
//                dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
//            }
//            dialog!!.setContentView(
//                linearLayout,
//                ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT
//                )
//            )
//            dialog!!.setCancelable(false)
//            dialog!!.setOnKeyListener { dialog, keyCode, event -> keyCode == KeyEvent.KEYCODE_SEARCH && event.repeatCount == 0 }
//            dialog?.let { it.show()  }
//        } else if (!dialog!!.isShowing) {
//            dialog!!.show()
//        }
//    }
//
//    fun end() {
//        if (dialog != null && dialog!!.isShowing) {
//            dialog!!.dismiss()
//            dialog = null
//        }
//    }
}