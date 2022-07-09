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
import com.youngsbook.common.Data
import com.youngsbook.common.YoungsFunction
import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.common.network.NetworkProgress
import com.youngsbook.common.network.SelfSigningHelper
import com.youngsbook.databinding.FindUserInformationBinding
import com.youngsbook.databinding.SignUpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class FindUserInformation : DialogFragment() {

    lateinit var binding: FindUserInformationBinding
    private lateinit var sharedPrefer : SharedPreferences

    val youngsProgress = NetworkProgress()

    private var auth : FirebaseAuth = Firebase.auth // 전화번호 인증을 위한 변수
    var verificationId = "" // 인증번호 저장을 위한 변수


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FindUserInformationBinding.inflate(layoutInflater)

        sharedPrefer = requireActivity().getSharedPreferences(Data.instance.LOGIN_INFO,AppCompatActivity.MODE_PRIVATE)
        auth = Firebase.auth

        initButton()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }


    private fun initButton() {



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
                    this@FindUserInformation.verificationId = verificationId
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

            CoroutineScope(Dispatchers.Main).launch { // 너무 빨리 인증번호를 확인하면 앱이 팅기기때문에 2초동안 기다리는 코드 추가
                binding.buttonCertifyNumber.isEnabled = false
                delay(2000)
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
        }

        binding.buttonFindUserInformation.setOnClickListener(object :  View.OnClickListener{
            override fun onClick(p0: View?) {
                if( ! binding.radiobuttonFindPassword.isChecked && ! binding.radiobuttonFindID.isChecked){
                    Toast.makeText(context,"찾을 정보를 선택해주세요", Toast.LENGTH_LONG).show()
                    return
                }


                if(binding.radiobuttonFindID.isChecked){
                    val enterLogin : JsonObject = JsonObject()
//                    enterLogin.addProperty("ID", this@FindUserInformation.binding.userid.text.toString().replace(" ",""))
//                    enterLogin.addProperty("PASSWORD", this@FindUserInformation.binding.password.text.toString().replace(" ",""))

                    CoroutineScope(Dispatchers.Default).launch {
                        SelfSigningHelper(context = requireContext())

                        NetworkConnect.connectHTTPS("findUserID.do",
                            enterLogin,
                            requireContext() // 실패했을때 Toast 메시지를 띄워주기 위한 Context
                            , onSuccess = { ->
                                if( ! binding.checkboxCertifyValue.isChecked) {
                                    Toast.makeText(context, "휴대폰 인증을 진행해주세요.", Toast.LENGTH_LONG).show()
                                    return@connectHTTPS
                                }

                                val jsonArray : JSONArray
                                jsonArray = YoungsFunction.stringToJson(NetworkConnect.resultString)

                                if(jsonArray.get(0).toString().isBlank())
                                {
                                    Toast.makeText(requireContext(),"아이디, 비밀번호가 맞지 않습니다.",Toast.LENGTH_LONG).show()
                                    youngsProgress.endProgressBar(binding.progressbar)
                                    youngsProgress.touchable(dialog?.window!! )
                                    return@connectHTTPS
                                }


                                youngsProgress.endProgressBar(binding.progressbar)
                                youngsProgress.touchable(window = dialog?.window!!)

                            }
                            , onFailure = {
                                youngsProgress.endProgressBar(binding.progressbar)
                                youngsProgress.touchable(dialog?.window!!)
                            }
                        )

                    }
                }
                else if(binding.radiobuttonFindPassword.isChecked){
                    val enterLogin : JsonObject = JsonObject()
//                    enterLogin.addProperty("ID", this@FindUserInformation.binding.userid.text.toString().replace(" ",""))
//                    enterLogin.addProperty("PASSWORD", this@FindUserInformation.binding.password.text.toString().replace(" ",""))

                    CoroutineScope(Dispatchers.Default).launch {
                        SelfSigningHelper(context = requireContext())

                        NetworkConnect.connectHTTPS("findUserPassword.do",
                            enterLogin,
                            requireContext() // 실패했을때 Toast 메시지를 띄워주기 위한 Context
                            , onSuccess = { ->
                                val jsonArray : JSONArray
                                jsonArray = YoungsFunction.stringToJson(NetworkConnect.resultString)

                                if(jsonArray.get(0).toString().isBlank())
                                {
                                    Toast.makeText(requireContext(),"아이디, 비밀번호가 맞지 않습니다.",Toast.LENGTH_LONG).show()
                                    youngsProgress.endProgressBar(binding.progressbar)
                                    youngsProgress.touchable(dialog?.window!! )
                                    return@connectHTTPS
                                }


                                youngsProgress.endProgressBar(binding.progressbar)
                                youngsProgress.touchable(window = dialog?.window!!)

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

        binding.radiobuttonFindPassword.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                binding.radiobuttonFindPassword.isChecked = true
//                binding.radiobuttonFindID.isChecked = false
            }
        })

        binding.radiobuttonFindID.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                binding.radiobuttonFindID.isChecked = true
//                binding.radiobuttonFindID.isChecked = false
            }
        })
//
//        binding.radiobuttonFindID.setOnClickListener(){
//            binding.linearLayoutResetPassword.visibility = View.GONE
//        }
//
//        binding.radiobuttonFindPassword.setOnClickListener(){
//            binding.linearLayoutResetPassword.visibility = View.VISIBLE
//        }
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

}