package com.youngsbook.ui.signUp

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
import com.youngsbook.common.YoungsFunction
import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.common.network.NetworkProgress
import com.youngsbook.databinding.SignUpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class SignUp : DialogFragment() {

    lateinit var binding: SignUpBinding

    val youngsProgress = NetworkProgress()

    private var auth : FirebaseAuth = Firebase.auth // 전화번호 인증을 위한 변수
    var verificationId = "" // 인증번호 저장을 위한 변수


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignUpBinding.inflate(layoutInflater)


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
//            else if(binding.editTextEmail.text.isNullOrBlank()){
//                YoungsFunction.messageBoxOK(requireContext(), "오류!", "이메일을 입력해주세요" )
//                return@setOnClickListener
//            }


            if( ! binding.checkboxCertifyValue.isChecked)
            {
                YoungsFunction.messageBoxOKCancelAction(requireContext(), "주의!!", "휴대폰인증을 하지 않을경우 아이디, 비밀번호를 찾을수 없습니다.\n계속 진행하시겠습니까?",
                    OKAction = {
                        signUp()
                    },
                    cancelAction = {
                        return@messageBoxOKCancelAction
                    }
                )

//                return@setOnClickListener
            }
            else {
                    signUp()
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
                delay(20000) // 20초간 비활성화
                binding.buttonSendCertifyNumber.isEnabled = true
            }

            CoroutineScope(Dispatchers.Main).launch { // 너무 빨리 인증번호를 확인하면 앱이 팅기기때문에 2초동안 기다리는 코드 추가
                binding.buttonCertifyNumber.isEnabled = false
                delay(5000)
                binding.buttonCertifyNumber.isEnabled = true
            }
        }

        binding.buttonCertifyNumber.setOnClickListener(){
            if(binding.editTextInputCertifyNumber.text.toString().length < 6) {
                Toast.makeText(requireContext(),"인증번호 6자리를 입력해주세요",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val credential = PhoneAuthProvider.getCredential(verificationId, binding.editTextInputCertifyNumber.text.toString())
            signInWithPhoneAuthCredential(credential)
            binding.editTextPhoneNumber.isEnabled = false
        }
    }

    private fun signUp() {
        val jsonObject: JsonObject = JsonObject()
        jsonObject.addProperty("NAME", binding.editTextName.text.toString())
        jsonObject.addProperty("ID", binding.editTextID.text.toString())
        jsonObject.addProperty("PASSWORD", binding.editTextPassword.text.toString())
        jsonObject.addProperty("EMAIL", binding.editTextEmail.text.toString())
        if(binding.editTextPhoneNumber.text.toString().isNotBlank()) {
            jsonObject.addProperty("PHONE_NUMBER", binding.editTextPhoneNumber.text.toString())
        }
        jsonObject.addProperty("SIGNUP_DATE", YoungsFunction.getNowDate())

        youngsProgress.startProgress(binding.progressbar)
        youngsProgress.notTouchable(window = dialog?.window!!)
        CoroutineScope(Dispatchers.Default).launch {
            NetworkConnect.connectHTTPS("SignUp.do",
                jsonObject,
                requireContext()// 실패했을때 Toast 메시지를 띄워주기 위한 Context
                , onSuccess = { ->
                    val sameIDCheck : Int = YoungsFunction.stringIntToJson(NetworkConnect.resultString)

                    if (sameIDCheck >= 1) { // 해당 아이디로 검색했을때 한개라도 있으면 가입안되도록
                        Toast.makeText(
                            context,
                            "동일한 아이디 혹은 전화번호로 가입되어있습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.editTextID.requestFocus()
                    } else {
                        Toast.makeText(
                            context,
                            "${binding.editTextID.text}님 가입을 환영합니다!",
                            Toast.LENGTH_SHORT
                        ).show()
                        this@SignUp.dismiss()
                    }

                    youngsProgress.endProgressBar(binding.progressbar)
                    youngsProgress.touchable(dialog?.window!!)
                }, onFailure = {
                    youngsProgress.endProgressBar(binding.progressbar)
                    youngsProgress.touchable(dialog?.window!!)
                }
            )
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