package com.youngsbook.ui.login

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.youngsbook.BuildConfig
import com.youngsbook.R
import com.youngsbook.common.Define
import com.youngsbook.common.SharedPreference
import com.youngsbook.common.YoungsContextFunction
import com.youngsbook.common.YoungsFunction
import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.common.network.NetworkProgress
import com.youngsbook.common.network.SelfSigningHelper
import com.youngsbook.databinding.ActivityLoginBinding
import com.youngsbook.ui.main.MainActivity
import com.youngsbook.ui.findUserInformation.FindUserInformation
import com.youngsbook.ui.signUp.SignUp
import org.json.JSONArray
import org.json.JSONObject
import com.kakao.sdk.user.model.User
import com.youngsbook.common.KakaoAPICall.getKakaoUserInfo
import com.youngsbook.parent.ParentActivity
import kotlinx.coroutines.*


class LoginActivity : ParentActivity()/*, KakaoLogin.IKLoginResult*/ {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor
    private val youngsProgress = NetworkProgress()

    var user: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater,null,false)

        setContentView(binding.root)

//        KakaoSdk.init(this, "418199908136a18cc519fe9dfbc48335")
//        kakaoLogin()


        binding.appVersion.text = "Version : ${BuildConfig.VERSION_CODE} (${BuildConfig.VERSION_NAME})${if(BuildConfig.DEBUG) ", Debug" else ""}"

        sharedPreferences = getSharedPreferences(SharedPreference.SAVE_LOGIN_INFO, MODE_PRIVATE)
        editor = sharedPreferences.edit()

        checkSharedPreference() // 저장된 값을 가져오는 함수

        val userid = binding.userid
        val password = binding.password
        val loading = binding.progressbar

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            if (loginState.usernameError != null) {
                userid.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        userid.afterTextChanged {
            loginViewModel.loginDataChanged(
                userid.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    userid.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            userid.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

        }

    }


    override fun onStart() {
        super.onStart()

        if(BuildConfig.DEBUG) {
            binding.buttonKakaoLogin.visibility = View.VISIBLE
        }
        else
        {
            binding.buttonKakaoLogin.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        initButton()
        if (binding.checkboxAutoLogin.isChecked && Define.whenLogin)
            binding.buttonLogin.performClick()
        YoungsContextFunction().loadAD(this,binding.adBanner)
    }

    private fun initButton() {

        binding.checkboxAutoLogin.setOnClickListener(object  : View.OnClickListener{
            override fun onClick(p0: View?) {
                if ( ! binding.checkboxSaveLoginInfo.isChecked)
                {
                    Toast.makeText(applicationContext,"로그인정보 저장을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
                    binding.checkboxAutoLogin.isChecked = false
                }
            }
        })

        binding.checkboxSaveLoginInfo.setOnClickListener(object  : View.OnClickListener{
            override fun onClick(p0: View?) {
                if (binding.checkboxAutoLogin.isChecked)
                {
                    Toast.makeText(applicationContext,"자동로그인을 먼저 해제해주세요.", Toast.LENGTH_SHORT).show()
                    binding.checkboxSaveLoginInfo.isChecked = true
                }
            }
        })


        binding.buttonSignUp.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                SignUp().let{
                    it.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialogTheme)
                    it.dialog?.window?.setWindowAnimations(android.R.style.Animation_Dialog)
                    it.showNow(supportFragmentManager,"")
                }
            }
        })

        binding.linearLayoutForgotLoginInfo.setOnClickListener(){
            FindUserInformation().let{
                it.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialogTheme)
                it.dialog?.window?.setWindowAnimations(android.R.style.Animation_Dialog)

                it.showNow(supportFragmentManager,"")
            }
        }

        binding.buttonLogin.setOnClickListener() {
            youngsProgress.startProgress(this@LoginActivity.binding.progressbar,window)

            if(binding.userid.text.isNullOrBlank() || binding.password.text.isNullOrBlank())
            {
                Toast.makeText(applicationContext,"아이디 혹은 비밀번호를 입력해주시기 바랍니다.",Toast.LENGTH_SHORT).show()
                youngsProgress.endProgressBar(binding.progressbar,window)
                return@setOnClickListener
            }

            if (checkBeforeLogin()) { // 아이디를 1자리, 비밀번호를 6자리 이상 입력했는지 체크
                val jsonObject : JsonObject = JsonObject()
                jsonObject.addProperty("IS_KAKAO_LOGIN", false)
                jsonObject.addProperty("ID", this@LoginActivity.binding.userid.text.toString().replace(" ",""))
                jsonObject.addProperty("PASSWORD", this@LoginActivity.binding.password.text.toString().replace(" ",""))

                CoroutineScope(Dispatchers.Default).launch {
                    SelfSigningHelper(context = applicationContext)

                    NetworkConnect.connectHTTPS("login.do",
                        jsonObject,
                        applicationContext // 실패했을때 Toast 메시지를 띄워주기 위한 Context
                        , onSuccess = { ->
                            val jsonArray : JSONArray = YoungsFunction.stringArrayToJson(NetworkConnect.resultString)

                            Define.whenLogin = true
                            Define.firstOpen = true

                            if(jsonArray.get(0).toString().isBlank())
                            {
                                Toast.makeText(applicationContext,"아이디, 비밀번호가 맞지 않거나\n아이디가 존재하지않습니다..",Toast.LENGTH_LONG).show()
                                youngsProgress.endProgressBar(binding.progressbar,window)
                                return@connectHTTPS
                            }

                            Define.NOW_LOGIN_USER_ID = (jsonArray.get(0) as JSONObject).getString("ID")
                            Define.NOW_LOGIN_USER_NAME = (jsonArray.get(0) as JSONObject).getString("NAME")
//                            Define.NOW_LOGIN_USER_CODE = (jsonArray.get(0) as JSONObject).getString("CODE").toInt()

                            editor.putString( // 로그인한 아이디 저장
                                SharedPreference.NOW_LOGIN_USER_ID,
                                (jsonArray.get(0) as JSONObject).getString("ID")
                            )
                            editor.putString( // 로그인한 이름 저장
                                SharedPreference.NOW_LOGIN_USER_NAME,
                                (jsonArray.get(0) as JSONObject).getString("NAME")
                            )
                            if (binding.checkboxSaveLoginInfo.isChecked) { // 로그인 정보 저장이 클릭되었을때
                                editor.putBoolean(SharedPreference.SAVE_LOGIN_INFO_BOOLEAN, true)
                                editor.putString(
                                    SharedPreference.SAVE_LOGIN_INFO_ID,
                                    (jsonArray.get(0) as JSONObject).getString("ID")
                                )
                                editor.putString(
                                    SharedPreference.SAVE_LOGIN_INFO_PASSWORD,
                                    (jsonArray.get(0) as JSONObject).getString("PASSWORD")
                                )
                            } else { // 로그인 정보 저장이 클리되어있지 않을때
                                editor.putBoolean(SharedPreference.SAVE_LOGIN_INFO_BOOLEAN, false)
                                editor.putString(SharedPreference.SAVE_LOGIN_INFO_ID, "")
                                editor.putString(SharedPreference.SAVE_LOGIN_INFO_PASSWORD, "")
                            }

                            if (binding.checkboxAutoLogin.isChecked) { // 자동로그인이 클릭되었을때
                                editor.putBoolean(SharedPreference.AUTO_LOGIN_BOOLEAN, true)
                            } else { // 자동로그인이 클리되어있지 않을때
                                editor.putBoolean(SharedPreference.AUTO_LOGIN_BOOLEAN, false)
                            }

                            editor.commit()
                            loginViewModel.login(binding.userid.text.toString(), binding.password.text.toString())

                            youngsProgress.endProgressBar(binding.progressbar,window)
                            openMainActivity()
                        }
                    , onFailure = {
                            youngsProgress.endProgressBar(binding.progressbar,window)
                        }
                    )

                }


            }
            else
            {
                Toast.makeText(applicationContext, "아이디, 비밀번호를 규칙에 맞게 입력해주세요.",Toast.LENGTH_SHORT).show()
                youngsProgress.endProgressBar(binding.progressbar,window)
                return@setOnClickListener
            }
        }



        binding.buttonKakaoLogin.setOnClickListener()
        {
            runBlocking {
                var mUser : User? = null
                GlobalScope.launch {
                    mUser = getKakaoUserInfo(context = this@LoginActivity)
                }.join()

                if (mUser == null)
                {
                    Toast.makeText(this@LoginActivity,"카카오 로그인을 진행해주세요",Toast.LENGTH_SHORT).show()
                    return@runBlocking
                }
                kakaoLogin(mUser)
            }
        }

    }


    private fun checkBeforeLogin() : Boolean // 아이디와 비밀번호를 자리수에 맞게 입력하는지 확인
    {
        if((binding.userid.text?.length ?: 0) >= 1 && binding.password.text.length >= 6)
            return true
        
        return false
    }

    private fun checkSharedPreference()
    {
        binding.checkboxSaveLoginInfo.isChecked = sharedPreferences.getBoolean(SharedPreference.SAVE_LOGIN_INFO_BOOLEAN,false)
        binding.checkboxAutoLogin.isChecked = sharedPreferences.getBoolean(SharedPreference.AUTO_LOGIN_BOOLEAN,false)
        binding.userid.setText(sharedPreferences.getString(SharedPreference.SAVE_LOGIN_INFO_ID,""))
        binding.password.setText(sharedPreferences.getString(SharedPreference.SAVE_LOGIN_INFO_PASSWORD,""))
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun kakaoLogin(pUser: User) {

        val jsonObject : JsonObject = JsonObject()
        jsonObject.addProperty("IS_KAKAO_LOGIN", true)
        jsonObject.addProperty("ID", pUser!!.id)
//        jsonObject.addProperty("PASSWORD", this@LoginActivity.binding.password.text.toString().replace(" ",""))

        CoroutineScope(Dispatchers.Default).launch {
            SelfSigningHelper(context = applicationContext)

            NetworkConnect.connectHTTPS("kakaoLogin.do",
                jsonObject,
                applicationContext // 실패했을때 Toast 메시지를 띄워주기 위한 Context
                , onSuccess = { ->
                    val jsonArray : JSONArray = YoungsFunction.stringArrayToJson(NetworkConnect.resultString)

                    Define.whenLogin = true
                    Define.firstOpen = true

                    if(jsonArray.get(0).toString().isBlank())
                    {
                        YoungsFunction.messageBoxOK(applicationContext,"Notice","카카오 가입을 진행하지 않았습니다. 카카오 가입을 진행해주세요.")
                        youngsProgress.endProgressBar(binding.progressbar,window)
                        return@connectHTTPS
                    }
//
//                    Define.NOW_LOGIN_USER_ID = (jsonArray.get(0) as JSONObject).getString("ID")
//                    Define.NOW_LOGIN_USER_NAME = (jsonArray.get(0) as JSONObject).getString("NAME")
////                            Define.NOW_LOGIN_USER_CODE = (jsonArray.get(0) as JSONObject).getString("CODE").toInt()
//
//                    editor.putString( // 로그인한 아이디 저장
//                        SharedPreference.NOW_LOGIN_USER_ID,
//                        (jsonArray.get(0) as JSONObject).getString("ID")
//                    )
//                    editor.putString( // 로그인한 이름 저장
//                        SharedPreference.NOW_LOGIN_USER_NAME,
//                        (jsonArray.get(0) as JSONObject).getString("NAME")
//                    )
//                    if (binding.checkboxSaveLoginInfo.isChecked) { // 로그인 정보 저장이 클릭되었을때
//                        editor.putBoolean(SharedPreference.SAVE_LOGIN_INFO_BOOLEAN, true)
//                        editor.putString(
//                            SharedPreference.SAVE_LOGIN_INFO_ID,
//                            (jsonArray.get(0) as JSONObject).getString("ID")
//                        )
//                        editor.putString(
//                            SharedPreference.SAVE_LOGIN_INFO_PASSWORD,
//                            (jsonArray.get(0) as JSONObject).getString("PASSWORD")
//                        )
//                    } else { // 로그인 정보 저장이 클리되어있지 않을때
//                        editor.putBoolean(SharedPreference.SAVE_LOGIN_INFO_BOOLEAN, false)
//                        editor.putString(SharedPreference.SAVE_LOGIN_INFO_ID, "")
//                        editor.putString(SharedPreference.SAVE_LOGIN_INFO_PASSWORD, "")
//                    }
//
//                    if (binding.checkboxAutoLogin.isChecked) { // 자동로그인이 클릭되었을때
//                        editor.putBoolean(SharedPreference.AUTO_LOGIN_BOOLEAN, true)
//                    } else { // 자동로그인이 클리되어있지 않을때
//                        editor.putBoolean(SharedPreference.AUTO_LOGIN_BOOLEAN, false)
//                    }
//
//                    editor.commit()
//                    loginViewModel.login(binding.userid.text.toString(), binding.password.text.toString())
//
//                    youngsProgress.endProgressBar(binding.progressbar,window)
//                    openMainActivity()
                }
                , onFailure = {
                    youngsProgress.endProgressBar(binding.progressbar,window)
                }
            )

        }


    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}