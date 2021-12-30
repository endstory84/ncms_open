/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_land_search_item2_big.view.*
import kotlinx.android.synthetic.main.include_wtnnc_camera_imageview.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.*
import kr.or.kreb.ncms.mobile.util.Constants
import kr.or.kreb.ncms.mobile.util.LogUtil
import kr.or.kreb.ncms.mobile.util.goneView
import kr.or.kreb.ncms.mobile.util.visibleView

/**
 * 조서 사진촬영 이미지
 */

class WtnncImageAdapter(
    var context: Context,
    val dataList: MutableList<WtnncImage>
) : RecyclerView.Adapter<WtnncImageAdapter.ViewHolder>() {

    val logUtil = LogUtil(WtnncImageAdapter::class.java.simpleName)
    lateinit var bitmap: Bitmap

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.include_wtnnc_camera_imageview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])

        holder.itemView.apply {

            // 삭제
            wtnncCameraDeleteBtn.setOnClickListener {
                wtnccCameraImageView.setImageResource(R.drawable.img_picture_none)
                wtnncCameraDeleteBtn.goneView()

                removeItem(position, Constants.BIZ_SUBCATEGORY_KEY.toString())
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: WtnncImage) {
            //itemView.wtnccCameraTextView.text = item.seq.toString()
            if (item.image != null) {

                itemView.apply {
                    wtnncCameraDeleteBtn.visibleView()
                    wtnccCameraImageView.setImageBitmap(item.image)
                }

                // 썸네일 이미지 클릭
                itemView.wtnccCameraImageView.setOnClickListener { expandImageView(item) }
            }

        }
    }

    fun expandImageView(item: WtnncImage) {
        if (item.image != null) {
            val inflater: LayoutInflater = LayoutInflater.from(context)
            val builder = AlertDialog.Builder(context)
            val alertView = inflater.inflate(R.layout.fragment_land_search_item2_big, null)
            builder.setView(alertView)
            alertView.img_big.setImageBitmap(item.image)

            builder.setCancelable(false)
            builder.setNegativeButton("닫기", null)
            builder.show()
        }
    }

    fun updateItem(data: WtnncImage, index: Int, bizCode:String) {

        val idx = if (index == 0) {
            index
        } else {
            index - 1
        }

        logUtil.d("updateItem getIndex -> $idx")
        dataList[idx] = data
        getImageDataList(bizCode)
        notifyDataSetChanged()
    }

    fun addItem(data: WtnncImage, bizCode:String) {
        dataList.add(data)
        getImageDataList(bizCode)
        notifyDataSetChanged()
    }

    private fun removeItem(position: Int, bizCode:String) {
        if (Constants.CAMERA_IMGAE_INDEX > 0)
            Constants.CAMERA_IMGAE_INDEX--
            dataList[position].image = null
//        dataList.removeAt(position)
//        notifyItemRemoved(position)
//        notifyItemRangeChanged(position, dataList.size)
        getImageDataList(bizCode)
    }

    fun getImageDataList(bizCode: String) {
        when(bizCode) {
            "LAD" -> LandInfoObject.wtnncImage = dataList
            "THING" -> ThingWtnObject.wtnncImage = dataList
            "BSN" -> ThingBsnObject.wtnncImage = dataList
            "FARM" -> ThingFarmObject.wtnncImage = dataList
            "TOMB" -> ThingTombObject.wtnncImage = dataList
            "MINRGT" -> ThingMinrgtObject.wtnncImage = dataList
            "FYHTS" -> ThingFyhtsObject.wtnncImage = dataList
//            "REST_LAD" -> ThingTombObject.wtnncImage = dataList
//            "REST_THING" -> ThingTombObject.wtnncImage = dataList
        }

    }
}

