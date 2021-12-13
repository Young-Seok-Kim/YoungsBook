package com.youngsbook.ui.main

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.youngsbook.R
import com.youngsbook.databinding.ActivityMainBinding
import com.youngsbook.ui.BookReview.WriteBookReview
import java.security.AccessController.getContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var mainActivityAdapter: MainActivityAdapter
    val datas = mutableListOf<MainActivityModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initList()
        initFAB()
    }
    private fun initFAB()
    {
        binding.FAB.setOnClickListener()
        {
            WriteBookReview().showNow(supportFragmentManager,"")

        }
    }

    private fun initList() {

        with(datas){
            add(MainActivityModel(1,"dog1",20))
            add(MainActivityModel(2,"dog2",20))
            add(MainActivityModel(3,"dog3",20))
        }

        val adapter=MainActivityAdapter() //어댑터 객체 만듦
        adapter.datalist=datas //데이터 넣어줌
        binding.listview.adapter=adapter //리사이클러뷰에 어댑터 연결
        binding.listview.layoutManager= LinearLayoutManager(this) //레이아웃 매니저 연결

    }
}