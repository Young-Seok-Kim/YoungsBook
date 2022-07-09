package com.youngsbook.ui.signup

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonObject
import com.youngsbook.common.Data
import com.youngsbook.common.YoungsFunction
import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.common.network.NetworkProgress
import com.youngsbook.databinding.SignUpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class SignUp : DialogFragment() {

    lateinit var binding: SignUpBinding
    private lateinit var sharedPrefer : SharedPreferences

    val youngsProgress = NetworkProgress()

    private var auth : FirebaseAuth = Firebase.auth // 전화번호 인증을 위한 변수
    var verificationId = "" // 인증번호 저장을 위한 변수


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignUpBinding.inflate(layoutInflater)

        sharedPrefer = requireActivity().getSharedPreferences(Data.instance.LOGIN_INFO,AppCompatActivity.MODE_PRIVATE)
        auth = Firebase.auth

        initButton()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }


    private fun initButton() {

        binding.joinButton.setOnClickListener() {

            if(binding.editTextName.text.isNullOrBlank()){
                YoungsFunction.messageBoxOK(requireContext(), "오류!", "이름을 입력해주세요" )
                return@setOnClickListener
            }
            else if(binding.editTextID.text.isNullOrBlank()){
                YoungsFunction.messageBoxOK(requireContext(), "오류!", "아이디를 입력해주세요" )
                return@setOnClickListener
            }
            else if (binding.editTextPassword.text.toString() != binding.editTextPasswordCheck.text.toString()) {
                YoungsFunction.messageBoxOK(requireContext(), "오류!", "비밀번호와 비밀번호 확인이 일치하지 않습니다." )
                return@setOnClickListener
            }
            else if(binding.editTextPasswordCheck.text.length < 6){
                YoungsFunction.messageBoxOK(requireContext(), "오류!", "비밀번호는 6자리 이상 입력해주세요" )
                return@setOnClickListener
            }
            else if(binding.editTextEmail.text.isNullOrBlank()){
                YoungsFunction.messageBoxOK(requireContext(), "오류!", "이메일을 입력해주세요" )
                return@setOnClickListener
            }


            if( ! binding.checkboxCertifyValue.isChecked)
            {
                return@setOnClickListener
            }

            val jsonObject: JsonObject = JsonObject()
            jsonObject.addProperty("NAME", binding.editTextName.text.toString())
            jsonObject.addProperty("ID", binding.editTextID.text.toString())
            jsonObject.addProperty("PASSWORD", binding.editTextPassword.text.toString())
            jsonObject.addProperty("EMAIL", binding.editTextEmail.text.toString())
            jsonObject.addProperty("PHONE_NUMBER", binding.editTextPhoneNumber.text.toString())
            jsonObject.addProperty("SIGNUP_DATE", YoungsFunction.getNowDate())

            youngsProgress.startProgress(binding.progressbar)
            youngsProgress.notTouchable(window = dialog?.window!!)
            CoroutineScope(Dispatchers.Default).launch {
                NetworkConnect.connectHTTPS("SignUp.do",
                    jsonObject,
                    requireContext()// 실패했을때 Toast 메시지를 띄워주기 위한 Context
                    , onSuccess = { ->
                        val jsonArray : JSONArray
                        jsonArray = YoungsFunction.stringToJson(NetworkConnect.resultString)

                        if ((jsonArray.get(0) as JSONObject).get("countID").toString().toInt() > 1)
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

        binding.buttonSendCertifyNumber.setOnClickListener(){
            if(binding.editTextPhoneNumber.text.toString().length < 4) {
                Toast.makeText(requireContext(),"휴대폰번호를 입력해주세요",Toast.LENGTH_SHORT).show()
                Log.d("휴대폰번호",binding.editTextInputCertifyNumber.text.toString())
                return@setOnClickListener
            }


            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) { }
                override fun onVerificationFailed(e: FirebaseException) {
                }
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@SignUp.verificationId = verificationId
                }
            }

            val optionsCompat =  PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(YoungsFunction.phoneNumber82(binding.editTextPhoneNumber.text.toString()))
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(optionsCompat)
            auth.setLanguageCode("kr")

            binding.linearLayoutCertifyNumber.visibility = View.VISIBLE

            Toast.makeText(context,"60초이내에 인증번호를 입력해주세요.",Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.Main).launch {
                binding.buttonSendCertifyNumber.isEnabled = false
                delay(60000) // 60초간 비활성화
                binding.buttonSendCertifyNumber.isEnabled = true
            }
        }

        binding.buttonCertifyNumber.setOnClickListener(){
            if(binding.editTextInputCertifyNumber.text.toString().length < 6) {
                Toast.makeText(requireContext(),"인증번호 6자리를 입력해주세요",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val credential = PhoneAuthProvider.getCredential(verificationId, binding.editTextInputCertifyNumber.text.toString())
            signInWithPhoneAuthCredential(credential)
        }
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    //인증성공
                    Toast.makeText(requireContext(),"휴대폰 인증성공",Toast.LENGTH_SHORT).show()
                    binding.checkboxCertifyValue.isChecked = true
                }
                else {
                    //인증실패
                    Toast.makeText(requireContext(),"인증번호를 확인해주세요",Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createAccount(email: String, password: String) { // Firebase 이메일로 가입하기

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireContext(), "계정 생성 완료.",
                            Toast.LENGTH_SHORT
                        ).show()
        //                        finish() // 가입창 종료
                    } else {
                        Toast.makeText(
                            requireContext(), "계정 생성 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

}