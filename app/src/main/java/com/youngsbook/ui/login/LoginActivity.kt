package com.youngsbook.ui.login

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.youngsbook.databinding.ActivityLoginBinding

import com.youngsbook.R
import com.youngsbook.common.YoungsFunction

import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap


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

        sharedPreferences = getSharedPreferences("loginInfo", MODE_PRIVATE)
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

    private fun initButton() {
//        println("test")
        binding.buttonLogin!!.setOnClickListener {
            // 로그인 버튼 클릭시 이벤트
//            println(binding.userid)
//            Log.d("WTF", binding?.userid?.text?.toString() ?: "wtfwtf")
//            binding.userid

            if (checkBeforeLogin()) { // 아이디를 1자리, 비밀번호를 6자리 이상 입력했는지 체크
                var test : String = ""

                val enterLogin : JsonObject = JsonObject()
                enterLogin.addProperty("ID", binding.userid!!.text.toString())
                enterLogin.addProperty("PASSWORD", binding.password.text.toString())
                NetworkConnect.startProgress(this) // 종료는 connectNetwork 안에서 해주므로 따로 해줄 필요는 없다
                CoroutineScope(Dispatchers.Default).launch {
                    NetworkConnect.connectNetwork("login.do",
                        enterLogin,
                        applicationContext // 실패했을때 Toast 메시지를 띄워주기 위한 Context
                        , onSuccess = { ->
                            val jsonArray : JSONArray
                            jsonArray = YoungsFunction.stringToJson(NetworkConnect.resultString)

                            if(jsonArray[0].toString().isBlank()) {
                                Toast.makeText(applicationContext,"아이디, 비밀번호를 확인해주시기 바랍니다.",Toast.LENGTH_SHORT).show()
                                return@connectNetwork
                            }
                            if (binding.checkboxSaveLoginInfo!!.isChecked) { // 자동로그인이 클릭되었을때
                                editor.putBoolean(getString(R.string.login_information), true)
                                editor.putString(
                                    getString(R.string.auto_login_id),
                                    binding.userid!!.text.toString()
                                )
                                editor.putString(
                                    getString(R.string.auto_login_password),
                                    binding.password.text.toString()
                                )
                                editor.commit()
                            } else {
                                editor.putBoolean(getString(R.string.login_information), false)
                                editor.putString(getString(R.string.auto_login_id), "")
                                editor.putString(getString(R.string.auto_login_password), "")
                                editor.commit()
                            }
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
            NetworkConnect.startProgress(this)
            NetworkConnect.endProgress()
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
        binding.checkboxSaveLoginInfo!!.isChecked = sharedPreferences.getBoolean(getString(R.string.login_information),false)
        binding.userid!!.setText(sharedPreferences.getString(getString(R.string.auto_login_id),""))
        binding.password.setText(sharedPreferences.getString(getString(R.string.auto_login_password),""))
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