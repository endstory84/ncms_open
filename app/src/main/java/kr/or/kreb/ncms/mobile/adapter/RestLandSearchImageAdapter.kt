/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_land_search_item2.view.*
import kotlinx.android.synthetic.main.fragment_land_search_item2_big.view.*
import kr.or.kreb.ncms.mobile.R


class RestLandSearchImageAdapter(val activity: Activity?, private val arrayList: ArrayList<Int>, var context: Context) :
    RecyclerView.Adapter<RestLandSearchImageAdapter.RestLandViewHolder>() {

    private var ck = 0
    private fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)

    inner class RestLandViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        var smallImg: ImageView = itemView!!.img_small
        var checkBox: CheckBox = itemView!!.img_check
        var deleteButton : Button = activity!!.findViewById(R.id.includeImageDeleteBtn)

        fun bind(num: Int, arrayList: Int) {

            if (ck == 1) {
                checkBox.visibility = View.VISIBLE
                deleteButton.visibility = View.VISIBLE

                smallImg.setOnClickListener{
                    checkBox.isChecked
                }

            } else {
                checkBox.visibility = View.GONE
                //조사 이미지뷰 dialog
                smallImg.setOnClickListener {
                    val inflater: LayoutInflater = LayoutInflater.from(context)
                    val builder = AlertDialog.Builder(context)
                    //builder.setTitle("Custom Dialog")
                    //builder.setIcon(R.drawable.test_img_0)

                    val custom_view = inflater.inflate(R.layout.fragment_restland_search_img_item_big, null)
                    builder.setView(custom_view)

                    custom_view.img_big.setImageResource(arrayList)

//            builder.setPositiveButton("닫기"){ dialogInterface, i ->
//               custom_view.run {
//                   img_big.setImageResource(R.drawable.test_img_0)
//               }

                    builder.setCancelable(false)
                    builder.setNegativeButton("닫기", null)
                    builder.show()
                }

            }
//            if(num >= checkBoxList.size)
//                checkBoxList.add(num, CheckBoxData(null, false))

            smallImg.setImageResource(arrayList)

//            checkBox.setOnClickListener {
//                checkBoxList[num].checked = checkBox.isChecked
//            }

//            //조사 이미지뷰 dialog
//            smallImg.setOnClickListener {
//                val inflater: LayoutInflater = LayoutInflater.from(context)
//                val builder = AlertDialog.Builder(context)
//                //builder.setTitle("Custom Dialog")
//                //builder.setIcon(R.drawable.test_img_0)
//
//                val custom_view = inflater.inflate(R.layout.b_view_item2_big, null)
//                builder.setView(custom_view)
//
//                custom_view.img_big.setImageResource(arrayList)
//
////            builder.setPositiveButton("닫기"){ dialogInterface, i ->
////               custom_view.run {
////                   img_big.setImageResource(R.drawable.test_img_0)
////               }
//
//                builder.setCancelable(false)
//                builder.setNegativeButton("닫기", null)
//                builder.show()
//            }

            smallImg.setOnLongClickListener {
                updateCheckBox(1)
                notifyDataSetChanged()
                checkBox.isChecked = true
                false
            }
        }
    }

    //// test
//    var tempArr = mutableListOf<List<Temp>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestLandViewHolder {
        val view = RestLandViewHolder(parent.inflate(R.layout.fragment_restland_search_img_item))
        return view
    }

    override fun onBindViewHolder(holder: RestLandViewHolder, position: Int) {

        //조사 이미지뷰
        holder.bind(position, arrayList[position])

    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    fun updateCheckBox(n: Int) {
        ck = n
    }

//    fun addItem(arr:Temp){
//        arr.id ="1"
//        arr.isChecked = true
//        tempArr.add(listOf(arr))
//    }

}