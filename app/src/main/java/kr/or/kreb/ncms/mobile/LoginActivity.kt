/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.activity_login.*
import kr.or.kreb.ncms.mobile.base.BaseActivity
import kr.or.kreb.ncms.mobile.databinding.ActivityLoginBinding
import kr.or.kreb.ncms.mobile.fragment.ConfirmDialogFragment
import kr.or.kreb.ncms.mobile.util.*
import java.util.concurrent.ExecutionException

class LoginActivity :
    BaseActivity<ActivityLoginBinding>(R.layout.activity_login, LoginActivity::class.java.simpleName),
    ConfirmDialogFragment.ConfirmDialogListener {

    lateinit var confirmDialogFragment: ConfirmDialogFragment

    private var _isIdSaveCheck = false
    private var _isValidationCheck = false
    private var _isPermissionCheck = false

    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun initViewStart() = initUI()

    override fun initDataBinding() {}

    override fun initViewFinal() {
        editTextLoginId.addTextChangedListener(CustomTextWatcher(editTextLoginId))
        editTextLoginPassword.addTextChangedListener(CustomTextWatcher(editTextLoginPassword))
        imageViewBtnLogin.setOnClickListener { connectLogin() }
        checkboxLoginSaverUser.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.id == R.id.checkboxLoginSaverUser) _isIdSaveCheck = isChecked
        }

        preferences = getSharedPreferences("appLogin", 0)
        editor = preferences.edit()

        // 로그인정보 저장
        if (preferences.getBoolean("loginCheck", false)) {
            editTextLoginId.setText(preferences.getString("ID",""))
            checkboxLoginSaverUser.isChecked = true
            editTextLoginPassword.requestFocus()
        } else {
            editTextLoginId.requestFocus()
        }

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
//        editTextLoginId.setText("admin")
//        editTextLoginPassword.setText("1234")
    }

    /**
     * 로그인
     */

    private fun connectLogin() {
        try {
            if (_isValidationCheck) {
                if (!validateName()) return
                if (!validatePassword()) return
                saveId() //아이디 저장
                val idVal: String = editTextLoginId.text.toString()
                val idPass: String = editTextLoginPassword.text.toString()
                log.d("$idVal, $idPass")

                toast.msg_success(getString(R.string.msg_login_validation_success), 500)
//                nextView(this, Constants.BIZ_LIST_ACT, null, null, null, null)
                nextViewBizList(this, Constants.BIZ_LIST_ACT, idVal)
            } else {

                toast.msg_error(getString(R.string.msg_login_validation_fail), 500)
            }

        } catch (e: ExecutionException) {
            log.e(e.toString())
        } catch (e: InterruptedException) {
            log.e(e.toString())
        }

    }

    private fun saveId() {
        if (_isIdSaveCheck) {
            val loginID: String = editTextLoginId.text.toString()
            editor.putString("ID", loginID)
            editor.putBoolean("loginCheck", true)
            editor.commit()
        } else {
            editor.remove("ID")
            editor.remove("loginCheck")
            editor.clear()
            editor.commit()
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
}