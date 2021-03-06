package com.youngsbook.ui.login

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.youngsbook.BuildConfig
import com.youngsbook.R
import com.youngsbook.common.SharedPreference
import com.youngsbook.common.YoungsContextFunction
import com.youngsbook.common.YoungsFunction
import com.youngsbook.common.network.NetworkConnect
import com.youngsbook.common.network.NetworkProgress
import com.youngsbook.common.network.SelfSigningHelper
import com.youngsbook.databinding.ActivityLoginBinding
import com.youngsbook.ui.main.MainActivity
import com.youngsbook.ui.signUp.FindUserInformation
import com.youngsbook.ui.signUp.SignUp
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
    val youngsProgress = NetworkProgress()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater,null,false)
//        EqMstrDtlBinding.inflate(inflater, container, false)

        setContentView(binding.root)
        binding.appVersion.text = "Version : ${BuildConfig.VERSION_CODE} (${BuildConfig.VERSION_NAME})${if(BuildConfig.DEBUG) ", Debug" else ""}"

        sharedPreferences = getSharedPreferences("login_Info", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        checkSharedPreference() // ????????? ?????? ???????????? ??????

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
            binding.buttonTest.visibility = View.VISIBLE
        }
        else
            binding.buttonTest.visibility = View.GONE

    }


    override fun onResume() {
        super.onResume()

        initButton()
        YoungsContextFunction().loadAD(this,binding.adBanner)
    }


    private fun openPlayStore(){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.youngsbook"))
        startActivity(intent)
    }


    private fun initButton() {

        binding.buttonSignUp.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                SignUp().let{
                    it.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialogTheme)
                    it.dialog?.window?.setWindowAnimations(android.R.style.Animation_Dialog)
                    it.showNow(supportFragmentManager,"")
                }
            }
        })

        binding.forgotLoginInfo.setOnClickListener(){
            FindUserInformation().let{
                it.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialogTheme)
                it.dialog?.window?.setWindowAnimations(android.R.style.Animation_Dialog)

                it.showNow(supportFragmentManager,"")
            }
        }
        binding.buttonLogin.setOnClickListener() {
            youngsProgress.startProgress(this@LoginActivity.binding.progressbar)
            youngsProgress.notTouchable(window)


            if(binding.userid.text.isNullOrBlank() || binding.password.text.isNullOrBlank())
            {
                Toast.makeText(applicationContext,"????????? ?????? ??????????????? ?????????????????? ????????????.",Toast.LENGTH_SHORT).show()
                youngsProgress.endProgressBar(binding.progressbar)
                youngsProgress.touchable(window)
                return@setOnClickListener
            }

            if (checkBeforeLogin()) { // ???????????? 1??????, ??????????????? 6?????? ?????? ??????????????? ??????
                val enterLogin : JsonObject = JsonObject()
                enterLogin.addProperty("ID", this@LoginActivity.binding.userid.text.toString().replace(" ",""))
                enterLogin.addProperty("PASSWORD", this@LoginActivity.binding.password.text.toString().replace(" ",""))

                CoroutineScope(Dispatchers.Default).launch {
                    SelfSigningHelper(context = applicationContext)

                    NetworkConnect.connectHTTPS("login.do",
                        enterLogin,
                        applicationContext // ??????????????? Toast ???????????? ???????????? ?????? Context
                        , onSuccess = { ->
                            val jsonArray : JSONArray
                            jsonArray = YoungsFunction.stringArrayToJson(NetworkConnect.resultString)

                            if(jsonArray.get(0).toString().isBlank())
                            {
                                Toast.makeText(applicationContext,"?????????, ??????????????? ?????? ????????????.",Toast.LENGTH_LONG).show()
                                youngsProgress.endProgressBar(binding.progressbar)
                                youngsProgress.touchable(window)
                                return@connectHTTPS
                            }
                            editor.putString( // ???????????? ????????? ??????
                                SharedPreference.LOGIN_ID,
                                (jsonArray.get(0) as JSONObject).getString("ID")
                            )
                            editor.putString( // ???????????? ????????????
                                // ??????
                                SharedPreference.LOGIN_PASSWORD,
                                (jsonArray.get(0) as JSONObject).getString("PASSWORD")
                            )
                            editor.putString( // ???????????? ?????? ??????
                                SharedPreference.LOGIN_NAME,
                                (jsonArray.get(0) as JSONObject).getString("NAME")
                            )
                            if (binding.checkboxSaveLoginInfo.isChecked) { // ?????????????????? ??????????????????
                                editor.putBoolean(SharedPreference.AUTO_LOGIN_BOOLEAN, true)
                                editor.putString(
                                    SharedPreference.AUTO_LOGIN_ID,
                                    (jsonArray.get(0) as JSONObject).getString("ID")
                                )
                                editor.putString(
                                    SharedPreference.AUTO_LOGIN_PASSWORD,
                                    (jsonArray.get(0) as JSONObject).getString("PASSWORD")
                                )
                            } else { // ?????????????????? ?????????????????? ?????????
                                editor.putBoolean(SharedPreference.AUTO_LOGIN_BOOLEAN, false)
                                editor.putString(SharedPreference.AUTO_LOGIN_ID, "")
                                editor.putString(SharedPreference.AUTO_LOGIN_PASSWORD, "")
                            }

                            editor.commit()
                            loginViewModel.login(binding.userid.text.toString(), binding.password.text.toString())

                            openMainActivity()


                            youngsProgress.endProgressBar(binding.progressbar)
                            youngsProgress.touchable(window)

                        }
                    , onFailure = {
                            youngsProgress.endProgressBar(binding.progressbar)
                            youngsProgress.touchable(window)
                        }
                    )

                }


            }
            else
            {
                Toast.makeText(applicationContext, "?????????, ??????????????? ????????? ?????? ??????????????????.",Toast.LENGTH_SHORT).show()
                youngsProgress.endProgressBar(binding.progressbar)
                youngsProgress.touchable(window)
                return@setOnClickListener
            }
        }

        binding.buttonTest.setOnClickListener(){

        }

    }


    private fun checkBeforeLogin() : Boolean // ???????????? ??????????????? ???????????? ?????? ??????????????? ??????
    {
        if((binding.userid.text?.length ?: 0) >= 1 && binding.password.text.length >= 6)
            return true
        
        return false
    }

    fun checkSharedPreference()
    {
        binding.checkboxSaveLoginInfo.isChecked = sharedPreferences.getBoolean(SharedPreference.AUTO_LOGIN_BOOLEAN,false)
        binding.userid.setText(sharedPreferences.getString(SharedPreference.AUTO_LOGIN_ID,""))
        binding.password.setText(sharedPreferences.getString(SharedPreference.AUTO_LOGIN_PASSWORD,""))
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