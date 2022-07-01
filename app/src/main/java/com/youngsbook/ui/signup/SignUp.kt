package com.youngsbook.ui.signup

import android.content.SharedPreferences
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
import com.youngsbook.common.network.NetworkProgress
import com.youngsbook.databinding.SignUpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class SignUp : DialogFragment() {

    lateinit var binding: SignUpBinding
    private lateinit var sharedPrefer : SharedPreferences

    val youngsProgress = NetworkProgress()

    lateinit var status : String

    private lateinit var onClickListener: OnDialogDismissListener

    interface OnDialogDismissListener
    {
        fun whenDismiss()
    }

    fun setOnDismissListener(listener: OnDialogDismissListener)
    {
        this@SignUp.onClickListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignUpBinding.inflate(layoutInflater)

        sharedPrefer = requireActivity().getSharedPreferences(Data.instance.LOGIN_INFO,AppCompatActivity.MODE_PRIVATE)

        initButton()

    }

    override fun onResume() {
        super.onResume()
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) // 터치 불가능 코드 회수
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }


    private fun initButton() {


        binding.joinButton.setOnClickListener() {
            if (binding.editTextPassword.text.toString() != binding.editTextPasswordCheck.text.toString()) {
                YoungsFunction.messageBoxOK(requireContext(), "오류!", "비밀번호와 비밀번호 확인이 일치하지 않습니다." )
                return@setOnClickListener
            }
            else if(binding.editTextPasswordCheck.text.length < 7){
                YoungsFunction.messageBoxOK(requireContext(), "오류!", "비밀번호는 7자리 이상 입력해주세요" )
                return@setOnClickListener
            }
            else if(binding.editTextName.text.isNullOrBlank()){
                YoungsFunction.messageBoxOK(requireContext(), "오류!", "이름을 입력해주세요" )
                return@setOnClickListener
            }
            else if(binding.editTextID.text.isNullOrBlank()){
                YoungsFunction.messageBoxOK(requireContext(), "오류!", "아이디를 입력해주세요" )
                return@setOnClickListener
            }
            else if(binding.editTextEmail.text.isNullOrBlank()){
                YoungsFunction.messageBoxOK(requireContext(), "오류!", "이메일을 입력해주세요" )
                return@setOnClickListener
            }

            val jsonObject: JsonObject = JsonObject()
            jsonObject.addProperty("NAME", binding.editTextName.text.toString())
            jsonObject.addProperty("ID", binding.editTextID.text.toString())
            jsonObject.addProperty("PASSWORD", binding.editTextPassword.text.toString())
            jsonObject.addProperty("EMAIL", binding.editTextEmail.text.toString())
            youngsProgress.startProgress(context = requireContext(),binding.progressbar)
            youngsProgress.notTouchable(window = dialog?.window!!)
            CoroutineScope(Dispatchers.Default).launch {
                NetworkConnect.connectHTTPS("SignUp.do",
                    jsonObject,
                    requireContext()// 실패했을때 Toast 메시지를 띄워주기 위한 Context
                    , onSuccess = { ->
                        val jsonArray : JSONArray
                        jsonArray = YoungsFunction.stringToJson(NetworkConnect.resultString)

                        if ((jsonArray.get(0) as JSONObject)?.get("countID").toString().toInt() > 1)
                        {
                            Toast.makeText(
                                context,
                                "동일한 아이디가 존재합니다",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.editTextID.requestFocus()
                        }
                        else {
                            Toast.makeText(
                                context,
                                "${binding.editTextID.text}님 가입을 환영합니다!",
                                Toast.LENGTH_SHORT
                            ).show()
                            this@SignUp.dismiss()
                        }

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

}