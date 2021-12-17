package com.youngsbook.ui.signup

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.gson.JsonObject
import com.youngsbook.common.Data
import com.youngsbook.common.YoungsFunction
import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.databinding.SignUpBinding
import com.youngsbook.databinding.WriteBookReviewBinding
import com.youngsbook.ui.main.MainActivity
import com.youngsbook.ui.main.MainActivityAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUp : DialogFragment() {

    lateinit var binding: SignUpBinding
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
        this@SignUp.onClickListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignUpBinding.inflate(layoutInflater)
        sharedPrefer = requireActivity().getSharedPreferences(Data.instance.login_Info,AppCompatActivity.MODE_PRIVATE)

        initButton()

    }

    override fun onResume() {
        super.onResume()
        // 전체화면으로 만드는 코드
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
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
            NetworkConnect.startProgress(requireContext()) // 종료는 connectNetwork 안에서 해주므로 따로 해줄 필요는 없다
            CoroutineScope(Dispatchers.Default).launch {
                NetworkConnect.connectNetwork("SignUp.do",
                    jsonObject,
                    requireContext()// 실패했을때 Toast 메시지를 띄워주기 위한 Context
                    , onSuccess = { ->
                        Toast.makeText(
                            context,
                            "${binding.editTextID.text}님 가입을 환영합니다!",
                            Toast.LENGTH_SHORT
                        ).show()
                        this@SignUp.dismiss()
                    }
                )
            }
        }
    }

}