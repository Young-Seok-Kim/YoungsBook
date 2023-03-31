package com.youngsbook.ui.main.myBookList

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.youngsbook.R
import com.youngsbook.common.Define
import com.youngsbook.common.RecyclerViewAdapter
import com.youngsbook.common.YoungsFunction
import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.common.network.NetworkProgressDialog
import com.youngsbook.databinding.MyBookListBinding
import com.youngsbook.parent.ParentFragment
import com.youngsbook.ui.bookreview.WriteBookReview
import com.youngsbook.ui.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray


class MyBookList : ParentFragment() {

    lateinit var binding: MyBookListBinding
    val youngsProgressDialog = NetworkProgressDialog

    var pastVisiblesItems: Int = 0
    var visibleItemCount: Int = 0
    var totalItemCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = MyBookListBinding.inflate(layoutInflater)

        initList()
        if (Define.firstOpen) {
            updateList()
            Define.firstOpen = false
        }
        else
        {
            binding.title.text = "현재 책을 ${MyBookListAdapter.instance.itemCount}권 읽었어요"
        }
        initFAB()
        initListener()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    private fun initList()
    {
        val recyclerView = binding.listview

        val mLayoutManager: LinearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = mLayoutManager

        MyBookListAdapter.instance.setOnItemTapListener(object : RecyclerViewAdapter.OnItemTapListener{
            override fun onDoubleTap(position: Int) { }

            override fun onLongTap(position: Int){
                YoungsFunction.messageBoxOKCancelAction(requireContext(), "삭제", "${MyBookListAdapter.instance.currentItem?.BOOK_NAME} 리뷰를 삭제하시겠습니까?",
                    OKAction = {
                        val jsonObject : JsonObject = JsonObject()
                        jsonObject.addProperty("review_no", MyBookListAdapter.instance.currentItem?.REVIEW_NO)
                        youngsProgressDialog.start(requireContext())
                        CoroutineScope(Dispatchers.Main).launch {
                            NetworkConnect.connectHTTPS("DeleteBookReview.do",
                                jsonObject,
                                context = requireContext()// 실패했을때 Toast 메시지를 띄워주기 위한 Context
                                , onSuccess = { ->
                                    updateList()
                                }
                                , onFailure = {
                                    youngsProgressDialog.end()
                                }
                            )

                            val toast = Toast.makeText(
                                requireContext(),
                                "${MyBookListAdapter.instance.currentItem?.BOOK_NAME}를 삭제했습니다.",
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
                    val bundle = Bundle()
                    bundle.putString("status", "U")
                    it.arguments = bundle

//                    it.status =
                    it.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialogTheme)
                    it.dialog?.window?.setWindowAnimations(android.R.style.Animation_Dialog)
                    it.showNow(childFragmentManager,"")
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
        MyBookListAdapter.instance.listView = recyclerView
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
                val bundle = Bundle()
                bundle.putString("status", "I")
                it.arguments = bundle
                it.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialogTheme)
                it.showNow(childFragmentManager,"")
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
        if(Define.NOW_LOGIN_USER_ID.isBlank())
            return

        val jsonObject : JsonObject = JsonObject()
        jsonObject.addProperty("USER_ID", Define.NOW_LOGIN_USER_ID)
        NetworkProgressDialog.start(requireContext())
        CoroutineScope(Dispatchers.Default).launch {
            NetworkConnect.connectHTTPS("SelectMyBookReview.do",
                jsonObject,
                requireContext() // 실패했을때 Toast 메시지를 띄워주기 위한 Context
                , onSuccess = { ->
                    MyBookListAdapter.instance.clear()
                    val jsonArray : JSONArray = YoungsFunction.stringArrayToJson(NetworkConnect.resultString)

                    if (jsonArray.toString() != "[\"\"]") {
                        val list = Gson().fromJson(
                            jsonArray.toString(),
                            Array<MyBookListModel>::class.java
                        )
 
                        for (item in list) {
                            MyBookListAdapter.instance.addItem(item)
                        }
                    }
                    binding.title.text = "현재 책을 ${MyBookListAdapter.instance.itemCount}권 읽었어요"

                    NetworkProgressDialog.end()
                }
                , onFailure = {
                    NetworkProgressDialog.end()
                }
            )
        }

    }
}