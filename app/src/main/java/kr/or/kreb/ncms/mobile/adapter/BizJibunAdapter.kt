/*
 * Create by sgablc team.eco-chain on 2022.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_bizlist_jibun_item.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.BizJibunListInfo
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.util.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class BizJibunAdapter(val callDataArr: MutableList<BizJibunListInfo>) : RecyclerView.Adapter<BizJibunAdapter.ViewHolder>() {

    private var logUtil: LogUtil = LogUtil("BizJibunAdapter")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BizJibunAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_bizlist_jibun_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: BizJibunAdapter.ViewHolder, position: Int) {
        holder.bind(callDataArr[position])
    }

    override fun getItemCount(): Int = callDataArr.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val context: Context = itemView.context

        @SuppressLint("SetTextI18n")
        fun bind(item: BizJibunListInfo) {
            with(itemView) {
                    item.apply {
                        if(item.searchCode == "LAD") {
                            bizJibunSearchNoTxt.text = "$no-$subNo"
                            bizJibunLegaldongNmTxt.text = legaldongNm
                            bizJibunBgnnLnmTxt.text = bgnnLnm
                            bizJibunIncrprLnmTxt.text = incrprLnm
                            bizJibunBgnnArTxt.text = bgnnAr
                            bizJibunIncrprArTxt.text = incrprAr
                            bizJibunGobuLndcgrNmTxt.text = gobuLndcgrNm
                            bizJibunOwnerNameTxt.text = ownerName
                            bizJibunDcnsAtTxt.text = dcsnAt
                        }
                        if(item.searchCode == "THING") {
                            bizJibunSearchNoTxt.text = thingWtnCode
                            bizJibunLegaldongNmTxt.text = legaldongNm
                            bizJibunBgnnLnmTxt.text = bgnnLnm
                            bizJibunIncrprLnmTxt.text = incrprLnm
                            bizJibunBgnnArTxt.text = bgnnAr
                            bizJibunIncrprArTxt.text = incrprAr
                            bizJibunGobuLndcgrNmTxt.text = gobuLndcgrNm
                            bizJibunOwnerNameTxt.text = ownerName
                            bizJibunDcnsAtTxt.text = dcsnAt
                        }
                }

                bizListJibunLayout.setOnClickListener {
                    //logUtil.d("BizJibunAdapter item Click $item")

                    val sumAddr: String
                    with(item){ sumAddr = "$legaldongNm $bgnnLnm" }

                    HttpUtil.getInstance(context!!)
                        .urlGetNaver("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=$sumAddr", "9mek16psq2", "1yoMMipAFWX37VohQWgXULY3AjPOGAwouy7feZqo",
                            object : Callback {
                                override fun onFailure(call: Call, e: IOException) = logUtil.d("fail")
                                override fun onResponse(call: Call, response: Response) {
                                    response.use {
                                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                                        for ((name, value) in response.headers) {
                                            logUtil.d("$name: $value")
                                        }

                                        val resultData = httpResultToJsonObject(response.body!!.string())
                                        val getX = resultData?.asJsonObject?.get("addresses")?.asJsonArray?.get(0)?.asJsonObject?.get("x")?.asString
                                        val getY = resultData?.asJsonObject?.get("addresses")?.asJsonArray?.get(0)?.asJsonObject?.get("y")?.asString

                                        logUtil.d("$getX, $getY")


                                        if(item.bizType == "rest") {
                                            if(item.searchCode == "LAD") {
                                                val itemJson = JSONObject()
                                                itemJson.put("saupCode", item.saupCode)
                                                itemJson.put("ladWtnCode", item.ladWtnCode)
                                                itemJson.put("legaldongCode", item.legaldongCode)
                                                itemJson.put("legaldongNm", item.legaldongNm)
                                                itemJson.put("bgnnLnm", item.bgnnLnm)
                                                itemJson.put("incrprLnm", item.incrprLnm)
                                                PreferenceUtil.setString(context, "bizSubCategory","잔여지")
                                                PreferenceUtil.setBiz(context, "bizSubCategoryKey", BizEnum.REST_LAD)
                                                nextView(context, Constants.MAP_ACT, BizEnum.REST_LAD, null,
                                                    "$getY, $getX", itemJson)
                                            } else if(item.searchCode == "THING") {
                                                val itemJson = JSONObject()
                                                itemJson.put("saupCode", item.saupCode)
                                                itemJson.put("thingWtnCode", item.thingWtnCode)
                                                itemJson.put("legaldongCode", item.legaldongCode)
                                                itemJson.put("legaldongNm", item.legaldongNm)
                                                itemJson.put("bgnnLnm", item.bgnnLnm)
                                                itemJson.put("incrprLnm", item.incrprLnm)
                                                PreferenceUtil.setString(context, "bizSubCategory","잔여건물")
                                                PreferenceUtil.setBiz(context, "bizSubCategoryKey", BizEnum.REST_THING)
                                                nextView(context, Constants.MAP_ACT, BizEnum.REST_THING, null,
                                                    "$getY, $getX", itemJson)
                                            }

                                        } else {
                                            PreferenceUtil.setString(context, "bizSubCategory","토지")
                                            PreferenceUtil.setBiz(context, "bizSubCategoryKey", BizEnum.LAD)
                                            nextView(context, Constants.MAP_ACT, BizEnum.LAD, null, "$getY, $getX", null)
                                        }



                                    }
                                }

                            }
                        )

                }
            }
        }
    }
}