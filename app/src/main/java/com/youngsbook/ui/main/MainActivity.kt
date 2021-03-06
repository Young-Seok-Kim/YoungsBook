package com.youngsbook.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.youngsbook.R
import com.youngsbook.common.*
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
        sharedPrefer = getSharedPreferences(SharedPreference.LOGIN_INFO,AppCompatActivity.MODE_PRIVATE)

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

            override fun onLongTap(position: Int){
                YoungsFunction.messageBoxOKCancelAction(this@MainActivity, "??????", "${MainActivityAdapter.instance.currentItem?.BOOK_NAME} ????????? ?????????????????????????",
                    OKAction = {
                        val jsonObject : JsonObject = JsonObject()
                        jsonObject.addProperty("review_no", MainActivityAdapter.instance.currentItem?.REVIEW_NO)
                        youngsProgress.startProgress(binding.progressbar)
                        youngsProgress.notTouchable(window)
                        CoroutineScope(Dispatchers.Main).launch {
                            NetworkConnect.connectHTTPS("DeleteBookReview.do",
                                jsonObject,
                                context = applicationContext// ??????????????? Toast ???????????? ???????????? ?????? Context
                                , onSuccess = { ->
                                    updateList()
                                }
                                , onFailure = {
                                    youngsProgress.endProgressBar(binding.progressbar)
                                    youngsProgress.touchable(window)
                                }
                            )

                            val toast = Toast.makeText(
                                applicationContext,
                                "${MainActivityAdapter.instance.currentItem?.BOOK_NAME}??? ??????????????????.",
                                Toast.LENGTH_SHORT
                                )

                                toast.setGravity(Gravity.CENTER_VERTICAL,0,700)
                                toast.show()
                        }
                    },
                    cancelAction = {
                        return@messageBoxOKCancelAction
                    }
                )
            }

            override fun onSingleTap(position: Int) {
                WriteBookReview().let {
                    it.status = Define.STATUS_UPDATE
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
                //OnScrollListener.SCROLL_STATE_IDLE??? ???????????? ??????????????? ??????????????? ???????????? ????????? ???????????????.
                //??? ???????????? ????????? ?????? ?????? ????????? ????????? ????????????
            }
        })
        MainActivityAdapter.instance.listView = recyclerView
    }
    private fun initListener()
    {
        binding.swipe.setOnRefreshListener {
            updateList()
            binding.swipe.isRefreshing = false // ???????????? ?????? ?????????
        }
    }
    private fun initFAB()
    {
        binding.FAB.setOnClickListener()
        {
            supportFragmentManager.executePendingTransactions()
            WriteBookReview().let {
                it.status = Define.STATUS_INSERT
                it.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialogTheme)
                it.showNow(supportFragmentManager,"")
                it.dialog?.window?.setWindowAnimations(android.R.style.Animation_Dialog)
                it.setOnDismissListener(object : WriteBookReview.OnDialogDismissListener{ // ?????????????????? ????????? ?????? ???????????? ??????????????????.
                    override fun whenDismiss() {
                        updateList()
                    }
                })
            }
        }
    }

    private fun updateList() {

        val jsonObject : JsonObject = JsonObject()
        jsonObject.addProperty("ID", sharedPrefer.getString(SharedPreference.LOGIN_ID," "))
        youngsProgress.startProgress(binding.progressbar)
        youngsProgress.notTouchable(window)
        CoroutineScope(Dispatchers.Default).launch {
            NetworkConnect.connectHTTPS("SelectMyBookReview.do",
                jsonObject,
                applicationContext // ??????????????? Toast ???????????? ???????????? ?????? Context
                , onSuccess = { ->
                    MainActivityAdapter.instance.clear()
                    val jsonArray : JSONArray = YoungsFunction.stringArrayToJson(NetworkConnect.resultString)

                    if (jsonArray.toString() != "[\"\"]") {
                        val list = Gson().fromJson(
                            jsonArray.toString(),
                            Array<MainActivityModel>::class.java
                        )

                        for (item in list) {
                            MainActivityAdapter.instance.addItem(item)
                        }
                    }
                    binding.title.text = "?????? ?????? ${MainActivityAdapter.instance.itemCount}??? ????????????"

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
            Toast.makeText(this, "\'??????\' ????????? ?????? ??? ???????????? ???????????????.", Toast.LENGTH_SHORT).show()
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
//            super.onBackPressed()
            finishAndRemoveTask()
        }
    }
}