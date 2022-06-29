package com.youngsbook.ui.login

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.youngsbook.BuildConfig
import com.youngsbook.common.Data
import com.youngsbook.common.Define
import com.youngsbook.common.YoungsFunction
import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.common.network.SelfSigningHelper
import com.youngsbook.common.network.SslConnect.postHttps
import com.youngsbook.databinding.ActivityLoginBinding
import com.youngsbook.ui.main.MainActivity
import com.youngsbook.ui.signup.SignUp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor
    private val resultIntent : Intent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater,null,false)
//        EqMstrDtlBinding.inflate(inflater, container, false)
        setContentView(binding.root)
        binding.appVersion?.text = "Version : ${BuildConfig.VERSION_NAME}"

        sharedPreferences = getSharedPreferences("login_Info", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        checkSharedPreference() // 저장된 값을 가져오는 함수

        val userid = binding.userid
        val password = binding.password
        val login = binding.buttonLogin
        val loading = binding.loading

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            if (loginState.usernameError != null) {
                userid?.error = getString(loginState.usernameError)
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

        userid?.afterTextChanged {
            loginViewModel.loginDataChanged(
                userid.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    userid?.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            userid?.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

        }

//        initButton()
    }

    override fun onStart() {
        super.onStart()

        if(BuildConfig.DEBUG)
            binding.buttonTest?.visibility = View.VISIBLE
        else
            binding.buttonTest?.visibility = View.GONE

        initButton()
    }

    override fun onResume() {
        super.onResume()
//        versionCheck()
    }

    private fun versionCheck()
    {
            val jsonObject : JsonObject = JsonObject()
            jsonObject.addProperty("clientAppVersion", BuildConfig.VERSION_NAME)
            NetworkConnect.startProgress(this) // 종료는 connectNetwork 안에서 해주므로 따로 해줄 필요는 없다
            CoroutineScope(Dispatchers.Default).launch {
                NetworkConnect.connectHTTPS("versionCheck.do",
                    jsonObject,
                    applicationContext // 실패했을때 Toast 메시지를 띄워주기 위한 Context
                    , onSuccess = { ->
//                        MainActivityAdapter.instance.clear()
                        val jsonArray : JSONArray
                        jsonArray = YoungsFunction.stringToJson(NetworkConnect.resultString)
                        Log.d("버전체크 jsonObject.toString()", jsonObject.toString())
                        Log.d("버전체크 NetworkConnect.resultString", NetworkConnect.resultString)
                        
                        if(jsonArray[0].toString().toBoolean() == true)
                            Log.d("업데이트여부","필요함")
                        else
                            Log.d("업데이트여부","필요없음")

                    }
                )
            }
    }


    private fun initButton() {
        binding.buttonLogin!!.setOnClickListener(View.OnClickListener {

            postHttps(Define.BASE_URL_HTTPS_DEBUG, 5, 5)

            if(binding.userid?.text.isNullOrBlank() || binding.password.text.isNullOrBlank())
            {
                Toast.makeText(applicationContext,"아이디 혹은 비밀번호를 입력해주시기 바랍니다.",Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            if (checkBeforeLogin()) { // 아이디를 1자리, 비밀번호를 6자리 이상 입력했는지 체크
                val enterLogin : JsonObject = JsonObject()
                enterLogin.addProperty("ID", this@LoginActivity.binding.userid!!.text.toString())
                enterLogin.addProperty("PASSWORD", this@LoginActivity.binding.password.text.toString())
                NetworkConnect.startProgress(this) // 종료는 connectNetwork 안에서 해주므로 따로 해줄 필요는 없다
                CoroutineScope(Dispatchers.Default).launch {
                    SelfSigningHelper(context = applicationContext)

                    NetworkConnect.connectHTTPS("login.do",
                        enterLogin,
                        applicationContext // 실패했을때 Toast 메시지를 띄워주기 위한 Context
                        , onSuccess = { ->
                            val jsonArray : JSONArray
                            jsonArray = YoungsFunction.stringToJson(NetworkConnect.resultString)

                            if(jsonArray.get(0).toString().isBlank())
                            {
                                Toast.makeText(applicationContext,"아이디, 비밀번호가 맞지 않습니다.",Toast.LENGTH_LONG).show()
                                return@connectHTTPS
                            }
                            editor.putString( // 로그인한 아이디 저장
                                Data.instance.LOGIN_ID,
                                (jsonArray.get(0) as JSONObject).getString("ID")
                            )
                            editor.putString( // 로그인한 비밀번호
                                // 저장
                                Data.instance.LOGIN_PASSWORD,
                                (jsonArray.get(0) as JSONObject).getString("PASSWORD")
                            )
                            editor.putString( // 로그인한 이름 저장
                                Data.instance.LOGIN_NAME,
                                (jsonArray.get(0) as JSONObject).getString("NAME")
                            )
                            if (binding.checkboxSaveLoginInfo!!.isChecked) { // 자동로그인이 클릭되었을때
                                editor.putBoolean(Data.instance.AUTO_LOGIN_BOOLEAN, true)
                                editor.putString(
                                    Data.instance.AUTO_LOGIN_ID,
                                    (jsonArray.get(0) as JSONObject).getString("ID")
                                )
                                editor.putString(
                                    Data.instance.AUTO_LOGIN_PASSWORD,
                                    (jsonArray.get(0) as JSONObject).getString("PASSWORD")
                                )
                            } else { // 자동로그인이 클리되어있지 않을때
                                editor.putBoolean(Data.instance.AUTO_LOGIN_BOOLEAN, false)
                                editor.putString(Data.instance.AUTO_LOGIN_ID, "")
                                editor.putString(Data.instance.AUTO_LOGIN_PASSWORD, "")
                            }

                            editor.commit()
                            loginViewModel.login(binding.userid!!.text.toString(), binding.password.text.toString())
                            openMainActivity()


                        }
                    )

                }

            }
            else
            {
                Toast.makeText(applicationContext, "아이디, 비밀번호를 규칙에 맞게 입력해주세요.",Toast.LENGTH_SHORT).show()
//                binding.userid?.text
                return@OnClickListener
            }

        })
        binding.buttonSignUp!!.setOnClickListener(){
            SignUp().let{
                it.showNow(supportFragmentManager,"")
                it.dialog?.window?.setWindowAnimations(android.R.style.Animation_Dialog)
            }
        }

        binding.buttonTest?.setOnClickListener(){
            versionCheck()
        }

    }

    private fun checkBeforeLogin() : Boolean // 아이디와 비밀번호를 자리수에 맞게 입력하는지 확인
    {
        if((binding.userid?.text?.length ?: 0) >= 1 && binding.password.text.length >= 6)
            return true
        
        return false
    }

    fun checkSharedPreference()
    {
        binding.checkboxSaveLoginInfo!!.isChecked = sharedPreferences.getBoolean(Data.instance.AUTO_LOGIN_BOOLEAN,false)
        binding.userid!!.setText(sharedPreferences.getString(Data.instance.AUTO_LOGIN_ID,""))
        binding.password.setText(sharedPreferences.getString(Data.instance.AUTO_LOGIN_PASSWORD,""))
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
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