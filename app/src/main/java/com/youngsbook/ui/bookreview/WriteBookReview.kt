package com.youngsbook.ui.bookreview

import android.R
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.gson.JsonObject
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.skydoves.balloon.*
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
    lateinit var balloon: Balloon
    val youngsProgressDialog = NetworkProgressDialog

    private var scanBook : JSONObject? = null
    private var scanBookModel : ScanBookModel? = null

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
        balloon = Balloon.Builder(requireContext())
            .setWidthRatio(1.0f)
            .setHeight(BalloonSizeSpec.WRAP)
            .setText("책의 바코드를 스캔해보세요!")
//        .setTextColorResource(R.color.white_87)
            .setTextSize(15f)
        .setIconDrawableResource(R.drawable.ic_dialog_info)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowSize(10)
            .setArrowPosition(0.5f)
            .setPadding(12)
            .setCornerRadius(8f)
//        .setBackgroundColorResource(ContextCompat.getColor(this,R.color.))
            .setBalloonAnimation(BalloonAnimation.ELASTIC)
//        .setLifecycleOwner(lifecycle)
            .build()

        binding.buttonBarcodeTip.setOnClickListener(){
            balloon.showAlignBottom(binding.buttonScanBarCode)
        }


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

        if (arguments?.getString("status") == "I")
        {
            binding.buttonDelete.visibility = View.GONE
            binding.buttonScanBarCode.visibility = View.VISIBLE
            binding.linearLayoutBarcode.visibility = View.VISIBLE
        }
        else if (arguments?.getString("status") == "U")
        {
            binding.editTextBookName.setText(MyBookListAdapter.instance.currentItem?.BOOK_NAME)
            binding.editTextBookName.isEnabled = false
            binding.editTextBookReview.setText(MyBookListAdapter.instance.currentItem?.REVIEW)
            binding.ratingBarStar.rating = MyBookListAdapter.instance.currentItem?.STAR_RATING ?: 0F // 기본값 0.0
            binding.linearLayoutBarcode.visibility = View.GONE
        }
    }


    private fun initButton() {
        binding.buttonSave.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val jsonObject: JsonObject = JsonObject() // insert와 update에서 동일하게 필요한값은 이곳에서 추가
                jsonObject.addProperty("book_name", binding.editTextBookName.text.toString())
                jsonObject.addProperty("read_date", YoungsFunction.getNowDate())
                jsonObject.addProperty("goal_read_date", binding.textviewGoalReadDate.text.toString())
                jsonObject.addProperty("review",binding.editTextBookReview.text.toString())
                jsonObject.addProperty("star_rating", binding.ratingBarStar.rating)
                jsonObject.addProperty("read_complete", binding.checkboxReadComplete.isChecked)

                if (arguments?.getString("status") == "I") {

                    if (binding.editTextBookName.text.isNullOrBlank()) {
                        Toast.makeText(context, "책 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                        return
                    } else if (binding.editTextBookReview.text.isNullOrBlank()) {
                        binding.editTextBookReview.setText(" ")
                    }


                    jsonObject.addProperty("reader_id", Define.NOW_LOGIN_USER_ID)
                    jsonObject.addProperty("reader_name",Define.NOW_LOGIN_USER_NAME)
                    
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

                                youngsProgressDialog.end()
                                dismiss()
                            }
                            , onFailure = {
                                youngsProgressDialog.end()
                          }
                        )

                    }

                }
                else if (arguments?.getString("status") == "U")
                {
                    if (binding.editTextBookName.text.isNullOrBlank()) {
                        Toast.makeText(context, "책 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                        return
                    } else if (binding.editTextBookReview.text.isNullOrBlank()) {
                        binding.editTextBookReview.setText(" ")
                    }

                    
                    jsonObject.addProperty("review_no", MyBookListAdapter.instance.currentItem!!.REVIEW_NO)
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

                                youngsProgressDialog.end()
                            }
                            , onFailure = {
                                youngsProgressDialog.end()
                            }
                        )
                    }
                }
            }
        })

        binding.buttonDelete.setOnClickListener()
        {
            YoungsFunction.messageBoxOKCancelAction(requireContext(),"경고","${MyBookListAdapter.instance.currentItem?.BOOK_NAME}의 리뷰를 삭제하시겠습니까?"
            , OKAction = {
                    val jsonObject: JsonObject = JsonObject()
                    jsonObject.addProperty(
                        "review_no",
                        MyBookListAdapter.instance.currentItem?.REVIEW_NO
                    )
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

                                youngsProgressDialog.end()
                                this@WriteBookReview.dismiss()
                            }, onFailure = {
                                youngsProgressDialog.end()
                            }
                        )
                    }

            },cancelAction = {

                })

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