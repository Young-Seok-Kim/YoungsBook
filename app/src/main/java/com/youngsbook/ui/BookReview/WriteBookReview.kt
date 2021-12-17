package com.youngsbook.ui.BookReview

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.gson.JsonObject
import com.youngsbook.common.Data
import com.youngsbook.common.YoungsFunction
import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.databinding.WriteBookReviewBinding
import com.youngsbook.ui.main.MainActivityAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WriteBookReview : DialogFragment() {

    lateinit var binding: WriteBookReviewBinding
    private lateinit var sharedPrefer : SharedPreferences
//    lateinit var mainActivityModel : MainActivityModel

    lateinit var status : String

    private lateinit var onClickListener: OnDialogDismissListener

    interface OnDialogDismissListener
    {
        fun whenDismiss()
    }

    fun setOnDismissListener(listener: OnDialogDismissListener)
    {
        this@WriteBookReview.onClickListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WriteBookReviewBinding.inflate(layoutInflater)

        sharedPrefer = requireActivity().getSharedPreferences(Data.instance.login_Info,AppCompatActivity.MODE_PRIVATE)

    }

    override fun onResume() {
        super.onResume()
        // 전체화면으로 만드는 코드
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) // 터치 불가능 코드 회수

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        whenOpen()
        initButton()

        return binding.root
    }

    private fun whenOpen()
    {
        if (status == Data.instance.status_insert)
        {
            binding.buttonDelete.visibility = View.GONE
        }
        else if (status == Data.instance.status_update)
        {
            binding.editTextBookName.setText(MainActivityAdapter.instance.currentItem?.BOOK_NAME)
            binding.editTextBookReview.setText(MainActivityAdapter.instance.currentItem?.REVIEW)
            binding.ratingBarStar.rating = MainActivityAdapter.instance.currentItem?.STAR_RATING ?: 0F // 기본값 0.0

        }
    }

    private fun initButton() {
        binding.buttonSave.setOnClickListener {
            if (status == "I") {

                if (binding.editTextBookName.text.isNullOrBlank()) {
                    Toast.makeText(context, "책 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else if (binding.editTextBookReview.text.isNullOrBlank()) {
                    binding.editTextBookReview.setText(" ")
                }


                val jsonObject: JsonObject = JsonObject()
                jsonObject.addProperty("book_name", binding.editTextBookName.text.toString())
                jsonObject.addProperty("read_date", YoungsFunction.getNowDate())
                jsonObject.addProperty(
                    "reader_id",
                    sharedPrefer.getString(Data.instance.login_id, " ")
                )
                jsonObject.addProperty(
                    "reader_name",
                    sharedPrefer.getString(Data.instance.login_name, " ")
                )
                jsonObject.addProperty("review", binding.editTextBookReview.text.toString())
                jsonObject.addProperty("star_rating", binding.ratingBarStar.rating)
                NetworkConnect.startProgress(requireContext()) // 종료는 connectNetwork 안에서 해주므로 따로 해줄 필요는 없다
                CoroutineScope(Dispatchers.Default).launch {
                    NetworkConnect.connectNetwork("InsertBookReview.do",
                        jsonObject,
                        requireContext()// 실패했을때 Toast 메시지를 띄워주기 위한 Context
                        , onSuccess = { ->
                            Toast.makeText(
                                context,
                                "${binding.ratingBarStar.rating}점으로 리뷰를 등록했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            dismiss()
                        }
                    )

                }
            }
            else if (status == "U")
            {
                if (binding.editTextBookName.text.isNullOrBlank()) {
                    Toast.makeText(context, "책 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else if (binding.editTextBookReview.text.isNullOrBlank()) {
                    binding.editTextBookReview.setText(" ")
                }

                val jsonObject: JsonObject = JsonObject()
                jsonObject.addProperty("book_name", binding.editTextBookName.text.toString())
                jsonObject.addProperty("read_date", YoungsFunction.getNowDate())
                jsonObject.addProperty("review",binding.editTextBookReview.text.toString())
                jsonObject.addProperty("star_rating", binding.ratingBarStar.rating)
                jsonObject.addProperty("review_no", MainActivityAdapter.instance.currentItem!!.REVIEW_NO)
                NetworkConnect.startProgress(requireContext()) // 종료는 connectNetwork 안에서 해주므로 따로 해줄 필요는 없다
                CoroutineScope(Dispatchers.Default).launch {
                    NetworkConnect.connectNetwork("UpdateBookReview.do",
                        jsonObject,
                        requireContext()// 실패했을때 Toast 메시지를 띄워주기 위한 Context
                        , onSuccess = { ->
                            Toast.makeText(
                                context,
                                "${binding.ratingBarStar.rating}점으로 리뷰를 등록했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            this@WriteBookReview.dismiss()

                        }
                    )
                }
            }
        }

        binding.buttonDelete.setOnClickListener()
        {
            val jsonObject : JsonObject = JsonObject()
            jsonObject.addProperty("review_no", MainActivityAdapter.instance.currentItem!!.REVIEW_NO)
            NetworkConnect.startProgress(requireContext()) // 종료는 connectNetwork 안에서 해주므로 따로 해줄 필요는 없다
            CoroutineScope(Dispatchers.Default).launch {
                NetworkConnect.connectNetwork("DeleteBookReview.do",
                    jsonObject,
                    requireContext()// 실패했을때 Toast 메시지를 띄워주기 위한 Context
                    , onSuccess = { ->
                        Toast.makeText(
                            context,
                            "${binding.editTextBookName.text}를 삭제했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        this@WriteBookReview.dismiss()
                    }
                )
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        onClickListener.whenDismiss()
    }

}