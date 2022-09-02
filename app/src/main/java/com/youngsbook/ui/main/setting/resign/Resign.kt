package com.youngsbook.ui.main.setting.resign

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.gson.JsonObject
import com.youngsbook.R
import com.youngsbook.common.Define
import com.youngsbook.common.YoungsFunction
import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.common.network.NetworkProgressDialog
import com.youngsbook.common.network.SelfSigningHelper
import com.youngsbook.databinding.ResignBinding
import com.youngsbook.ui.login.LoginActivity
import com.youngsbook.ui.findUserInformation.FindUserInformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Resign : DialogFragment() {
    lateinit var binding: ResignBinding
    val youngsProgressDialog = NetworkProgressDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ResignBinding.inflate(layoutInflater)

        binding.textViewIDValue.text = Define.NOW_LOGIN_USER_ID

        initButton()
    }

    private fun initButton() {
        binding.textViewFindUserInformation.setOnClickListener(){
            FindUserInformation().let{
                it.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialogTheme)
                it.dialog?.window?.setWindowAnimations(android.R.style.Animation_Dialog)
                it.showNow(childFragmentManager,"")
            }
        }

        binding.buttonOK.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                youngsProgressDialog.start(requireContext())
                if (!(binding.editTextPassword.text.toString() == binding.editTextPasswordCheck.text.toString()) )
                {
                    Toast.makeText(requireContext(),"비밀번호가 맞지 않습니다.",Toast.LENGTH_LONG).show()
                    youngsProgressDialog.end()
                    return
                }
                else if (binding.editTextPassword.text.toString().isBlank() || binding.editTextPasswordCheck.text.toString().isBlank())
                {
                    Toast.makeText(requireContext(),"비밀번호를 입력해주세요",Toast.LENGTH_LONG).show()
                    youngsProgressDialog.end()
                    return
                }

                val enterLogin : JsonObject = JsonObject()
                enterLogin.addProperty("ID", this@Resign.binding.textViewIDValue.text.toString().replace(" ",""))
                enterLogin.addProperty("PASSWORD", this@Resign.binding.editTextPassword.text.toString().replace(" ",""))
                enterLogin.addProperty("RESIGN_USER_CODE", Define.NOW_LOGIN_USER_CODE)

                CoroutineScope(Dispatchers.Default).launch {
                    SelfSigningHelper(context = requireContext())

                    NetworkConnect.connectHTTPS("resign.do",
                        enterLogin,
                        requireContext() // 실패했을때 Toast 메시지를 띄워주기 위한 Context
                        , onSuccess = { ->
                            val deleteCount = YoungsFunction.stringIntToJson(NetworkConnect.resultString)

                            if(deleteCount == 0)
                            {
                                Toast.makeText(requireContext(),"아이디, 비밀번호가 맞지 않습니다.", Toast.LENGTH_LONG).show()
                                youngsProgressDialog.end()
                                return@connectHTTPS
                            }

                            Define.NOW_LOGIN_USER_ID = ""
                            Define.NOW_LOGIN_USER_CODE = 0

                            Toast.makeText(requireContext(),"그동안 YoungsBook을 이용해주셔서 감사합니다.", Toast.LENGTH_LONG).show()

                            youngsProgressDialog.end()

                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            startActivity(intent)
                            activity?.finish()

                        }
                        , onFailure = {
                            youngsProgressDialog.end()
                        }
                    )

                }
            }
        })
    }
}