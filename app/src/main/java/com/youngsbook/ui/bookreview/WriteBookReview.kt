package com.youngsbook.ui.bookreview

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.gson.JsonObject
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.youngsbook.common.Define
import com.youngsbook.common.SharedPreference
import com.youngsbook.common.YoungsContextFunction
import com.youngsbook.common.YoungsFunction
import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.common.network.NetworkProgressDialog
import com.youngsbook.common.scan.ScanBookActivity
import com.youngsbook.common.scan.ScanBookModel
import com.youngsbook.databinding.WriteBookReviewBinding
import com.youngsbook.ui.main.myBookList.MyBookListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class WriteBookReview : DialogFragment() {

    lateinit var binding: WriteBookReviewBinding
    val youngsProgressDialog = NetworkProgressDialog
    private lateinit var sharedPrefer : SharedPreferences

    var status : String = "Default"
    private var scanBook : JSONObject? = null
    private var scanBookModel : ScanBookModel? = null
    val test = ActivityResultContracts.StartActivityForResult()

    private val activityResultLauncher : ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    scanBook = YoungsFunction.bookSearch(result.data?.extras?.getString("SCAN_RESULT").toString())
                    scanBook?.let { scanAction(it) }
                }
            }
        }


    private lateinit var onClickListener: OnDialogDismissListener

    interface OnDialogDismissListener
    {
        fun whenDismiss()
    }

    private fun scanAction(scanBook : JSONObject){
        Log.d("QR 코드 스캔 성공", scanBook.toString())
        if ((scanBook["items"] as JSONArray).isNull(0))
        {
            Toast.makeText(context,"책을 찾을수 없습니다.",Toast.LENGTH_LONG).show()
        }
        else {
            scanBookModel = ScanBookModel((scanBook["items"] as JSONArray).getJSONObject(0))
            binding.editTextBookName.setText(scanBookModel?.title)
        }
    }

    fun setOnDismissListener(listener: OnDialogDismissListener)
    {
        this@WriteBookReview.onClickListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WriteBookReviewBinding.inflate(layoutInflater)
        sharedPrefer = requireActivity().getSharedPreferences(SharedPreference.SAVE_LOGIN_INFO,AppCompatActivity.MODE_PRIVATE)

    }

    override fun onResume() {
        super.onResume()
        YoungsContextFunction().loadAD(requireContext(),binding.adBanner)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        whenOpen()
        initButton()

        return binding.root
    }

    private fun whenOpen()
    {
        binding.textviewGoalReadDate.text = MyBookListAdapter.instance.currentItem?.GOAL_READ_DATE

        if(MyBookListAdapter.instance.currentItem?.READ_COMPLETE == "1")
            binding.checkboxReadComplete.isChecked = true

        if (status == Define.STATUS_INSERT)
        {
            binding.buttonDelete.visibility = View.GONE
        }
        else if (status == Define.STATUS_UPDATE)
        {
            binding.editTextBookName.setText(MyBookListAdapter.instance.currentItem?.BOOK_NAME)
            binding.editTextBookName.isEnabled = false
            binding.editTextBookReview.setText(MyBookListAdapter.instance.currentItem?.REVIEW)
            binding.ratingBarStar.rating = MyBookListAdapter.instance.currentItem?.STAR_RATING ?: 0F // 기본값 0.0
            binding.buttonScanBarCode.visibility = View.GONE
        }
    }


    private fun initButton() {
        binding.buttonSave.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                if (binding.editTextBookName.text.length >= 200)
                {
                    Toast.makeText(context,"책 제목을 더 짧게 입력해주세요.",Toast.LENGTH_LONG).show()
                    return
                }

                val jsonObject: JsonObject = JsonObject() // insert와 update에서 동일하게 필요한값은 이곳에서 추가
                jsonObject.addProperty("book_name", binding.editTextBookName.text.toString())
                jsonObject.addProperty("read_date", YoungsFunction.getNowDate())
                jsonObject.addProperty("goal_read_date", binding.textviewGoalReadDate.text.toString())
                jsonObject.addProperty("review",binding.editTextBookReview.text.toString())
                jsonObject.addProperty("star_rating", binding.ratingBarStar.rating)
                jsonObject.addProperty("read_complete", binding.checkboxReadComplete.isChecked)
                jsonObject.addProperty("reader_code", Define.NOW_LOGIN_USER_CODE)

                if (status == Define.STATUS_INSERT) {

                    if (binding.editTextBookName.text.isNullOrBlank()) {
                        Toast.makeText(context, "책 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                        return
                    } else if (binding.editTextBookReview.text.isNullOrBlank()) {
                        binding.editTextBookReview.setText(" ")
                    }


                    jsonObject.addProperty(
                        "reader_id",
                        sharedPrefer.getString(SharedPreference.SAVE_LOGIN_ID, " ")
                    )
                    jsonObject.addProperty(
                        "reader_name",
                        sharedPrefer.getString(SharedPreference.SAVE_LOGIN_NAME, " ")
                    )
                    
//                    youngsProgress.startProgress(binding.progressbar,dialog?.window!!)
                    NetworkProgressDialog.start(requireContext())

                    CoroutineScope(Dispatchers.Default).launch {
                        NetworkConnect.connectHTTPS("InsertBookReview.do",
                            jsonObject,
                            requireContext()// 실패했을때 Toast 메시지를 띄워주기 위한 Context
                            , onSuccess = { ->
                                Toast.makeText(
                                    context,
                                    "${binding.ratingBarStar.rating}점으로 리뷰를 등록했습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()

//                                youngsProgress.endProgressBar(binding.progressbar,dialog?.window!!)
                                youngsProgressDialog.end()
                                dismiss()
                            }
                            , onFailure = {
//                                youngsProgress.endProgressBar(binding.progressbar,dialog?.window!!)
                                youngsProgressDialog.end()
                          }
                        )

                    }

                }
                else if (status == Define.STATUS_UPDATE)
                {
                    if (binding.editTextBookName.text.isNullOrBlank()) {
                        Toast.makeText(context, "책 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                        return
                    } else if (binding.editTextBookReview.text.isNullOrBlank()) {
                        binding.editTextBookReview.setText(" ")
                    }

                    
                    jsonObject.addProperty("review_no", MyBookListAdapter.instance.currentItem!!.REVIEW_NO)
//                    youngsProgress.startProgress(binding.progressbar,dialog?.window!!)
                    youngsProgressDialog.start(requireContext())
                    CoroutineScope(Dispatchers.Default).launch {
                        NetworkConnect.connectHTTPS("UpdateBookReview.do",
                            jsonObject,
                            requireContext()// 실패했을때 Toast 메시지를 띄워주기 위한 Context
                            , onSuccess = { ->
                                Toast.makeText(
                                    context,
                                    "${binding.editTextBookName.text} 리뷰를 수정했습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                this@WriteBookReview.dismiss()

//                                youngsProgress.endProgressBar(binding.progressbar,dialog?.window!!)
                                youngsProgressDialog.end()
                            }
                            , onFailure = {
//                                youngsProgress.endProgressBar(binding.progressbar,dialog?.window!!)
                                youngsProgressDialog.end()
                            }
                        )
                    }
                }
            }
        })

        binding.buttonDelete.setOnClickListener()
        {
            val jsonObject : JsonObject = JsonObject()
            jsonObject.addProperty("review_no", MyBookListAdapter.instance.currentItem?.REVIEW_NO)
//            youngsProgress.startProgress(binding.progressbar,dialog?.window!!)
            youngsProgressDialog.start(requireContext())
            CoroutineScope(Dispatchers.Default).launch {
                NetworkConnect.connectHTTPS("DeleteBookReview.do",
                    jsonObject,
                    requireContext()// 실패했을때 Toast 메시지를 띄워주기 위한 Context
                    , onSuccess = { ->
                        Toast.makeText(
                            context,
                            "${binding.editTextBookName.text}를 삭제했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

//                        youngsProgress.endProgressBar(binding.progressbar,dialog?.window!!)
                        youngsProgressDialog.end()
                        this@WriteBookReview.dismiss()
                    }
                , onFailure = {
//                        youngsProgress.endProgressBar(binding.progressbar,dialog?.window!!)
                    youngsProgressDialog.end()
                    }
                )
            }
        }

        binding.buttonScanBarCode.setOnClickListener(View.OnClickListener {

            val integrator = IntentIntegrator(activity)
            integrator.captureActivity = ScanBookActivity::class.java
            integrator.setBarcodeImageEnabled(false) //스캔 된 이미지 가져올 지
            integrator.setBeepEnabled(false)//스캔 시 비프음 ON/OFF
            integrator.setOrientationLocked(false) // flase로 하면 핸드폰 방향에 따라 가로, 세로가 바뀐다.
            integrator.setPrompt("책의 바코드를 읽어주세요")//QR 스캐너 하단 메세지 셋팅
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES) // QR 코드, 바코드 모두 인식하도록 설정


            activityResultLauncher.launch(integrator.createScanIntent())


            integrator.initiateScan() //초기화
        })

        binding.ImageButtonGoalReadDate.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                YoungsFunction.setDate(requireContext(),binding.textviewGoalReadDate,"-")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result.contents == null) {
            Log.e("this", "잘못된 QR코드입니다.")
        } else {
            Log.e("this", result.contents.toString())
        }
    }


    override fun dismiss() {
        super.dismiss()
        onClickListener.whenDismiss()
    }

}