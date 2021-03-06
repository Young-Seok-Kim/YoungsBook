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
import com.youngsbook.common.network.NetworkProgress
import com.youngsbook.common.scan.ScanBookModel
import com.youngsbook.databinding.WriteBookReviewBinding
import com.youngsbook.ui.main.MainActivityAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class WriteBookReview : DialogFragment() {

    lateinit var binding: WriteBookReviewBinding
    private lateinit var sharedPrefer : SharedPreferences
    val youngsProgress = NetworkProgress()

    var status : String = "Default"
    private var scanBook : JSONObject? = null
    private var scanBookModel : ScanBookModel? = null

    private val childForResult : ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    scanBook = YoungsFunction.bookSearch(result.data?.extras?.getString("SCAN_RESULT").toString())
                    scanBook?.let { scanAction(it) }
//                    binding.editTextBookName.text = scanBook.
                }
            }
        }


    private lateinit var onClickListener: OnDialogDismissListener

    interface OnDialogDismissListener
    {
        fun whenDismiss()
    }
    fun scanAction(scanBook : JSONObject){
        Log.d("QR ?????? ?????? ??????", scanBook.toString())
        if ((scanBook["items"] as JSONArray).isNull(0))
        {
            Toast.makeText(context,"?????? ????????? ????????????.",Toast.LENGTH_LONG).show()
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
        sharedPrefer = requireActivity().getSharedPreferences(SharedPreference.LOGIN_INFO,AppCompatActivity.MODE_PRIVATE)

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
        binding.textviewGoalReadDate.text = MainActivityAdapter.instance.currentItem?.GOAL_READ_DATE

        if(MainActivityAdapter.instance.currentItem?.READ_COMPLETE == "1")
            binding.checkboxReadComplete.isChecked = true

        if (status == Define.STATUS_INSERT)
        {
            binding.buttonDelete.visibility = View.GONE
        }
        else if (status == Define.STATUS_UPDATE)
        {
            binding.editTextBookName.setText(MainActivityAdapter.instance.currentItem?.BOOK_NAME)
            binding.editTextBookName.isEnabled = false
            binding.editTextBookReview.setText(MainActivityAdapter.instance.currentItem?.REVIEW)
            binding.ratingBarStar.rating = MainActivityAdapter.instance.currentItem?.STAR_RATING ?: 0F // ????????? 0.0
            binding.buttonScanBarCode.visibility = View.GONE
        }
    }


    private fun initButton() {
        binding.buttonSave.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                if (binding.editTextBookName.text.length >= 200)
                {
                    Toast.makeText(context,"??? ????????? ??? ?????? ??????????????????.",Toast.LENGTH_LONG).show()
                    return
                }

                val jsonObject: JsonObject = JsonObject() // insert??? update?????? ???????????? ??????????????? ???????????? ??????
                jsonObject.addProperty("book_name", binding.editTextBookName.text.toString())
                jsonObject.addProperty("read_date", YoungsFunction.getNowDate())
                jsonObject.addProperty("goal_read_date", binding.textviewGoalReadDate.text.toString())
                jsonObject.addProperty("review",binding.editTextBookReview.text.toString())
                jsonObject.addProperty("star_rating", binding.ratingBarStar.rating)
                jsonObject.addProperty("read_complete", binding.checkboxReadComplete.isChecked)

                if (status == Define.STATUS_INSERT) {

                    if (binding.editTextBookName.text.isNullOrBlank()) {
                        Toast.makeText(context, "??? ????????? ??????????????????", Toast.LENGTH_SHORT).show()
                        return
                    } else if (binding.editTextBookReview.text.isNullOrBlank()) {
                        binding.editTextBookReview.setText(" ")
                    }


                    jsonObject.addProperty(
                        "reader_id",
                        sharedPrefer.getString(SharedPreference.LOGIN_ID, " ")
                    )
                    jsonObject.addProperty(
                        "reader_name",
                        sharedPrefer.getString(SharedPreference.LOGIN_NAME, " ")
                    )
                    
                    youngsProgress.startProgress(binding.progressbar)
                    youngsProgress.notTouchable(dialog?.window!!)

                    CoroutineScope(Dispatchers.Default).launch {
                        NetworkConnect.connectHTTPS("InsertBookReview.do",
                            jsonObject,
                            requireContext()// ??????????????? Toast ???????????? ???????????? ?????? Context
                            , onSuccess = { ->
                                Toast.makeText(
                                    context,
                                    "${binding.ratingBarStar.rating}????????? ????????? ??????????????????.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                youngsProgress.endProgressBar(binding.progressbar)
                                youngsProgress.touchable(dialog?.window!!)

                                dismiss()
                            }
                            , onFailure = {
                                youngsProgress.endProgressBar(binding.progressbar)
                                youngsProgress.touchable(dialog?.window!!)
                            }
                        )

                    }

                }
                else if (status == Define.STATUS_UPDATE)
                {
                    if (binding.editTextBookName.text.isNullOrBlank()) {
                        Toast.makeText(context, "??? ????????? ??????????????????", Toast.LENGTH_SHORT).show()
                        return
                    } else if (binding.editTextBookReview.text.isNullOrBlank()) {
                        binding.editTextBookReview.setText(" ")
                    }

                    
                    jsonObject.addProperty("review_no", MainActivityAdapter.instance.currentItem!!.REVIEW_NO)
                    youngsProgress.startProgress(binding.progressbar)
                    youngsProgress.notTouchable(dialog?.window!!)

                    CoroutineScope(Dispatchers.Default).launch {
                        NetworkConnect.connectHTTPS("UpdateBookReview.do",
                            jsonObject,
                            requireContext()// ??????????????? Toast ???????????? ???????????? ?????? Context
                            , onSuccess = { ->
                                Toast.makeText(
                                    context,
                                    "${binding.editTextBookName.text} ????????? ??????????????????.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                this@WriteBookReview.dismiss()

                                youngsProgress.endProgressBar(binding.progressbar)
                                youngsProgress.touchable(dialog?.window!!)

                            }
                            , onFailure = {
                                youngsProgress.endProgressBar(binding.progressbar)
                                youngsProgress.touchable(dialog?.window!!)
                            }
                        )
                    }
                }
            }
        })

        binding.buttonDelete.setOnClickListener()
        {
            val jsonObject : JsonObject = JsonObject()
            jsonObject.addProperty("review_no", MainActivityAdapter.instance.currentItem?.REVIEW_NO)
            youngsProgress.startProgress(binding.progressbar)
            youngsProgress.notTouchable(dialog?.window!!)
            CoroutineScope(Dispatchers.Default).launch {
                NetworkConnect.connectHTTPS("DeleteBookReview.do",
                    jsonObject,
                    requireContext()// ??????????????? Toast ???????????? ???????????? ?????? Context
                    , onSuccess = { ->
                        Toast.makeText(
                            context,
                            "${binding.editTextBookName.text}??? ??????????????????.",
                            Toast.LENGTH_SHORT
                        ).show()

                        youngsProgress.endProgressBar(binding.progressbar)
                        youngsProgress.touchable(dialog?.window!!)

                        this@WriteBookReview.dismiss()
                    }
                , onFailure = {
                        youngsProgress.endProgressBar(binding.progressbar)
                        youngsProgress.touchable(dialog?.window!!)
                    }
                )
            }
        }

        binding.buttonScanBarCode.setOnClickListener(View.OnClickListener {
            val integrator = IntentIntegrator(requireActivity()) //context??? ???????????????
            integrator.setBarcodeImageEnabled(false) //?????? ??? ????????? ????????? ???
            integrator.setBeepEnabled(false)//?????? ??? ????????? ON/OFF
            integrator.setPrompt("?????? ???????????? ???????????????")//QR ????????? ?????? ????????? ??????
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)

            childForResult.launch(integrator.createScanIntent())
            integrator.initiateScan() //?????????
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
            Log.e("this", "????????? QR???????????????.")
        } else {
            Log.e("this", result.contents.toString())
        }
    }


    override fun dismiss() {
        super.dismiss()
        onClickListener.whenDismiss()
    }

}