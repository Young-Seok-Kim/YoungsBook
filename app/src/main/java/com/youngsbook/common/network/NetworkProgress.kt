package com.youngsbook.common.network

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.youngsbook.ui.main.MainActivity

object NetworkProgress {

    private var dialog: Dialog? = null

    fun start(contextParam: Context? = null) {
        var context = contextParam
        if (context == null) {
            Toast.makeText(context,"context 없음",Toast.LENGTH_SHORT).show()
//            context = activity //Emaintec.activity!!
            return
        }
        if (dialog == null || dialog!!.context !== context) {
            dialog?.dismiss()
            dialog = null

            val linearLayout = LinearLayout(context)
            linearLayout.gravity = Gravity.CENTER
            linearLayout.setBackgroundColor(Color.TRANSPARENT)

            val progressBar = ProgressBar(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                val wrapDrawable = DrawableCompat.wrap(progressBar.indeterminateDrawable)
                DrawableCompat.setTint(
                    wrapDrawable,
                    ContextCompat.getColor(context, android.R.color.holo_red_light)
                )
                progressBar.indeterminateDrawable = DrawableCompat.unwrap(wrapDrawable)
            } else {
                progressBar.indeterminateDrawable.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        android.R.color.holo_red_light
                    ), PorterDuff.Mode.SRC_IN
                )
            }
            linearLayout.addView(
                progressBar,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            dialog = Dialog(context)
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            if (dialog!!.window != null) {
                dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            }
            dialog!!.setContentView(
                linearLayout,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            dialog!!.setCancelable(false)
            dialog!!.setOnKeyListener { dialog, keyCode, event -> keyCode == KeyEvent.KEYCODE_SEARCH && event.repeatCount == 0 }
            dialog?.let { it.show()  }
        } else if (!dialog!!.isShowing) {
            dialog!!.show()
        }
    }

    fun end() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
            dialog = null
        }
    }
}