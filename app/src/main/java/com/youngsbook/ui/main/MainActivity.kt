package com.youngsbook.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.youngsbook.databinding.ActivityMainBinding
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
//        binding.FAB?.setOnClickListener()
//        {
////            initList()
//        }
    }

    private fun initList() {

        with(datas){
            add(MainActivityModel("dog1",20))
            add(MainActivityModel("dog2",20))
            add(MainActivityModel("dog3",20))

        }

        val adapter=MainActivityAdapter() //어댑터 객체 만듦
        adapter.datalist=datas //데이터 넣어줌
        binding.listview.adapter=adapter //리사이클러뷰에 어댑터 연결
        binding.listview.layoutManager= LinearLayoutManager(this) //레이아웃 매니저 연결

    }
}