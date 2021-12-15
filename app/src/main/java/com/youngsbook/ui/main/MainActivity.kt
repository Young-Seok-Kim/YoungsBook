package com.youngsbook.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.youngsbook.R
import com.youngsbook.common.Data
import com.youngsbook.common.RecyclerViewAdapter
import com.youngsbook.common.YoungsFunction
import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.databinding.ActivityMainBinding
import com.youngsbook.ui.BookReview.WriteBookReview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var mainActivityAdapter: MainActivityAdapter
    private lateinit var sharedPrefer : SharedPreferences
    val datas = mutableListOf<MainActivityModel>()

    var pastVisiblesItems: Int = 0
    var visibleItemCount: Int = 0
    var totalItemCount: Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefer = getSharedPreferences(Data.instance.login_Info,AppCompatActivity.MODE_PRIVATE)

        binding = ActivityMainBinding.inflate(layoutInflater)

        val dialog = WriteBookReview()

        setContentView(binding.root)
        initList()
        updateList()
        initFAB()
        initListener()
    }
    private fun initList()
    {
        val recyclerView = binding.listview

        val mLayoutManager: LinearLayoutManager
        mLayoutManager = LinearLayoutManager(application)
        recyclerView.setLayoutManager(mLayoutManager)

        MainActivityAdapter.instance.setOnItemTapListener(object : RecyclerViewAdapter.OnItemTapListener{
            override fun onDoubleTap(position: Int) {
            }

            override fun onLongTap(position: Int): Boolean {
                return true
            }

            override fun onSingleTap(position: Int) {
                WriteBookReview().let {
                    it.status = Data.instance.status_update
                    it.setOnDismissListener(object : WriteBookReview.OnDialogDismissListener{
                        override fun whenDismiss() {
                            updateList()
                        }
                    })
                    it.showNow(supportFragmentManager,"")
                }
            }
        })

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {


            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) //check for scroll down
                {
                    visibleItemCount = mLayoutManager.childCount
                    totalItemCount = mLayoutManager.itemCount
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition()

                        if (visibleItemCount + pastVisiblesItems > totalItemCount) {

                            updateList()
                        }

                }
            }

            override fun onScrollStateChanged(view: RecyclerView, scrollState: Int) {
                //OnScrollListener.SCROLL_STATE_IDLE은 스크롤이 이동하다가 멈추었을때 발생되는 스크롤 상태입니다.
                //즉 스크롤이 바닥에 닿아 멈춘 상태에 처리를 하겠다는
            }
        })
        MainActivityAdapter.instance.listView = recyclerView
    }
    private fun initListener()
    {
        binding.swipe.setOnRefreshListener {
            updateList()
            binding.swipe.isRefreshing = false // 새로고침 마크 지우기
        }
    }
    private fun initFAB()
    {
        binding.FAB.setOnClickListener()
        {
            WriteBookReview().let {
                it.status = Data.instance.status_insert
                it.setOnDismissListener(object : WriteBookReview.OnDialogDismissListener{
                    override fun whenDismiss() {
                        updateList()
                    }
                })
                it.showNow(supportFragmentManager,"")
            }
        }
    }

    private fun updateList() {

        val jsonObject : JsonObject = JsonObject()
        jsonObject.addProperty("ID", sharedPrefer.getString(Data.instance.login_id," "))
        NetworkConnect.startProgress(this) // 종료는 connectNetwork 안에서 해주므로 따로 해줄 필요는 없다
        CoroutineScope(Dispatchers.Default).launch {
            NetworkConnect.connectNetwork("SelectMyBookReview.do",
                jsonObject,
                applicationContext // 실패했을때 Toast 메시지를 띄워주기 위한 Context
                , onSuccess = { ->
                    val jsonArray : JSONArray
                    jsonArray = YoungsFunction.stringToJson(NetworkConnect.resultString)
                    val list = Gson().fromJson(jsonArray.toString(), Array<MainActivityModel>::class.java)

//                    val adapter=MainActivityAdapter() //어댑터 객체 만듦
//                    adapter.datalist=datas //데이터 넣어줌
//                    binding.listview.adapter=adapter //리사이클러뷰에 어댑터 연결
//                    binding.listview.layoutManager= LinearLayoutManager(this@MainActivity) //레이아웃 매니저 연결

                    MainActivityAdapter.instance.clear()

                    val adapter = MainActivityAdapter //어댑터 객체 만듦
                    adapter.instance.datalist = list.toMutableList()


                }
            )
        }
    }
}