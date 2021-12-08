package com.youngsbook

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

object YoungsFunction {
    fun alertDialog(context: Context, title : String, message : String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(title).setMessage(message)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
}