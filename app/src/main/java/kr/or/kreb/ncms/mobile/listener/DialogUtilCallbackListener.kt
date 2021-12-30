/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.listener

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_bsn_search.*
import kotlinx.android.synthetic.main.fragment_tomb_search.*
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.data.*
import kr.or.kreb.ncms.mobile.util.*
import org.json.JSONObject

/**
 *  [DialogUtil] 콜백 리스너
 *  @param jsonData
 *  @param progressDialog
 *  @param type - 각자 다이얼로그 Type 지정
 *  [onPositiveClickListener] - 예
 *  [onNegativeClickListener] - 아니오
 */

class DialogUtilCallbackListener (
    var context: Context?,
    var activity: Activity?,
    var jsonData: JSONObject,
    var progressDialog: AlertDialog,
    var url: String
) : DialogUtil.ClickListener {

    var dialog: DialogUtil = DialogUtil(context, activity)
    var builder: MaterialAlertDialogBuilder? = null
    var logUtil: LogUtil = LogUtil(DialogUtilCallbackListener::class.java.simpleName)

    private fun getActivity(): MapActivity = (activity as MapActivity)

    override fun onPositiveClickListener(dialog: DialogInterface, type: String) {

        logUtil.d("DialogUtil $type -> 확인")
        builder = context?.let { MaterialAlertDialogBuilder(it) }!!

        when (type) {

            "지장물조서" -> {
                when(ThingWtnObject.thingSmallCl){
                    "" -> {
                        logUtil.d("지장물 -> 소분류 미선택")
                        this.dialog.wtnccAlertDialog("소분류를 선택해주세요.", builder!!, "지장물 소분류").show()
                        return
                    }
                    else -> {
                        when(ThingWtnObject.thingSmallCl) {
                            "A023001", "A023004", "A023006", "A023007", "A023008", "A023009" -> {
                                logUtil.d("일반 지장물 -> 소분류 선택")
                                getActivity().setWtnccHttpConnectionData(jsonData)
                            }
                            "A023002", "A023003" -> {
                                logUtil.d(" 건축물 -> 소분류 선택")
                                getActivity().setWtnccHttpConnectionData(jsonData)
                            }
                            "A023005" -> {
                                logUtil.d(" 수목 -> 소분류 선택")
                                when(ThingWtnObject.examinMthd){
                                    "" -> {
                                        this.dialog.wtnccAlertDialog("조사방식을 선택해주세요.", builder!!, "지장물(수목) 조사방식").show()
                                        return
                                    }
                                    else -> getActivity().setWtnccHttpConnectionData(jsonData)
                                }
                            }
                        }
                    }
                }
            }

            "분묘조서" -> {
                when (ThingTombObject.tombTy) {
                    "0", "" -> {
                        logUtil.d("분묘구분 -> 미선택")
                        this.dialog.wtnccAlertDialog("분묘구분을 선택해주세요.", builder!!, "분묘구분").show()
                        return
                    }

                    "1", "2" -> {
                        when (ThingTombObject.balmClText) {
                            "" -> {
                                this.dialog.wtnccAlertDialog("분묘연고자를 선택해주세요.", builder!!, "분묘연고자").show()
                                return
                            }
                            else -> {
//                                when (activity?.tombBytgtCn?.text.toString()) {
//                                    "" -> {
//                                        this.dialog.wtnccAlertDialog("매장규모를 선택해주세요.", builder!!, "분묘매장규모").show()
//                                        return
//                                    }
//                                    else -> getActivity().setWtnccHttpConnectionData()
//                                }
                                getActivity().setWtnccHttpConnectionData(jsonData)
                            }
                        }

                    }
                }
            }

            "광업조서" -> {

                var minrgtRequireArr = mutableListOf(
                    ThingMinrgtObject.minrgtRegNo,
                    ThingMinrgtObject.minrgtRegDe,
                    ThingMinrgtObject.minrgtLgstr,
                    ThingMinrgtObject.mnrlKnd,
                )

                minrgtRequireArr.forEach { obj ->

                    when (obj) {
                        ThingMinrgtObject.minrgtRegNo -> {
                            if (obj == "제호") {
                                this.dialog.wtnccAlertDialog("등록번호를 입력해주세요.", builder!!, "광업등록번호").show()
                                return
                            }
                        }
                        ThingMinrgtObject.minrgtRegDe -> {
                            if (obj == "") {
                                this.dialog.wtnccAlertDialog("등록일자를 입력해주세요.", builder!!, "광업등록일자").show()
                                return
                            }
                        }
                        ThingMinrgtObject.minrgtLgstr -> {
                            if (obj == "당진호") {
                                this.dialog.wtnccAlertDialog("광업지적을 입력해주세요.", builder!!, "광업지적").show()
                                return
                            }
                        }
                        ThingMinrgtObject.mnrlKnd -> {
                            if (obj == "") {
                                this.dialog.wtnccAlertDialog("광종을 입력해주세요.", builder!!, "광종").show()
                                return
                            }
                        }
                    }

                }

                getActivity().setWtnccHttpConnectionData(jsonData)
            }

            "어업조서" -> {

                var fyhtsRequireArr = mutableListOf(
                    ThingFyhtsObject.administGrc,
                    ThingFyhtsObject.lcnsKnd,
                    ThingFyhtsObject.lcnsNo,
                    ThingFyhtsObject.fshrMth
                )

                fyhtsRequireArr.forEach { obj ->

                    when (obj) {
                        ThingFyhtsObject.administGrc -> {
                            if (obj == "") {
                                this.dialog.wtnccAlertDialog("행정관청을 입력해주세요.", builder!!, "어업행정관청").show()
                                return
                            }
                        }
                        ThingFyhtsObject.lcnsKnd -> {
                            if (obj == "") {
                                this.dialog.wtnccAlertDialog("면허종류를 입력해주세요.", builder!!, "어업면허종류").show()
                                return
                            }
                        }
                        ThingFyhtsObject.lcnsNo -> {
                            if (obj == "") {
                                this.dialog.wtnccAlertDialog("면허번호를 입력해주세요.", builder!!, "어업면허번호").show()
                                return
                            }
                        }
                        ThingFyhtsObject.fshrMth -> {
                            if (obj == "") {
                                this.dialog.wtnccAlertDialog("어업의방법을 입력해주세요.", builder!!, "어업의방법").show()
                                return
                            }
                        }
                    }

                }

                getActivity().setWtnccHttpConnectionData(jsonData)

            }

            "영업조서" -> {
                when (jsonData.getJSONObject("thing").getString("thingSmallCl")) {
                    "A016001", "A016040" -> {
                        logUtil.d("일반영업 or 잠업")

                        when (activity?.bsnCommPossesnSeSpinner?.selectedItemPosition.toString()) {
                            "" -> {
                                this.dialog.wtnccAlertDialog("점유 구분을 선택해주세요.", builder!!, "(영업/잠업/)점유구분").show()
                                return
                            }
                            else -> {
                                when (ThingBsnObject.bsnPrmisnCl) {
                                    "" -> {
                                        this.dialog.wtnccAlertDialog("허가등 구분을 선택해주세요.", builder!!, "(영업/잠업/) 허가등구분").show()
                                        return
                                    }
                                    else -> {
                                        when (ThingBsnObject.bsnPrmisnCl) {
                                            "A028001" -> {
                                                when (activity?.bsnSgnbrdNm?.text.toString()) {
                                                    "" -> {
                                                        this.dialog.wtnccAlertDialog("간판명을 입력해주세요.", builder!!, "(영업/잠업/) 간판명").show()
                                                        return
                                                    }
                                                    else -> {
                                                        when (activity?.bsnRgsde?.text.toString()) {
                                                            "" -> {
                                                                this.dialog.wtnccAlertDialog("사업자등록일을 선택해주세요.", builder!!, "(영업/잠업/) 사업자등록일").show()
                                                                return
                                                            }
                                                            else -> {
                                                                when (activity?.bsnCmpnm?.text.toString()) {
                                                                    "" -> {
                                                                        this.dialog.wtnccAlertDialog("상호명을 입력해주세요.", builder!!, "(영업/잠업/) 상호명").show()
                                                                        return
                                                                    }
                                                                    else -> {
                                                                        getActivity().setWtnccHttpConnectionData(jsonData)
                                                                        WtnncUtil(activity!!, context!!).thingViewPagerClose(); /* 조서 닫기*/ ToastUtil(context).msg_success("입력이 완료되었습니다.", 100)
                                                                    }

                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                            }
                                            else -> {
                                                when (ThingBsnObject.bsnPrmisnNo) {
                                                    "" -> {
                                                        this.dialog.wtnccAlertDialog("자유업 이외는 허가등번호를 입력하셔야합니다.", builder!!, "(영업/잠업/) 허가등번호").show()
                                                        return
                                                    }
                                                    else -> {
                                                        getActivity().setWtnccHttpConnectionData(jsonData)
                                                        WtnncUtil(activity!!, context!!).thingViewPagerClose() // 조서 닫기
                                                        ToastUtil(context).msg_success("입력이 완료되었습니다.", 100)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }

                    /** 축산 **/
                    "A016050" -> {
                        logUtil.d("축산업")

                        when (activity?.bsnCommPossesnSeSpinner?.selectedItemPosition.toString()) {
                            "" -> {
                                this.dialog.wtnccAlertDialog("점유 구분을 선택해주세요.", builder!!, "(영업/잠업/)점유구분").show()
                                return
                            }
                            else -> {
                                when (activity?.bsnPrmisnSeSpinner?.selectedItemPosition.toString()) {
                                    "" -> {
                                        this.dialog.wtnccAlertDialog("허가등 구분을 선택해주세요.", builder!!, "(영업/잠업/) 허가등구분").show()
                                        return
                                    }
                                    else -> {
                                        when (activity?.bsnPrmisnSeSpinner?.selectedItemPosition.toString()) {
                                            "A028001" -> {
                                                when (activity?.bsnSgnbrdNm?.text.toString()) {
                                                    "" -> {
                                                        this.dialog.wtnccAlertDialog("간판명을 입력해주세요.", builder!!, "(영업/잠업/) 간판명").show()
                                                        return
                                                    }
                                                    else -> {
                                                        when (activity?.bsnRgsde?.text.toString()) {
                                                            "" -> {
                                                                this.dialog.wtnccAlertDialog("사업자등록일을 선택해주세요.", builder!!, "(영업/잠업/) 사업자등록일").show()
                                                                return
                                                            }
                                                            else -> {
                                                                when (activity?.bsnCmpnm?.text.toString()) {
                                                                    "" -> {
                                                                        this.dialog.wtnccAlertDialog("상호명을 입력해주세요.", builder!!, "(영업/잠업/) 상호명").show()
                                                                        return
                                                                    }
                                                                    else -> {

                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                            }
                                            else -> {
                                                when (ThingBsnObject.bsnPrmisnNo) {
                                                    "" -> {
                                                        this.dialog.wtnccAlertDialog("자유업 이외는 허가등번호를 입력하셔야합니다.", builder!!, "(영업/잠업/) 허가등번호").show()
                                                        return
                                                    }
                                                    else -> {
                                                        getActivity().setWtnccHttpConnectionData(jsonData)
                                                        WtnncUtil(activity!!, context!!).thingViewPagerClose() // 조서 닫기
                                                        ToastUtil(context).msg_success("입력이 완료되었습니다.", 100)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                    else -> {
                        this.dialog.wtnccAlertDialog("영업 소분류를 선택해주세요.", builder!!, "영업소분류").show()
                        return
                    }
                }
            }

            "농업조서" -> {
                when (ThingFarmObject.posesnClvthmTy) {
                    "" -> {
                        this.dialog.wtnccAlertDialog("점유구분을 선택해주세요.", builder!!, "농업점유구분").show()
                        return
                    }
                    else -> {
                        getActivity().setWtnccHttpConnectionData(jsonData)
                        WtnncUtil(activity!!, context!!).thingViewPagerClose() // 조서 닫기
                        ToastUtil(context).msg_success("입력이 완료되었습니다.", 100)
                    }
                }
            }

            "거주자조서" -> {
//                when(ThingResidntObject.thingSmallCl){
//                    "A002003" -> {
//                        logUtil.d("이주정착금")
//                    }
//                    "A002002" -> {
//                        logUtil.d("주거이전비")
//                        
//                    }
//                    "A002001" -> {
//                        logUtil.d("이사비")
//                    }
//                    "A002005" -> {
//                        logUtil.d("재편입가산특례")
//                    }
//                    else -> {
//                        this.dialog.wtnccAlertDialog(
//                            "소분류를 선택해주세요.", builder!!, "거주자소분류"
//                        ).show()
//                        return
//                    }
//                }
                when (ThingResidntObject.thingSmallCl) {
                    "" -> {
                        this.dialog.wtnccAlertDialog("소분류를 선택해주세요.", builder!!, "거주자소분류").show()
                        return
                    }
                    else -> {
                        getActivity().setWtnccHttpConnectionData(jsonData)
                    }
                }
            }

        }
    }

    override fun onNegativeClickListener(dialog: DialogInterface, type: String) {
        logUtil.d("DialogUtil $type -> 취소")
        dialog.dismiss()
    }
}