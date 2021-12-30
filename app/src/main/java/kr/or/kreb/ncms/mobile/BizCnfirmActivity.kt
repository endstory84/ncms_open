/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile

import android.annotation.SuppressLint
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.android.synthetic.main.activity_biz_cnfirm.*
import kotlinx.android.synthetic.main.activity_biz_cnfirm_with_drawerlayout.*
import kotlinx.android.synthetic.main.include_bizinfo.*
import kotlinx.android.synthetic.main.include_drawnavigation.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kr.or.kreb.ncms.mobile.base.BaseActivity
import kr.or.kreb.ncms.mobile.databinding.ActivityBizCnfirmWithDrawerlayoutBinding
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.listener.NavSetItemListener
import kr.or.kreb.ncms.mobile.util.*

/**
 * @info 사업 확인
 */

class BizCnfirmActivity :
    BaseActivity<ActivityBizCnfirmWithDrawerlayoutBinding>(R.layout.activity_biz_cnfirm_with_drawerlayout, BizCnfirmActivity::class.java.simpleName),
    View.OnClickListener {

    override fun initViewStart() {

        setToolBar(appToolbar)
        setBackButtonAboveActionBar(true,"사업 확인")

        clearCameraValue()

        val getBizCategory = PreferenceUtil.getString(applicationContext, "bizCategory","")
        val getbsnsNm = PreferenceUtil.getString(applicationContext, "bsnsNm","")

        textViewBizInfoSubCategory.goneView()

        textViewBizInfoCategory.text = getBizCategory
        textViewBizInfoName.text =  getbsnsNm
        textViewBizInfoEmployee.text ="현장조사원: 홍길동"

        //setColorBizCategory(this, textViewBizInfoCategory, textViewBizInfoSubCategory)
    }

    override fun initDataBinding() {}

    @SuppressLint("ClickableViewAccessibility")
    override fun initViewFinal() {

        // Drawer On/Off
//        appToolbar.setNavigationOnClickListener {
//            layout_biz_cnfirm_drawer.openDrawer(GravityCompat.START)
//            imageViewDrawerClose.setOnClickListener { layout_biz_cnfirm_drawer.closeDrawer(GravityCompat.START) }
//        }

        layout_biz_cnfirm_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        val navListener = NavSetItemListener(this)
        navigationViewMain.setNavigationItemSelectedListener(navListener)

        buttonBizCnfirmLotMap.setOnClickListener(this)
        buttonBizCnfirmLad.setOnClickListener(this)
        buttonBizCnfirmThing.setOnClickListener(this)
        buttonBizCnfirmBsn.setOnClickListener(this)
        buttonBizCnfirmFarm.setOnClickListener(this)
        buttonBizCnfirmResidnt.setOnClickListener(this)
        buttonBizCnfirmTomb.setOnClickListener(this)
        buttonBizCnfirmMnidst.setOnClickListener(this)
        buttonBizCnfirmFshr.setOnClickListener(this)

        // 21.11.1 By KDS
        // TODO : 임시 잔여지 버튼 추가
        buttonBizCnfirmRestLAD.setOnClickListener(this)
        buttonBizCnfirmRestThing.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        setPageCode(Constants.BIZ_CNFIRM_ACT)
    }


    // TODO: 2021-06-08 사업별 확인 모바일 임시 코드 다시 재정의 해야함.
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.buttonBizCnfirmLotMap -> {
                PreferenceUtil.setString(applicationContext, "bizSubCategory","용지도")
                PreferenceUtil.setBiz(applicationContext, "bizSubCategoryKey", BizEnum.LOTMAP)
                nextView(this, Constants.MAP_ACT, BizEnum.LOTMAP, null)
            }
            R.id.buttonBizCnfirmLad -> {
                PreferenceUtil.setString(applicationContext, "bizSubCategory","토지")
                PreferenceUtil.setBiz(applicationContext, "bizSubCategoryKey", BizEnum.LAD)
                nextView( this, Constants.MAP_ACT, BizEnum.LAD, null)
            }
            R.id.buttonBizCnfirmThing -> {
                PreferenceUtil.setString(applicationContext, "bizSubCategory","지장물")
                PreferenceUtil.setBiz(applicationContext, "bizSubCategoryKey", BizEnum.THING)
                nextView( this, Constants.MAP_ACT, BizEnum.THING, null)
            }
            R.id.buttonBizCnfirmBsn -> {
                PreferenceUtil.setString(applicationContext, "bizSubCategory","영업ㆍ축산업ㆍ잠업")
                PreferenceUtil.setBiz(applicationContext,"bizSubCategoryKey", BizEnum.BSN)
                nextView( this, Constants.MAP_ACT, BizEnum.BSN, null)
            }
            R.id.buttonBizCnfirmFarm -> {
                PreferenceUtil.setString(applicationContext, "bizSubCategory","농업")
                PreferenceUtil.setBiz(applicationContext, "bizSubCategoryKey", BizEnum.FARM)
                nextView(this, Constants.MAP_ACT, BizEnum.FARM, null)
            }
            R.id.buttonBizCnfirmMnidst -> {
                PreferenceUtil.setString(applicationContext, "bizSubCategory","광업권")
                PreferenceUtil.setBiz(applicationContext, "bizSubCategoryKey", BizEnum.MINRGT)
                nextView(this, Constants.MAP_ACT, BizEnum.MINRGT, null)
            }
            R.id.buttonBizCnfirmFshr -> {
                PreferenceUtil.setString(applicationContext, "bizSubCategory","어업권")
                PreferenceUtil.setBiz(applicationContext, "bizSubCategoryKey", BizEnum.FYHTS)
                nextView(this, Constants.MAP_ACT, BizEnum.FYHTS, null)
            }
            R.id.buttonBizCnfirmResidnt -> {
                PreferenceUtil.setString(applicationContext, "bizSubCategory","거주자")
                PreferenceUtil.setBiz(applicationContext, "bizSubCategoryKey", BizEnum.RESIDNT)
                nextView(this, Constants.MAP_ACT, BizEnum.RESIDNT, null)
            }
            R.id.buttonBizCnfirmTomb -> {
                PreferenceUtil.setString(applicationContext, "bizSubCategory","분묘")
                PreferenceUtil.setBiz(applicationContext, "bizSubCategoryKey", BizEnum.TOMB)
                nextView(this, Constants.MAP_ACT, BizEnum.TOMB, null)
            }
            // TODO : (임시) 잔여지 / 잔여 건물
            R.id.buttonBizCnfirmRestLAD -> {
                PreferenceUtil.setString(applicationContext, "bizSubCategory", "잔여지")
                PreferenceUtil.setBiz(applicationContext, "bizSubCategoryKey", BizEnum.REST_LAD)
                nextView(this, Constants.MAP_ACT, BizEnum.REST_LAD, null)
            }
            R.id.buttonBizCnfirmRestThing -> {
                PreferenceUtil.setString(applicationContext, "bizSubCategory", "잔여 물건")
                PreferenceUtil.setBiz(applicationContext, "bizSubCategoryKey", BizEnum.REST_THING)
                nextView(this, Constants.MAP_ACT, BizEnum.REST_THING, null)
            }
            else -> println("getBizCnfirmCode -> 1000")
        }
    }

    companion object {
        private val TAG: String? = BizCnfirmActivity::class.simpleName
    }
}