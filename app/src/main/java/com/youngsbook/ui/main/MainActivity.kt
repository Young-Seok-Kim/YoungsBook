package com.youngsbook.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.youngsbook.R
import com.youngsbook.common.SharedPreference
import com.youngsbook.databinding.ActivityMainBinding
import com.youngsbook.ui.main.myBookList.MyBookList
import com.youngsbook.ui.main.setting.Setting
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences : SharedPreferences
    private var backKeyPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(SharedPreference.SAVE_LOGIN_INFO, MODE_PRIVATE)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction().replace(binding.frameLayoutActivityMain.id, MyBookList()).commit()

        binding.bottomNavigationview.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    supportFragmentManager.beginTransaction().replace(binding.frameLayoutActivityMain.id, MyBookList()).commit()
                }
//                R.id.goal->{
////                    supportFragmentManager.beginTransaction()
////                        .replace(binding.frameLayoutActivityMain.id, Goal()).commit()
//                    YoungsFunction.messageBoxOK(context = this@MainActivity, "정보","해당 서비스는 준비중입니다.")
//                }
                R.id.setting->{
                    Setting().let {
                        supportFragmentManager.beginTransaction()
                            .replace(binding.frameLayoutActivityMain.id, it).commit()
                    }
                }
                else -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.frameLayoutActivityMain.id, MyBookList()).commit()
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
//           YoungsContextFunction().loadAD(context = applicationContext, binding.adBanner)
    }



    override fun onBackPressed() {
        if(!binding.bottomNavigationview.menu[0].isChecked)
        {
            supportFragmentManager.beginTransaction().replace(binding.frameLayoutActivityMain.id, MyBookList()).commit()
            binding.bottomNavigationview.menu[0].isChecked = true
            return
        }

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText( this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finishAndRemoveTask()
        }
    }
}