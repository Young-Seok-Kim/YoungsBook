package com.youngsbook.ui.login

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.gson.JsonObject
import com.youngsbook.common.Data
import com.youngsbook.common.Define
import com.youngsbook.databinding.ActivityLoginBinding

import com.youngsbook.common.YoungsFunction

import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.common.network.SelfSigningHelper
import com.youngsbook.common.network.SslConnect.postHttps
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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        initButton()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initButton() {
        binding.buttonLogin!!.setOnClickListener {
            // 로그인 버튼 클릭시 이벤트
//            Log.d("raw Test", applicationContext.resources.openRawResource(R.raw.test).read().toString())

            postHttps(Define.BASE_URL_HTTPS_DEBUG, 5, 5)


            if (checkBeforeLogin()) { // 아이디를 1자리, 비밀번호를 6자리 이상 입력했는지 체크
                val enterLogin : JsonObject = JsonObject()
                enterLogin.addProperty("ID", binding.userid!!.text.toString())
                enterLogin.addProperty("PASSWORD", binding.password.text.toString())
                NetworkConnect.startProgress(this) // 종료는 connectNetwork 안에서 해주므로 따로 해줄 필요는 없다
                CoroutineScope(Dispatchers.Default).launch {
                    SelfSigningHelper(context = applicationContext)

                    NetworkConnect.connectHTTPS("login.do",
                        enterLogin,
                        applicationContext // 실패했을때 Toast 메시지를 띄워주기 위한 Context
                        , onSuccess = { ->
                            val jsonArray : JSONArray
                            jsonArray = YoungsFunction.stringToJson(NetworkConnect.resultString)

                            if(jsonArray[0].toString().isBlank()) {
                                Toast.makeText(applicationContext,"아이디, 비밀번호를 확인해주시기 바랍니다.",Toast.LENGTH_SHORT).show()
//                                return@connectHTTPS
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
                            } else {
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
                binding.userid?.text
                return@setOnClickListener
            }
        }
        binding.buttonSignUp!!.setOnClickListener(){
            SignUp().let{
                it.showNow(supportFragmentManager,"")
                it.dialog?.window?.setWindowAnimations(android.R.style.Animation_Dialog)
            }
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