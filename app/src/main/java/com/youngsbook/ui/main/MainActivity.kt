package com.youngsbook.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.youngsbook.common.Data
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

    lateinit var dialogDismissListener: DialogDismissListener

    interface DialogDismissListener{
        fun whenDismiss()
    }

    fun setOnDataChangeListener() {
        this.dialogDismissListener = object : DialogDismissListener {
            override fun whenDismiss() {
                updateList()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefer = getSharedPreferences(Data.instance.login_Info,AppCompatActivity.MODE_PRIVATE)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        updateList()
        initFAB()
        initListener()
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
            WriteBookReview().showNow(supportFragmentManager,"")

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

                    datas.clear()

                    for(i in 0 until jsonArray.length())
                    {
                        with(datas)
                        {
                            if (!(jsonArray.isNull(i) || jsonArray[i] == "")) {
                                add(
                                    MainActivityModel(
                                        (jsonArray[i] as JSONObject).get("READER_NAME").toString(),
                                        (jsonArray[i] as JSONObject).get("READ_DATE").toString(),
                                        (jsonArray[i] as JSONObject).get("REVIEW_NO").toString().toInt(),
                                        (jsonArray[i] as JSONObject).get("STAR_RATING").toString().toFloat(),
                                        (jsonArray[i] as JSONObject).get("BOOK_NAME").toString(),
                                        (jsonArray[i] as JSONObject).get("READER_ID").toString(),
                                        (jsonArray[i] as JSONObject).get("REVIEW").toString()
                                    )
                                )
                            }
                        }
                    }

                    val adapter=MainActivityAdapter() //어댑터 객체 만듦
                    adapter.datalist=datas //데이터 넣어줌
                    binding.listview.adapter=adapter //리사이클러뷰에 어댑터 연결
                    binding.listview.layoutManager= LinearLayoutManager(this@MainActivity) //레이아웃 매니저 연결
                }
            )
        }
    }
}