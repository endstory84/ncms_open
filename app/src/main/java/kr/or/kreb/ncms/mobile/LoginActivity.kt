/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile

import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jiransoft.mdm.library.MDMLib
import com.jiransoft.mdm.library.Services.OnMangobananaCompleteListener
import kotlinx.android.synthetic.main.activity_login.*
import kr.or.kreb.ncms.mobile.base.BaseActivity
import kr.or.kreb.ncms.mobile.databinding.ActivityLoginBinding
import kr.or.kreb.ncms.mobile.fragment.ConfirmDialogFragment
import kr.or.kreb.ncms.mobile.util.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.ExecutionException

class LoginActivity :
    BaseActivity<ActivityLoginBinding>(R.layout.activity_login, LoginActivity::class.java.simpleName),
    ConfirmDialogFragment.ConfirmDialogListener,
    OnMangobananaCompleteListener {

    private val USERINFO_EMP_CD     = "empCd"
    private val USERINFO_EMP_NM     = "empNm"
    private val USERINFO_DEPT_CD    = "deptCd"
    private val USERINFO_DEPT_NM    = "deptNm"
    private val USERINFO_OFCPS      = "ofcps"
    private val USERINFO_CLSF       = "clsf"


    lateinit var confirmDialogFragment: ConfirmDialogFragment

    private var _isIdSaveCheck = false
    private var _isValidationCheck = false
    private var _isPermissionCheck = false

//    private lateinit var preferences: SharedPreferences
//    private lateinit var editor: SharedPreferences.Editor

    private var loginId: String = ""

    private var mdmLib: MDMLib? = null

    private var mdmHandler: Handler? = null

//    private var MDM_HOST: String = "mdm.reb.or.kr:44300"
//    private var MDM_COMPANY: String = "20032300"

    /**
     * MDM 사용유무
     */
    private val USE_MDM: Boolean = false


    override fun initViewStart() {

        initUI()

        if (USE_MDM) {
            settingMDM()
        }

    }

    override fun initDataBinding() {}

    override fun initViewFinal() {

        dialogUtil = DialogUtil(this, this)

        editTextLoginId.addTextChangedListener(CustomTextWatcher(editTextLoginId))
        editTextLoginPassword.addTextChangedListener(CustomTextWatcher(editTextLoginPassword))
        imageViewBtnLogin.setOnClickListener { connectLogin() }
        checkboxLoginSaverUser.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.id == R.id.checkboxLoginSaverUser) _isIdSaveCheck = isChecked
        }

        // 로그인정보 저장
        val isSavedId = PreferenceUtil.getLoginIsSaveId(this)
        val savedId = PreferenceUtil.getLoginId(this)

        checkboxLoginSaverUser.isChecked = isSavedId

        // ID 저장 & 저장된 ID가 존재
        if ( isSavedId && savedId.isNotEmpty() ) {
            editTextLoginId.setText( savedId )
            editTextLoginPassword.requestFocus()
            editTextLoginPassword.setSelection(0)
        }
        else {
            editTextLoginId.requestFocus()
            editTextLoginId.setSelection(0)
        }

//        preferences = getSharedPreferences("appLogin", 0)
//        editor = preferences.edit()
//
//        // 로그인정보 저장
//        if (preferences.getBoolean("loginCheck", false)) {
//            editTextLoginId.setText(preferences.getString("ID",""))
//            checkboxLoginSaverUser.isChecked = true
//            editTextLoginPassword.requestFocus()
//        } else {
//            editTextLoginId.requestFocus()
//        }

        permissionCheck()

    }

    override fun onResume() {
        super.onResume()
        setPageCode(Constants.LOGIN_ACT)
    }

    /**
     * 퍼미션 체크
     */
    private fun permissionCheck() {
        PermissionUtil.hasPermission(this, this)
    }

    private fun setPermissionAlert() {
        confirmDialogFragment = ConfirmDialogFragment(R.drawable.ic_notice, getString(R.string.msg_permission_title), getString(R.string.msg_permission_content), getString(R.string.msg_alert_y), getString(R.string.msg_alert_n),)

        if(!confirmDialogFragment.isVisible) {
            confirmDialogFragment.show(supportFragmentManager,"confirmDialog")
        }
    }

    /**
     * 권한 요청 결과
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {

        when (requestCode) {
            PermissionUtil.PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty()) {
                    for((i, permission) in permissions.withIndex()) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            log.d("권한획득 실패-> $permission")
                            _isPermissionCheck = false
                        } else {
                            log.d("권한획득 성공-> $permission")
                            _isPermissionCheck = true
                        }
                    }

                    if(!_isPermissionCheck){
                        val resultPermission = PermissionUtil.shouldShowRequestPermissionRationale(this)
                        if (resultPermission) {
                            toast.msg_error(getString(R.string.msg_permsission_status_fail), 100)
                            setPermissionAlert()
                        } else {
                            toast.msg_error(getString(R.string.msg_permission_fail), 100)
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        PermissionUtil.launchPermissionSettings(this)
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
       dialog.dismiss()
        finish()
    }

    private fun initUI() {

        setTheme(R.style.NCMS_AppTheme)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setStatusBarColor()
        }

        Constants.PAGE_ACT = Constants.LOGIN_ACT

        textInputId.isCounterEnabled = true
        textInputId.counterMaxLength = 10

    }

    /**
     * 로그인
     */

    private fun connectLogin() {
        try {
            if (_isValidationCheck) {
                if (!validateName()) return
                if (!validatePassword()) return

                loginId = editTextLoginId.text.toString()
                val idPass: String = editTextLoginPassword.text.toString()
                log.d("$loginId, $idPass")
                saveId() //아이디 저장

                if (USE_MDM) {
                    mdmLib?.mangobanana(this, mdmHandler, loginId)
                }
                else {
                    reqLogin(loginId, idPass)
                }

            } else {

                toast.msg_error(getString(R.string.msg_login_validation_fail), 500)

            }

        } catch (e: ExecutionException) {
            log.e(e.toString())
        } catch (e: InterruptedException) {
            log.e(e.toString())
        }

    }

    fun reqLogin(id: String, pwd: String) {
        val reqLoginUrl = resources.getString(R.string.mobile_url) + "reqLogin"//"auth.do"
        val loginMap = HashMap<String, String>()
        loginMap.put("id", encryptCBC(id))
        loginMap.put("pwd", encryptCBC(pwd))

        val progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(this))

        HttpUtil.getInstance(this)
            .callerUrlInfoPostWebServer(loginMap, progressDialog, reqLoginUrl,
                object: Callback {

                    override fun onFailure(call: Call, e: IOException) {
                        progressDialog.dismiss()
                        runOnUiThread {
                            toast.msg_error(R.string.msg_server_login_fail, 100)
                        }

                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseString = response.body!!.string()

                        log.d("auth response : $responseString")

//                        val dataJSON = JSONObject(responseString).getJSONObject("list").getJSONArray("bsnsChoise") as JSONArray
                        progressDialog.dismiss()

                        val dataJSON = JSONObject(responseString)
                        if (dataJSON.has("userInfo")) {

                            val resultJSON = JSONObject(responseString).getJSONObject("userInfo")
                            val empCd = resultJSON.getString(USERINFO_EMP_CD)
                            val empNm = resultJSON.getString(USERINFO_EMP_NM)
                            val deptCd = resultJSON.getString(USERINFO_DEPT_CD)
                            val deptNm = resultJSON.getString(USERINFO_DEPT_NM)
                            val ofcps = resultJSON.getString(USERINFO_OFCPS)
                            val clsf = resultJSON.getString(USERINFO_CLSF)

                            PreferenceUtil.setUserInfo(
                                this@LoginActivity,
                                empCd,
                                empNm,
                                deptCd,
                                deptNm,
                                ofcps,
                                clsf
                            )

                            runOnUiThread {
                                toast.msg_success(getString(R.string.msg_login_validation_success), 500)
                                nextViewBizList(this@LoginActivity, Constants.BIZ_LIST_ACT, loginId)
                            }
                        }
                        else {
                            runOnUiThread {
                                toast.msg_error(R.string.msg_server_login_fail, 100)
                            }
                        }

                    }
                })
    }

    private fun saveId() {
        if (_isIdSaveCheck) {
//            val loginID: String = editTextLoginId.text.toString()
//            editor.putString("ID", loginID)
//            editor.putBoolean("loginCheck", true)
//            editor.commit()

            PreferenceUtil.setLoginInfo(this, true, loginId)
        } else {
//            editor.remove("ID")
//            editor.remove("loginCheck")
//            editor.clear()
//            editor.commit()

            PreferenceUtil.removeLoginInfo(this)
        }
    }

    /* 로그인 검증 */
    inner class CustomTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (view.id == R.id.editTextLoginPassword) {
//                if (editTextLoginPassword.text.toString().length > 4) {
                    imageViewBtnLogin.setBackgroundColor(resources.getColor(R.color.light_orange, theme))
                    imageViewBtnLogin.isClickable = true
                    _isValidationCheck = true
//                } else {
//                    imageViewBtnLogin.setBackgroundColor(resources.getColor(R.color.gray, theme))
//                    imageViewBtnLogin.isClickable = false
//                }
            }
        }

        override fun afterTextChanged(editable: Editable) {
            when (view.id) {
                R.id.editTextLoginId -> validateName()
                R.id.editTextLoginPassword -> validatePassword()
                else -> {
                }
            }
        }
    }

    private fun validateName(): Boolean {
        if (editTextLoginId.text.toString().isEmpty()) {
            textInputId.error = getString(R.string.login_err_msg_id)
            requestFocus(editTextLoginId)
            return false
        } else {
            textInputId.isErrorEnabled = false
        }
        return true
    }

    private fun validatePassword(): Boolean {
        if (editTextLoginPassword.text.toString().isEmpty()) {
            textInputPassword.error =
                getString(R.string.login_err_msg_pw); requestFocus(editTextLoginPassword); return false
        } else {
            textInputPassword.isErrorEnabled = false; _isValidationCheck = true
        }; return true
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    companion object {
        private val TAG: String? = LoginActivity::class.simpleName
    }

    /**
     * MDM 관련 설정
     */
    fun settingMDM() {
        mdmLib = MDMLib.getInstance(this, getString(R.string.MDM_HOST), getString(R.string.MDM_COMPANY))
        mdmLib?.setUserNotificationIcon(R.drawable.ic_stat_device_blocking)

        MDMLib.setDebugMode(true); // 실 운영서버 시 fasle

        mdmHandler = Handler()

        mdmLib?.setOnMangobananaCompleteListener(this)
        mdmLib?.startCurrentStatusCheckService(this, null)



//        val type: Int = MDMLib.MANGO_APPLE_TYPE_D
//
//        mdmLib?.mangoapple(this, type, this)

    }

    override fun onMangobananaComplete(code: String?, message: String?) {

        if ("0x00000000" == code) {

//            toast.msg_success(getString(R.string.msg_login_validation_success), 500)
////                nextView(this, Constants.BIZ_LIST_ACT, null, null, null, null)
//            nextViewBizList(this, Constants.BIZ_LIST_ACT, loginId)

            loginId = editTextLoginId.text.toString()
            val idPass: String = editTextLoginPassword.text.toString()
            reqLogin(loginId, idPass)

        } else {
            toast.msg_error(getString(R.string.msg_mdm_fail), 100)
            finish()
        }
    }

}