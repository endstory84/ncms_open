 /*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.thing_dialog_item2.view.*
import kotlinx.android.synthetic.main.thing_search_gnrl.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.fragment.ThingDialogFragment
import kr.or.kreb.ncms.mobile.listener.ThingViewPagerInterface

 class ThingDialogRvAdapter2(
    private val mActivity: Activity,
    val mContext: Context,
    private val thingType: Int,
    private val treeImgList: ArrayList<Int>,
    private val treeTitleList: ArrayList<String>,
    private val treeTextList: ArrayList<String>,
    val v: ThingDialogFragment,
    val value: Int,
    val thingListener: ThingViewPagerInterface,
    val jibun: String?
) : RecyclerView.Adapter<ThingDialogRvAdapter2.thingViewHolder>() {

    private fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)


    inner class thingViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!)

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): thingViewHolder {
         return thingViewHolder(parent.inflate(R.layout.thing_dialog_item2))
     }

    override fun onBindViewHolder(holder: thingViewHolder, position: Int) {

        holder.itemView.imageViewThingDialog.setImageResource(treeImgList[position])
        holder.itemView.textViewThingDialog01.text = treeTitleList[position]
        holder.itemView.textViewThingDialog02.text = treeTextList[position]


        holder.itemView.thingDialogItemView.setOnClickListener {view ->

            val thingAddView: LinearLayout? = mActivity.findViewById(R.id.thingLinearAddView)
            val selectItem: Any? = mActivity.thingSmallSpinner?.selectedItem


            if(value == 2) {
                thingOpenViewPager(view)
            } else if(value == 1) {
                settingThingViewPager(view)
            }

        }
    }

    override fun getItemCount(): Int {
        return treeImgList.size
    }

    fun thingOpenViewPager(view: View) {
        val setTextValue = view.textViewThingDialog01.text
        when (thingType) {
            0 -> { // 동산이전
                thingListener.loadViewPage("A023001", setTextValue.toString(), jibun!!)
                v.dismiss()
            }
            1 -> {
                mActivity.thingSmallSpinner?.setSelection(2)
                v.dismiss()
            }
            2 -> { // 집합건축물
                mActivity.thingSmallSpinner?.setSelection(3)
                v.dismiss()
            }
            3 -> { // 공작물
                mActivity.thingSmallSpinner?.setSelection(4)
                v.dismiss()
            }
            4 -> { // 수목
                mActivity.thingSmallSpinner?.setSelection(5)
                v.dismiss()
            }
            5 -> { // 개간비
                mActivity.thingSmallSpinner?.setSelection(6)
                v.dismiss()
            }
            6 -> { // 잔여지가격손실
                mActivity.thingSmallSpinner?.setSelection(7)
                v.dismiss()
            }
            7 -> { // 소유권이외의권리
                mActivity.thingSmallSpinner?.setSelection(8)
                v.dismiss()
            }
            8 -> { // 기타
                mActivity.thingSmallSpinner?.setSelection(9)
                v.dismiss()
            }

            else -> {
                mActivity.thingSmallSpinner?.setSelection(0)
                v.dismiss()
            }
        }
    }


    fun settingThingViewPager(view: View?) {
        val setTextValue = view!!.textViewThingDialog01.text

        mActivity.thingKndEdit.setText(setTextValue)

        when (thingType) {
            0 -> { // 동산이전
                mActivity.thingSmallSpinner?.setSelection(1)

                v.dismiss()
            }
            1 -> { // 일반건축물

//                    if (selectItem !="건물") {
//                        thingAddView?.removeAllViews()
//                    }

                mActivity.thingSmallSpinner?.setSelection(2)
                v.dismiss()
            }
            2 -> { // 집합건축물
                mActivity.thingSmallSpinner?.setSelection(3)
                v.dismiss()
            }
            3 -> { // 공작물
                mActivity.thingSmallSpinner?.setSelection(4)
                v.dismiss()
            }
            4 -> { // 수목
//                    thingAddView?.visibleView()
//
//                    if (selectItem !="수목") {
//                        thingAddView?.removeAllViews()
//                    }
//
//                    /*물건 추가부분*/
//                    val addLayoutView = R.layout.thing_search_wdpt_item
//                    WtnncUtill(mActivity, mContext).wtnncLayoutAdd(addLayoutView, thingAddView)

                mActivity.thingSmallSpinner?.setSelection(5)
                v.dismiss()

//                    //수목 조사방식 spinner
//                    ThingSearchFragment(mActivity, mContext, null).thingSpinnerAdapter(
//                        R.array.thingExaminMthArray,
//                        mActivity.thingItemExaminMthSpnr
//                    )
            }
            5 -> { // 개간비
                mActivity.thingSmallSpinner?.setSelection(6)
                v.dismiss()
            }
            6 -> { // 잔여지가격손실
                mActivity.thingSmallSpinner?.setSelection(7)
                v.dismiss()
            }
            7 -> { // 소유권이외의권리
                mActivity.thingSmallSpinner?.setSelection(8)
                v.dismiss()
            }
            8 -> { // 기타
                mActivity.thingSmallSpinner?.setSelection(9)
                v.dismiss()
            }

            else -> {

//                    thingAddView?.visibleView()
//
//                    if (selectItem !="일반") {
//                        thingAddView?.removeAllViews()
//                    }
//
//                    /*물건 추가부분*/
//                    val addLayoutView = R.layout.thing_search_gnrl_item
//                    WtnncUtill(mActivity, mContext).wtnncLayoutAdd(addLayoutView, thingAddView)

                mActivity.thingSmallSpinner?.setSelection(0)
                v.dismiss()
            }
        }
    }
}