package com.youngsbook.common

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.youngsbook.R

class YoungsContextFunction {
    var m_adView: AdView? = null

    fun loadAD(context: Context, adBinding : AdView){

        MobileAds.initialize( context, context.getString(R.string.app_ad_id));

        m_adView = adBinding
        val adRequest = AdRequest.Builder().build()
        m_adView?.loadAd(adRequest)

    }
}