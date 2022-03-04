 /*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.dev.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.thing_dialog_item2.view.*
import kotlinx.android.synthetic.main.thing_search_gnrl.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.ThingWtnObject
import kr.or.kreb.ncms.mobile.fragment.ThingDialogFragment
import kr.or.kreb.ncms.mobile.listener.ThingViewPagerInterface
import kr.or.kreb.ncms.mobile.util.PermissionUtil.logUtil
import org.json.JSONArray

 class ThingKndDetailListAdapter(
    private val mActivity: Activity,
    val mContext: Context,
    private var thingType: String,
     private var thingKndSub: String,
//    private val treeImgList: ArrayList<Int>,
//    private val treeTitleList: ArrayList<String>,
//    private val treeTextList: ArrayList<String>,
    private var detailData: JSONArray,
    val v: ThingDialogFragment,
    val value: Int,
    val thingListener: ThingViewPagerInterface,
    val jibun: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)


    inner class thingViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!)

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): thingViewHolder {
         return thingViewHolder(parent.inflate(R.layout.thing_dialog_item2))
     }

//    override fun onBindViewHolder(holder: thingViewHolder, position: Int) {
//
//        holder.itemView.imageViewThingDialog.setImageResource(treeImgList[position])
//        holder.itemView.textViewThingDialog01.text = treeTitleList[position]
//        holder.itemView.textViewThingDialog02.text = treeTextList[position]
//
//
////        holder.itemView.thingDialogItemView.setOnClickListener {view ->
////
//////            val thingAddView: LinearLayout? = mActivity.findViewById(R.id.thingLinearAddView)
//////            val selectItem: Any? = mActivity.thingSmallSpinner?.selectedItem
//////
//////
//////            if(value == 2) {
//////                thingOpenViewPager(view)
//////            } else if(value == 1) {
//////                settingThingViewPager(view)
//////            }
////
////        }
//    }

    override fun getItemCount(): Int {
        return detailData.length()
    }

     override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

         holder.itemView.apply {
             val detailItemJson = detailData.getJSONObject(position)

             thingDetailTitle.setText(checkStringNull(detailItemJson.getString("thingTitle")))
             thingDetailContent.text = checkStringNull(detailItemJson.getString("thingCn"))


             val thingListCode = detailItemJson.getString("thingListCode")
             val atflNm1 = detailItemJson.getString("atflNm1")
             val atflNm2 = detailItemJson.getString("atflNm2")
             val atflNm3 = detailItemJson.getString("atflNm3")
             val atflNm4 = detailItemJson.getString("atflNm4")
             val atflNm5 = detailItemJson.getString("atflNm5")


             logUtil.d(thingListCode)
             logUtil.d(atflNm1)
             logUtil.d(atflNm2)
             logUtil.d(atflNm3)
             logUtil.d(atflNm4)
             logUtil.d(atflNm5)
//             val am = getResources().assets

             try {

                 if(thingType.equals("0")) {
                     imageViewThingDialog.setImageBitmap(loadBitmapAssets("tree/" + thingListCode + "/" + atflNm1))
                     imagaSubThingKnd1.setImageBitmap(loadBitmapAssets("tree/" + thingListCode + "/" + atflNm1))
                     imagaSubThingKnd1.setOnClickListener {
                         imageViewThingDialog.setImageBitmap(loadBitmapAssets("tree/" + thingListCode + "/" + atflNm1))
                     }
                     if (checkStringNull(atflNm2) == "") {
                         imagaSubThingKnd2.visibility = View.GONE
                     } else {
                         imagaSubThingKnd2.setImageBitmap(loadBitmapAssets("tree/" + thingListCode + "/" + atflNm2))
                         imagaSubThingKnd2.setOnClickListener {
                             imageViewThingDialog.setImageBitmap(loadBitmapAssets("tree/" + thingListCode + "/" + atflNm2))
                         }

                     }
                     if (checkStringNull(atflNm3) == "") {
                         imagaSubThingKnd3.visibility = View.GONE
                     } else {
                         imagaSubThingKnd3.setImageBitmap(loadBitmapAssets("tree/" + thingListCode + "/" + atflNm3))
                         imagaSubThingKnd3.setOnClickListener {
                             imageViewThingDialog.setImageBitmap(loadBitmapAssets("tree/" + thingListCode + "/" + atflNm3))
                         }

                     }
                     if (checkStringNull(atflNm4) == "") {
                         imagaSubThingKnd4.visibility = View.GONE
                     } else {
                         imagaSubThingKnd4.setImageBitmap(loadBitmapAssets("tree/" + thingListCode + "/" + atflNm4))
                         imagaSubThingKnd4.setOnClickListener {
                             imageViewThingDialog.setImageBitmap(loadBitmapAssets("tree/" + thingListCode + "/" + atflNm4))
                         }

                     }
                     if (checkStringNull(atflNm5) == "") {
                         imagaSubThingKnd5.visibility = View.GONE
                     } else {
                         imagaSubThingKnd5.setImageBitmap(loadBitmapAssets("tree/" + thingListCode + "/" + atflNm5))
                         imagaSubThingKnd5.setOnClickListener {
                             imageViewThingDialog.setImageBitmap(loadBitmapAssets("tree/" + thingListCode + "/" + atflNm5))
                         }
                     }
                 }else if(thingType.equals("1")) {
                     val thingImageCode = thingListCode.substring(0,1)
                     imageViewThingDialog.setImageBitmap(loadBitmapAssets("material/" + thingImageCode + "/" + atflNm1))
                     imagaSubThingKnd1.setImageBitmap(loadBitmapAssets("material/" + thingImageCode + "/" + atflNm1))
                     imagaSubThingKnd1.setOnClickListener {
                         imageViewThingDialog.setImageBitmap(loadBitmapAssets("material/" + thingImageCode + "/" + atflNm1))
                     }
                     if (checkStringNull(atflNm2) == "") {
                         imagaSubThingKnd2.visibility = View.GONE
                     } else {
                         imagaSubThingKnd2.setImageBitmap(loadBitmapAssets("material/" + thingImageCode + "/" + atflNm2))
                         imagaSubThingKnd2.setOnClickListener {
                             imageViewThingDialog.setImageBitmap(loadBitmapAssets("material/" + thingImageCode + "/" + atflNm2))
                         }

                     }
                     if (checkStringNull(atflNm3) == "") {
                         imagaSubThingKnd3.visibility = View.GONE
                     } else {
                         imagaSubThingKnd3.setImageBitmap(loadBitmapAssets("material/" + thingImageCode + "/" + atflNm3))
                         imagaSubThingKnd3.setOnClickListener {
                             imageViewThingDialog.setImageBitmap(loadBitmapAssets("material/" + thingImageCode + "/" + atflNm3))
                         }

                     }
                     if (checkStringNull(atflNm4) == "") {
                         imagaSubThingKnd4.visibility = View.GONE
                     } else {
                         imagaSubThingKnd4.setImageBitmap(loadBitmapAssets("material/" + thingImageCode + "/" + atflNm4))
                         imagaSubThingKnd4.setOnClickListener {
                             imageViewThingDialog.setImageBitmap(loadBitmapAssets("material/" + thingImageCode + "/" + atflNm4))
                         }

                     }
                     if (checkStringNull(atflNm5) == "") {
                         imagaSubThingKnd5.visibility = View.GONE
                     } else {
                         imagaSubThingKnd5.setImageBitmap(loadBitmapAssets("material/" + thingImageCode + "/" + atflNm5))
                         imagaSubThingKnd5.setOnClickListener {
                             imageViewThingDialog.setImageBitmap(loadBitmapAssets("material/" + thingImageCode + "/" + atflNm5))
                         }
                     }
                 }else if(thingType.equals("2")) {
                     val thingImageCode = thingListCode.substring(0,2)
                     imageViewThingDialog.setImageBitmap(loadBitmapAssets("buildt/" + thingImageCode + "/" + atflNm1))
                     imagaSubThingKnd1.setImageBitmap(loadBitmapAssets("buildt/" + thingImageCode + "/" + atflNm1))
                     imagaSubThingKnd1.setOnClickListener {
                         imageViewThingDialog.setImageBitmap(loadBitmapAssets("buildt/" + thingImageCode + "/" + atflNm1))
                     }
                     if (checkStringNull(atflNm2) == "") {
                         imagaSubThingKnd2.visibility = View.GONE
                     } else {
                         imagaSubThingKnd2.setImageBitmap(loadBitmapAssets("buildt/" + thingImageCode + "/" + atflNm2))
                         imagaSubThingKnd2.setOnClickListener {
                             imageViewThingDialog.setImageBitmap(loadBitmapAssets("buildt/" + thingImageCode + "/" + atflNm2))
                         }

                     }
                     if (checkStringNull(atflNm3) == "") {
                         imagaSubThingKnd3.visibility = View.GONE
                     } else {
                         imagaSubThingKnd3.setImageBitmap(loadBitmapAssets("buildt/" + thingImageCode + "/" + atflNm3))
                         imagaSubThingKnd3.setOnClickListener {
                             imageViewThingDialog.setImageBitmap(loadBitmapAssets("buildt/" + thingImageCode + "/" + atflNm3))
                         }

                     }
                     if (checkStringNull(atflNm4) == "") {
                         imagaSubThingKnd4.visibility = View.GONE
                     } else {
                         imagaSubThingKnd4.setImageBitmap(loadBitmapAssets("buildt/" + thingImageCode + "/" + atflNm4))
                         imagaSubThingKnd4.setOnClickListener {
                             imageViewThingDialog.setImageBitmap(loadBitmapAssets("buildt/" + thingImageCode + "/" + atflNm4))
                         }

                     }
                     if (checkStringNull(atflNm5) == "") {
                         imagaSubThingKnd5.visibility = View.GONE
                     } else {
                         imagaSubThingKnd5.setImageBitmap(loadBitmapAssets("buildt/" + thingImageCode + "/" + atflNm5))
                         imagaSubThingKnd5.setOnClickListener {
                             imageViewThingDialog.setImageBitmap(loadBitmapAssets("buildt/" + thingImageCode + "/" + atflNm5))
                         }
                     }
                 }

//                 val imageBitmap1 = BitmapUtils.loadBitmapFromAssets("tree/"+thingListCode+"/"+atflNm1)
//                 val imageBitmap2 = BitmapUtils.loadBitmapFromAssets("tree/"+thingListCode+"/"+atflNm2)


//                 imageViewThingDialog.setImageBitmap(imageBitmap)
             } catch(e: Exception) {
                 logUtil.d(e.toString())
             }

             thingDialogItemView.setOnClickListener { view ->
                val thingAddView: LinearLayout? = mActivity.findViewById(R.id.thingLinearAddView)
                val selectItem: Any? = mActivity.thingSmallSpinner?.selectedItem


                if(value == 2) {
                    thingOpenViewPager(view, thingListCode,detailItemJson)
                } else if(value == 1) {
                    settingThingViewPager(view, thingListCode,detailItemJson)
                }

             }
//             imageViewThingDialog.setImageDrawable(R.drawable.)
//

         }
     }

    fun thingOpenViewPager(view: View, thingListCode: String, data: JSONObject) {
        val setTextValue = view.thingDetailTitle.text
        ThingWtnObject.thingNewSearch = "Y"
        when (thingType) {
            "0" -> { // 수목
                thingListener.loadViewPage("A023005", setTextValue.toString(), jibun!!)
                v.dismiss()
            }
            "1" -> { //공작물
                thingListener.loadViewPage("A023004", setTextValue.toString(), jibun!!)
                v.dismiss()
            }
            "2" -> { // 건물
                ThingWtnObject.strctNdStndrd = setTextValue.toString()
                if(
                thingListCode.equals("BE01")||
                thingListCode.equals("BE02")||
                thingListCode.equals("BF01")||
                thingListCode.equals("BF02")||
                thingListCode.equals("BF03")||
                thingListCode.equals("BF04")||
                thingListCode.equals("BF05")||
                thingListCode.equals("BG01")||
                thingListCode.equals("BG02")||
                thingListCode.equals("BH01")||
                thingListCode.equals("BH02")||
                thingListCode.equals("BI01")||
                thingListCode.equals("BJ01")||
                thingListCode.equals("BJ02")||
                thingListCode.equals("BJ03")||
                thingListCode.equals("BK01")||
                thingListCode.equals("BK02")) {

                    thingListener.loadViewPage("A023003", data.getString("thingSclasTy"), jibun!!)
                    v.dismiss()
                } else {
                    thingListener.loadViewPage("A023002", data.getString("thingSclasTy"), jibun!!)
                    v.dismiss()
                }
            }
            else -> {
                mActivity.thingSmallSpinner?.setSelection(0)
                v.dismiss()
            }
        }
    }


    fun settingThingViewPager(view: View?, thingListCode: String, data:JSONObject) {

        val setTextValue = view!!.thingDetailTitle.text
        ThingWtnObject.thingNewSearch = "Y"

        when (thingType) {
            "0" -> { // 수목
                thingListener.loadViewPage("A023005", setTextValue.toString(), jibun!!)
                v.dismiss()
            }
            "1" -> { //공작물
                thingListener.loadViewPage("A023004", setTextValue.toString(), jibun!!)
                v.dismiss()
            }
            "2" -> { // 건물
                ThingWtnObject.strctNdStndrd = setTextValue.toString()
                if(
                    thingListCode.equals("BE01")||
                    thingListCode.equals("BE02")||
                    thingListCode.equals("BF01")||
                    thingListCode.equals("BF02")||
                    thingListCode.equals("BF03")||
                    thingListCode.equals("BF04")||
                    thingListCode.equals("BF05")||
                    thingListCode.equals("BG01")||
                    thingListCode.equals("BG02")||
                    thingListCode.equals("BH01")||
                    thingListCode.equals("BH02")||
                    thingListCode.equals("BI01")||
                    thingListCode.equals("BJ01")||
                    thingListCode.equals("BJ02")||
                    thingListCode.equals("BJ03")||
                    thingListCode.equals("BK01")||
                    thingListCode.equals("BK02")) {

                    thingListener.loadViewPage("A023003", data.getString("thingSclasTy"), jibun!!)
                    v.dismiss()
                } else {
                    thingListener.loadViewPage("A023002", data.getString("thingSclasTy"), jibun!!)
                    v.dismiss()
                }
            }
            else -> {
                mActivity.thingSmallSpinner?.setSelection(0)
                v.dismiss()
            }
        }
    }

     fun checkStringNull(nullString: String): String {
         if (nullString == "null") {
             return ""
         } else {
             return nullString
         }
     }

     fun loadBitmapAssets(path: String): Bitmap {

         var assetsBitmap: Bitmap? = null

         val assetsOpenFile = mContext.getResources().assets.open(path)
         assetsBitmap = BitmapFactory.decodeStream(assetsOpenFile)

         return assetsBitmap
     }

     fun setJSONArray(data: JSONArray, thingType: String, thingKndSub: String) {
         this.detailData = data
         this.thingType = thingType
         this.thingKndSub = thingKndSub
     }
}