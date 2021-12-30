/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.base

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kr.or.kreb.ncms.mobile.BizListActivity
import kr.or.kreb.ncms.mobile.util.Constants
import kr.or.kreb.ncms.mobile.util.DialogUtil
import kr.or.kreb.ncms.mobile.util.LogUtil
import kr.or.kreb.ncms.mobile.util.ToastUtil
import kotlin.system.exitProcess

abstract class BaseActivity<T : ViewDataBinding>(private val layoutId: Int, activityName: String) : AppCompatActivity() {

    lateinit var binding: T
    private var isSetBackButtonValid = false

    lateinit var dialogUtil: DialogUtil
    lateinit var dialogBuilder: MaterialAlertDialogBuilder

    val tag     : String by lazy { activityName }
    val log     : LogUtil by lazy { LogUtil(tag::class.java.simpleName) }
    val toast   : ToastUtil by lazy { ToastUtil(applicationContext) }

    var downloadRootPath : String? = null
    var backPressedTime :Long = 0

    /**
     * 레이아웃을 띄운 직후 호출.
     * 뷰나 액티비티의 속성 등을 초기화.
     * ex) 리사이클러뷰, 툴바, 드로어뷰..
     */
    abstract fun initViewStart()

    /**
     * 두번째로 호출.
     * 데이터 바인딩 및 rxjava 설정.
     * ex) rxjava observe, databinding observe..
     */
    abstract fun initDataBinding()

    /**
     * 가장 마지막에 호출. 바인딩 이후에 할 일을 여기에 구현.
     * 그 외에 설정할 것이 있으면 이곳에서 설정.
     * 클릭 리스너도 이곳에서 설정.
     */
    abstract fun initViewFinal()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        downloadRootPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path

        setContentView(layoutId)
        initViewStart()
        initDataBinding()
        initViewFinal()

    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            println("가로")

            when(getPageCode()){
                2 -> {
                    (this as BizListActivity).gridManager = GridLayoutManager(this, 2)
                    recylerViewBizMain.layoutManager = gridManager
                }
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            println("세로")

            when(getPageCode()){
                2 -> {
                    (this as BizListActivity).layoutManager = LinearLayoutManager(this)
                    layoutManager.orientation = LinearLayoutManager.VERTICAL
                    recylerViewBizMain.layoutManager = layoutManager
                }
            }
        }
    }

    fun setPageCode(code: Int){ Constants.PAGE_ACT = code }

    fun getPageCode(): Int = Constants.PAGE_ACT

    /**
     * 툴바 세팅
     */

    fun setToolBar(toolbar: MaterialToolbar){
        setSupportActionBar(toolbar)
        supportActionBar
    }

    /**
     * 기본 툴바를 썼을 때 뒤로가기 버튼을 넣는 코드
     */
    fun setBackButtonAboveActionBar(titleShow: Boolean, titleString: String?) {

        setSupportActionBar(appToolbar)

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            if (!titleShow){
                setDisplayShowTitleEnabled(false)
            } else {
                titleString?.let {
                    title = it
                }
            }
        }
        isSetBackButtonValid = true
    }

    override fun onBackPressed() {

        dialogUtil = DialogUtil(this, this)
        dialogBuilder = MaterialAlertDialogBuilder(this)

        when (getPageCode()) {
            Constants.BIZ_LIST_ACT -> {
                if(System.currentTimeMillis() - backPressedTime < 2000){
                    ActivityCompat.finishAffinity(this)
                    System.runFinalization()
                    exitProcess(0)
                }
                ToastUtil(this).msg_warning("뒤로가기 버튼을 한번 더 누르면 종료됩니다.", 1000)
                backPressedTime = System.currentTimeMillis()

            }
            Constants.CAMERA_ACT, Constants.LOGIN_ACT -> super.onBackPressed()
        }

    }

    override fun finish() {
        super.finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            //toolbar의 back키 눌렀을 때 동작
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}