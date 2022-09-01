package com.youngsbook.ui.signUp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.youngsbook.common.network.NetworkProgressDialog
import com.youngsbook.common.network.SelfSigningHelper
import com.youngsbook.databinding.FindUserInformationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.concurrent.TimeUnit


class FindUserInformation : DialogFragment() {

    lateinit var binding: FindUserInformationBinding

    val youngsProgressDialog = NetworkProgressDialog

    private var auth : FirebaseAuth = Firebase.auth // 전화번호 인증을 위한 변수
    var verificationId = "" // 인증번호 저장을 위한 변수


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FindUserInformationBinding.inflate(layoutInflater)

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
        }

        binding.buttonFindUserInformation.setOnClickListener(object :  View.OnClickListener{
            override fun onClick(p0: View?) {
                if( ! binding.checkboxCertifyValue.isChecked)
                {
                    Toast.makeText(context, "휴대폰인증을 진행해주세요",Toast.LENGTH_SHORT).show()
                    return
                }

//                youngsProgress.startProgress(binding.progressbar,dialog?.window!!)
                youngsProgressDialog.start(requireContext())
                if(binding.radiobuttonFindID.isChecked){ // 아이디 찾기
                    val jsonToServer : JsonObject = JsonObject()
                    jsonToServer.addProperty("PHONE_NUMBER", this@FindUserInformation.binding.editTextPhoneNumber.text.toString())

                    CoroutineScope(Dispatchers.Default).launch {
                        SelfSigningHelper(context = requireContext())

                        NetworkConnect.connectHTTPS("findUserID.do",
                            jsonToServer,
                            requireContext() // 실패했을때 Toast 메시지를 띄워주기 위한 Context
                            , onSuccess = { ->
                                val jsonArray : JSONArray = YoungsFunction.stringArrayToJson(NetworkConnect.resultString)
                                if (jsonArray.get(0).toString().isBlank()) {
                                    Toast.makeText(requireContext(),"해당정보로 가입된 회원이 없습니다.",Toast.LENGTH_LONG).show()
//                                    youngsProgress.endProgressBar(binding.progressbar,dialog?.window!!)
                                    youngsProgressDialog.end()
                                    return@connectHTTPS
                                }


                                YoungsFunction.messageBoxOK(requireContext(),"정보","회원님의 아이디는 ${jsonArray.getJSONObject(0).get("ID")}입니다.")

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
                else if(binding.radiobuttonFindPassword.isChecked){ // 비밀번호 찾기

                    if(binding.editTextPasswordCheck.text.length < 6){
                        YoungsFunction.messageBoxOK(requireContext(), "오류!", "비밀번호는 6자리 이상 입력해주세요" )
//                        youngsProgress.endProgressBar(binding.progressbar,dialog?.window!!)
                        youngsProgressDialog.end()
                        return
                    }

                    if((binding.editTextPasswordCheck.text.toString() != binding.editTextPassword.text.toString()) == true)
                    {
                        YoungsFunction.messageBoxOK(requireContext(), "오류!", "비밀번호와 비밀번호 확인이 같지않습니다." )
//                        youngsProgress.endProgressBar(binding.progressbar,dialog?.window!!)
                        youngsProgressDialog.end()
                        return
                    }

                    val sendJson : JsonObject = JsonObject()
                    sendJson.addProperty("UPDATE_PASSWORD", this@FindUserInformation.binding.editTextPasswordCheck.text.toString())
                    sendJson.addProperty("PHONE_NUMBER", this@FindUserInformation.binding.editTextPhoneNumber.text.toString())

                    CoroutineScope(Dispatchers.Default).launch {
                        SelfSigningHelper(context = requireContext())

                        NetworkConnect.connectHTTPS("updateUserPassword.do",
                            sendJson,
                            requireContext() // 실패했을때 Toast 메시지를 띄워주기 위한 Context
                            , onSuccess = { ->
                                val userCount : Int = YoungsFunction.stringIntToJson(NetworkConnect.resultString)

                                if (userCount == 0)
                                {
                                    YoungsFunction.messageBoxOK(requireContext(),"정보","해당번호로 가입된 회원이 없습니다.")
                                }
                                else{
                                    Toast.makeText(context,"비밀번호가 재설정 되었습니다.", Toast.LENGTH_LONG).show()
                                }
//                                youngsProgress.endProgressBar(binding.progressbar,dialog?.window!!)
                                youngsProgressDialog.end()
                                dismiss()

                            }
                            , onFailure = {
                                binding.linearLayoutResetPassword.visibility = View.VISIBLE
//                                youngsProgress.endProgressBar(binding.progressbar,dialog?.window!!)
                                youngsProgressDialog.end()
                            }
                        )

                    }
                }

            }
        })

        binding.radiobuttonFindPassword.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                binding.linearLayoutResetPassword.visibility = View.VISIBLE
            }
        })

        binding.radiobuttonFindID.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                binding.linearLayoutResetPassword.visibility = View.GONE
            }
        })

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful && !binding.checkboxCertifyValue.isChecked) {
                    //인증성공
                    Toast.makeText(requireContext(),"휴대폰 인증성공",Toast.LENGTH_SHORT).show()
                    binding.editTextPhoneNumber.isEnabled = false
                    binding.checkboxCertifyValue.isChecked = true
                    binding.editTextPassword.isEnabled = true
                    binding.editTextPasswordCheck.isEnabled = true
                    binding.buttonCertifyNumber.isEnabled = false
                }
                else if (!task.isSuccessful){
                    //인증실패
                    Toast.makeText(requireContext(),"인증번호를 확인해주세요",Toast.LENGTH_SHORT).show()
                }
            }
    }

}