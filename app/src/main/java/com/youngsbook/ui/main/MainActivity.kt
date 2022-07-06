package com.youngsbook.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.youngsbook.R
import com.youngsbook.common.Data
import com.youngsbook.common.RecyclerViewAdapter
import com.youngsbook.common.YoungsContextFunction
import com.youngsbook.common.YoungsFunction
import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.common.network.NetworkProgress
import com.youngsbook.databinding.ActivityMainBinding
import com.youngsbook.ui.bookreview.WriteBookReview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPrefer : SharedPreferences
    val youngsProgress = NetworkProgress()

    var pastVisiblesItems: Int = 0
    var visibleItemCount: Int = 0
    var totalItemCount: Int = 0

    private var backKeyPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefer = getSharedPreferences(Data.instance.LOGIN_INFO,AppCompatActivity.MODE_PRIVATE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        initList()
        updateList()
        initFAB()
        initListener()
    }

    override fun onResume() {
        super.onResume()
           YoungsContextFunction().loadAD(context = applicationContext, binding.adBanner)
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
                    it.status = Data.instance.STATUS_UPDATE
                    it.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialogTheme)
                    it.dialog?.window?.setWindowAnimations(android.R.style.Animation_Dialog)
                    it.showNow(supportFragmentManager,"")
                    it.setOnDismissListener(object : WriteBookReview.OnDialogDismissListener{
                        override fun whenDismiss() {
                            updateList()
                        }
                    })
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
            supportFragmentManager.executePendingTransactions()
            WriteBookReview().let {
                it.status = Data.instance.STATUS_INSERT
                it.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialogTheme)
                it.showNow(supportFragmentManager,"")
                it.dialog?.window?.setWindowAnimations(android.R.style.Animation_Dialog)
                it.setOnDismissListener(object : WriteBookReview.OnDialogDismissListener{ // 다이얼로그를 띄워준 후에 리스너를 등록해야한다.
                    override fun whenDismiss() {
                        updateList()
                    }
                })
            }
        }
    }

    private fun updateList() {

        val jsonObject : JsonObject = JsonObject()
        jsonObject.addProperty("ID", sharedPrefer.getString(Data.instance.LOGIN_ID," "))
        youngsProgress.startProgress(binding.progressbar) // 종료는 connectNetwork 안에서 해주므로 따로 해줄 필요는 없다
        youngsProgress.notTouchable(window)
        CoroutineScope(Dispatchers.Default).launch {
            NetworkConnect.connectHTTPS("SelectMyBookReview.do",
                jsonObject,
                applicationContext // 실패했을때 Toast 메시지를 띄워주기 위한 Context
                , onSuccess = { ->
                    MainActivityAdapter.instance.clear()
                    val jsonArray : JSONArray
                    jsonArray = YoungsFunction.stringToJson(NetworkConnect.resultString)
                    if (jsonArray.toString() != "[\"\"]") {
                        val list = Gson().fromJson(
                            jsonArray.toString(),
                            Array<MainActivityModel>::class.java
                        )



                        for (item in list) {
                            MainActivityAdapter.instance.addItem(item)
                        }
                    }
                    binding.title.text = "현재 책을 ${MainActivityAdapter.instance.itemCount}권 읽었어요"

                    youngsProgress.endProgressBar(binding.progressbar)
                    youngsProgress.touchable(window)
                }
            , onFailure = {
                    youngsProgress.endProgressBar(binding.progressbar)
                    youngsProgress.touchable(window)
                }
            )
        }

    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
//            super.onBackPressed()
            finishAndRemoveTask()
        }
    }
}