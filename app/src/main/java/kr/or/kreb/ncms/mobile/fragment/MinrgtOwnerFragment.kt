/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_add_choice_owner_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_new_modify_owner_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_owner_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_owner_relate.*
import kotlinx.android.synthetic.main.fragment_add_owner_relate.view.*
import kotlinx.android.synthetic.main.fragment_add_select_owner_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_select_owner_dialog.view.addOwnerRgistAddrText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.cancelBtn
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerCrpNoText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerDelvyAddrText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerDivisionText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerNameText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerSameNameText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.selectInputBtn
import kotlinx.coroutines.*
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.*
import kr.or.kreb.ncms.mobile.base.BaseFragment
import kr.or.kreb.ncms.mobile.data.ThingMinrgtObject
import kr.or.kreb.ncms.mobile.data.ThingWtnObject
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.util.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MinrgtOwnerFragment (val fragmentActivity: FragmentActivity) : BaseFragment(),
    BaseOwnerRecyclerViewAdapter.OnOwnerEventListener,
    NewOwnerRecyclerViewAdapter.OnNewOwnerEventListener,
    DialogUtil.ClickListener
{
    var thingDataJson: JSONObject? = null

    var thingOwnerInfoJson: JSONArray? = null

    var dcsnAt: String? = "N"

    private lateinit var adapter: AddOwnerInputAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

//        return inflater.inflate(R.layout.fragment_land_owner, null)
        val view = inflater.inflate(R.layout.fragment_add_owner_relate, null)
        dialogUtil = DialogUtil(context, activity)
        dialogUtil!!.setClickListener(this)
        builder = context?.let { MaterialAlertDialogBuilder(it) }!!
        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))

        init(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    fun init(view: View) {
        var dataString = requireActivity().intent!!.extras!!.get("MinrgtInfo") as String

        thingDataJson = JSONObject(dataString!!)

//        val thingOwnerInfoJson = thingDataJson!!.getJSONArray("ownerInfo") as JSONArray
        thingOwnerInfoJson = thingDataJson!!.getJSONArray("ownerInfo") as JSONArray

        dcsnAt = thingDataJson!!.getJSONObject("ThingSearch").getString("dcsnAt")

        if(ThingMinrgtObject.thingNewSearch.equals("Y")) {
            view.ownerRecyclerView.visibility = View.GONE
            view.newOwnerRecyclerView.visibility = View.VISIBLE

            if(ThingWtnObject.thingNewOwnerInfoJson != null && ThingWtnObject.thingNewOwnerInfoJson!!.length() > 1) {
                GlobalScope.launch {
                    delay(500)
                    withContext(Dispatchers.Main) {
                        newOwnerAdapterCall(ThingWtnObject.thingNewOwnerInfoJson!!)
                    }
                }
            } else {
                dialogUtil?.run {
                    alertDialog(
                        "????????? ??????",
                        "?????? ?????? ??? ????????? ???????????? ?????????????????????????",
                        builder!!,
                        "???????????????"
                    ).show()
                }
            }

        } else {
            view.ownerRecyclerView.visibleView()
            view.newOwnerRecyclerView.goneView()
            recyclerViewAdapter = OwnerRecyclerViewAdapter(
                context!!,
                BizEnum.MINRGT,
                thingOwnerInfoJson!!,
                dcsnAt!!,
                this
            )
            view.ownerRecyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
            view.ownerRecyclerView.adapter = recyclerViewAdapter
        }


    }

    override fun onAddRelateBtnClicked(data: JSONObject) {

        if(dcsnAt == "Y") {
            activity?.runOnUiThread {
                toast.msg_error(R.string.msg_search_dcsc_at_resut, 100)
            }
        } else {

            val ownerData = data

            val ownerSearch = HashMap<String, String>()
            ownerSearch.put("searchSaupCode", PreferenceUtil.getString(context!!, "saupCode", "defaual"))
            ownerSearch.put("searchName", "")
            ownerSearch.put("searchSameNameNo", "")
            ownerSearch.put("searchInhbtntCprNo", "")

            val ownerUrl = context!!.resources.getString(R.string.mobile_url) + "ownerInfo"

            HttpUtil.getInstance(context!!)
                .callerUrlInfoPostWebServer(ownerSearch, progressDialog, ownerUrl,
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            progressDialog!!.dismiss()
                            logUtil.e("fail")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseString = response.body!!.string()

                            logUtil.d("ownerInfo response ------------------> $responseString")

                            val ownerInfoJson = JSONObject(responseString).getJSONObject("list").getJSONArray("owner")

                            layoutInflater.inflate(R.layout.fragment_add_owner_dialog, null).let { view ->
                                view.addOwnerTitleText.text = context!!.resources.getString(R.string.wtnncCommAddRelate)

                                adapter = AddOwnerInputAdapter(context!!)

                                activity?.runOnUiThread {
                                    val searchManager =
                                        context!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager

                                    view.searchViewOwner.run {
                                        setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
                                        setIconifiedByDefault(false)
                                        isSubmitButtonEnabled = true
                                        queryHint = "????????? ?????? ??? ???????????? ??????????????????."
                                    }
                                    view.searchViewOwner.setOnQueryTextListener(object :
                                        androidx.appcompat.widget.SearchView.OnQueryTextListener {
                                        override fun onQueryTextSubmit(query: String?): Boolean {
                                            return false
                                        }

                                        override fun onQueryTextChange(query: String?): Boolean {
                                            adapter.filter.filter(query)
                                            return true
                                        }
                                    })

                                    adapter.filter.filter("")

                                    val layoutManager = LinearLayoutManager(context)
                                    layoutManager.orientation = LinearLayoutManager.VERTICAL
                                    view.addOwnerListView.layoutManager = layoutManager
                                }

                                for (i in 0 until ownerInfoJson.length()) {
                                    adapter.addItem(ownerInfoJson.getJSONObject(i))
                                }

                                view.addOwnerListView.adapter = adapter

                                val ownerInfoDialog = AddOwnerDialogFragment(context!!, activity!!, view).apply {
                                    isCancelable = false
                                    show(fragmentActivity.supportFragmentManager, "ownerInfoDialog")
                                }
                                view.cancelBtn.setOnClickListener {
                                    ownerInfoDialog.dismiss()
                                }

                                view.selectInputBtn.setOnClickListener {
                                    val selectOwnerData = adapter.getSelectItem()

                                    ownerInfoDialog.dismiss()

                                    layoutInflater.inflate(R.layout.fragment_add_select_relate_dialog, null)
                                        .let { view ->
                                            val ownerRelateSelectDialog = AddOwnerSelectDialogFragment(view).apply {
                                                isCancelable = false
                                                show(fragmentActivity.supportFragmentManager, "ownerRelateSelectDialog")
                                            }
                                            view.ownerDivisionText.text = selectOwnerData.indvdlGrpSeNm
                                            view.ownerNameText.text = selectOwnerData.indvdlGrpNm

                                            val sameNameNoString = checkStringNull(selectOwnerData.sameNameNo)
                                            if (sameNameNoString == "") {
                                                view.ownerSameNameText.text = "-"
                                            } else {
                                                view.ownerSameNameText.text = sameNameNoString
                                            }

                                            val crpNoString = checkStringNull(selectOwnerData.inhbtntCprNo)
                                            if (crpNoString.equals("")) {
                                                view.ownerCrpNoText.text = crpNoString
                                            } else {
                                                //val crpNoStringSub = crpNoString.substring(0,8)
                                                val crpNoStringSub = withIhidNumAsterRisk(crpNoString)
                                                view.ownerCrpNoText.text = crpNoStringSub
                                            }

                                            view.ownerDelvyAddrText.text =
                                                "${checkStringNull(selectOwnerData.delvyZip)} ${
                                                    checkStringNull(selectOwnerData.delvyAdres)
                                                } ${checkStringNull(selectOwnerData.delvyAdresDetail)}"

                                            view.cancelBtn.setOnClickListener {
                                                ownerRelateSelectDialog.dismiss()
                                            }
                                            view.selectInputBtn.setOnClickListener {
                                                logUtil.d("selectItemdata size -------------------> ${selectOwnerData.toString()}")

                                                val pcnRightRelateString = view.ownerPcnRightRelate.text.toString()
                                                val ownerRelateBankSpotNm = view.ownerRelateBankSpotNm.text.toString()


                                                if (pcnRightRelateString == "") {
                                                    dialogUtil!!.wtnccAlertDialog(
                                                        """????????? ??????????????? ???????????? ???????????????.""".trimMargin(),
                                                        builder!!,
                                                        "???????????????"
                                                    ).show()
                                                } else {
                                                    val addRelateUrl =
                                                        context!!.resources.getString(R.string.mobile_url) + "addThingRelate"

                                                    val relateAddJson = JSONObject()
                                                    val relateAddData = JSONObject()

                                                    relateAddData.put("delvyAdres", selectOwnerData.delvyAdres)
                                                    relateAddData.put(
                                                        "delvyAdresDetail",
                                                        selectOwnerData.delvyAdresDetail
                                                    )
                                                    relateAddData.put("delvyZip", selectOwnerData.delvyZip)
                                                    relateAddData.put("indvdlGrpCode", selectOwnerData.indvdlGrpCode)
                                                    relateAddData.put("indvdlGrpNm", selectOwnerData.indvdlGrpNm)
                                                    relateAddData.put("indvdlGrpSe", selectOwnerData.indvdlGrpSe)
                                                    relateAddData.put("indvdlGrpSeNm", selectOwnerData.indvdlGrpSeNm)
                                                    relateAddData.put("inhbtntCprNo", selectOwnerData.inhbtntCprNo)
                                                    relateAddData.put("sameNameNo", selectOwnerData.sameNameNo)
                                                    relateAddData.put("pcnRightRelate", pcnRightRelateString)
                                                    relateAddData.put("spotNm", ownerRelateBankSpotNm)

                                                    relateAddData.put(
                                                        "thingCl",
                                                        thingDataJson!!.getJSONObject("ThingSearch")
                                                            .getString("thingSmallCl")
                                                    )

                                                    relateAddData.put(
                                                        "register",
                                                        PreferenceUtil.getString(context!!, "id", "defaual")
                                                    )

                                                    relateAddJson.put("addRelate", relateAddData)
                                                    relateAddJson.put("ownerInfo", ownerData)
                                                    relateAddJson.put("ThingSearch", thingDataJson)

                                                    HttpUtil.getInstance(context!!)
                                                        .callUrlJsonWebServer(relateAddJson,
                                                            progressDialog,
                                                            addRelateUrl,
                                                            object : Callback {
                                                                override fun onFailure(call: Call, e: IOException) {
                                                                    progressDialog!!.dismiss()
                                                                    logUtil.d("fail")
                                                                }

                                                                override fun onResponse(
                                                                    call: Call,
                                                                    response: Response
                                                                ) {
                                                                    val responseString = response.body!!.string()

                                                                    logUtil.d("addRelate response -------------> $responseString")

                                                                    progressDialog!!.dismiss()

                                                                    activity!!.runOnUiThread {
                                                                        val dataJsonObject =
                                                                            JSONObject(responseString).getJSONObject("list")

                                                                        recyclerViewAdapter.setJSONArray(
                                                                            dataJsonObject.getJSONArray(
                                                                                "ownerInfo"
                                                                            )
                                                                        )
                                                                        recyclerViewAdapter.notifyDataSetChanged()
                                                                    }

                                                                    ownerRelateSelectDialog.dismiss()
                                                                }

                                                            })
                                                }
                                            }
                                        }
                                }
                            }
                        }

                    })
        }
    }


    override fun onAddNewOwnerBtnClicked() {

        logUtil.d("onAddNewOwnerBtnClicked ->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")

        if(dcsnAt == "Y") {
            activity?.runOnUiThread {
                toast.msg_error(R.string.msg_search_dcsc_at_resut, 100)
            }
        } else {

            dialogUtil?.run {
                alertDialog(
                    "????????? ??????",
                    "?????? ?????? ??? ????????? ???????????? ?????????????????????????",
                    builder!!,
                    "???????????????"
                ).show()
            }

        }

    }

    override fun onPositiveClickListener(dialog: DialogInterface, type: String) {

        when (type) {
            "?????????" -> {

            }
            "???????????????" -> {
                val choiceOwnerInfoUrl = context!!.resources.getString(R.string.mobile_url) + "choiceThingOwnerInfo"
                val choiceData = HashMap<String, String>()

                val ladData = thingDataJson!!.getJSONObject("ThingSearch")
                choiceData.put("saupCode", ladData.getString("saupCode"))
                choiceData.put("incrprLnm", ladData.getString("incrprLnm"))

                HttpUtil.getInstance(context!!)
                    .callerUrlInfoPostWebServer(choiceData, progressDialog, choiceOwnerInfoUrl,
                        object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                logUtil.d("fail")
                                progressDialog?.dismiss()
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responseString = response.body!!.string()

                                logUtil.d("choiceOwnerInfo Response -------------------> $responseString")

                                progressDialog!!.dismiss()

                                val ownerData = JSONObject(responseString).getJSONObject("list") as JSONObject

                                val ownerInfo = ownerData.getJSONArray("ownerInfo")
                                layoutInflater.inflate(R.layout.fragment_add_choice_owner_dialog, null).let { view ->
                                    activity?.runOnUiThread {
                                        val ownerChoiceSelectDialog = AddOwnerSelectDialogFragment(view).apply {
                                            isCancelable = false
                                            show(fragmentActivity.supportFragmentManager, "ownerChoiceSelectDialog")

                                        }

                                        view.addChoiceOwnerListView.adapter = AddChoiceOwnerAdapter(context!!)

                                        for (i in 0 until ownerInfo.length()) {
                                            (view.addChoiceOwnerListView.adapter as AddChoiceOwnerAdapter).addItem(
                                                ownerInfo.getJSONObject(
                                                    i
                                                )
                                            )
                                        }
                                        view.cancelBtn.setOnClickListener {
                                            ownerChoiceSelectDialog.dismiss()
                                            newOwnerAdapterCall(thingOwnerInfoJson!!)
                                        }
                                        view.selectInputBtn.setOnClickListener {

                                            val selectOwnerChoiceData =
                                                (view.addChoiceOwnerListView.adapter as AddChoiceOwnerAdapter).getSelectItem()


                                            ownerChoiceSelectDialog.dismiss()

                                            if(ThingMinrgtObject.thingNewSearch == "Y") {
                                                if(ThingWtnObject.thingNewOwnerInfoJson != null) {

                                                    val ownerJson = ThingWtnObject.thingNewOwnerInfoJson as JSONArray

                                                    for (i in 0 until selectOwnerChoiceData.size) {
                                                        var selectOwnerJson = JSONObject()
                                                        selectOwnerJson.put("ownerNm", selectOwnerChoiceData.get(i).indvdlGrpNm)
                                                        selectOwnerJson.put(
                                                            "sameNameNo",
                                                            selectOwnerChoiceData.get(i).sameNameNo
                                                        )
                                                        selectOwnerJson.put(
                                                            "delvyAdres",
                                                            selectOwnerChoiceData.get(i).delvyAdres
                                                        )
                                                        selectOwnerJson.put("delvyZip", selectOwnerChoiceData.get(i).delvyZip)
                                                        selectOwnerJson.put(
                                                            "delvyAdresDetail",
                                                            selectOwnerChoiceData.get(i).delvyAdresDetail
                                                        )
                                                        selectOwnerJson.put(
                                                            "ihidnum",
                                                            selectOwnerChoiceData.get(i).inhbtntCprNo
                                                        )
                                                        selectOwnerJson.put(
                                                            "posesnSe",
                                                            selectOwnerChoiceData.get(i).indvdlGrpSe
                                                        )
                                                        selectOwnerJson.put(
                                                            "rgistAdres",
                                                            selectOwnerChoiceData.get(i).rgistAdres
                                                        )
                                                        selectOwnerJson.put("posesnQota", "")
                                                        selectOwnerJson.put("unDcsnOwnerAt", "N")
                                                        selectOwnerJson.put(
                                                            "indvdlGrpCode",
                                                            selectOwnerChoiceData.get(i).indvdlGrpCode
                                                        )
                                                        selectOwnerJson.put("hapyuAt", "")
                                                        selectOwnerJson.put("hapyuGroupCode", "")
                                                        selectOwnerJson.put("qotaAr", "")
                                                        selectOwnerJson.put("delvyChange", "N")
                                                        selectOwnerJson.put("thingCl", "")
                                                        selectOwnerJson.put("relate", "")

                                                        ownerJson.put(selectOwnerJson)
                                                    }
                                                    newOwnerAdapterCall(ownerJson)
                                                } else {

                                                    if (selectOwnerChoiceData!!.size > 0) {

                                                        var selectOwnerArray = JSONArray()
                                                        var tempJson = JSONObject()
                                                        tempJson.put("ownerNm", "")
                                                        tempJson.put("sameNameNo", "")
                                                        tempJson.put("delvyAdres", "")
                                                        tempJson.put("delvyZip", "")
                                                        tempJson.put("delvyAdresDetail", "")
                                                        tempJson.put("ihidnum", "")
                                                        tempJson.put("posesnSe", "")
                                                        tempJson.put("rgistAdres", "")
                                                        tempJson.put("posesnQota", "")
                                                        tempJson.put("unDcsnOwnerAt", "N")
                                                        tempJson.put("indvdlGrpCode", "")
                                                        tempJson.put("hapyuAt", "")
                                                        tempJson.put("hapyuGroupCode", "")
                                                        tempJson.put("qotaAr", "")
                                                        tempJson.put("delvyChange", "N")
                                                        tempJson.put("thingCl", "")
                                                        tempJson.put("relate", "")

                                                        selectOwnerArray.put(tempJson)

                                                        for (i in 0 until selectOwnerChoiceData.size) {
                                                            var selectOwnerJson = JSONObject()
                                                            selectOwnerJson.put(
                                                                "ownerNm",
                                                                selectOwnerChoiceData.get(i).indvdlGrpNm
                                                            )
                                                            selectOwnerJson.put(
                                                                "sameNameNo",
                                                                selectOwnerChoiceData.get(i).sameNameNo
                                                            )
                                                            selectOwnerJson.put(
                                                                "delvyAdres",
                                                                selectOwnerChoiceData.get(i).delvyAdres
                                                            )
                                                            selectOwnerJson.put(
                                                                "delvyZip",
                                                                selectOwnerChoiceData.get(i).delvyZip
                                                            )
                                                            selectOwnerJson.put(
                                                                "delvyAdresDetail",
                                                                selectOwnerChoiceData.get(i).delvyAdresDetail
                                                            )
                                                            selectOwnerJson.put(
                                                                "ihidnum",
                                                                selectOwnerChoiceData.get(i).inhbtntCprNo
                                                            )
                                                            selectOwnerJson.put(
                                                                "posesnSe",
                                                                selectOwnerChoiceData.get(i).indvdlGrpSe
                                                            )
                                                            selectOwnerJson.put(
                                                                "rgistAdres",
                                                                selectOwnerChoiceData.get(i).rgistAdres
                                                            )
                                                            selectOwnerJson.put("posesnQota", "")
                                                            selectOwnerJson.put("unDcsnOwnerAt", "N")
                                                            selectOwnerJson.put(
                                                                "indvdlGrpCode",
                                                                selectOwnerChoiceData.get(i).indvdlGrpCode
                                                            )
                                                            selectOwnerJson.put("hapyuAt", "")
                                                            selectOwnerJson.put("hapyuGroupCode", "")
                                                            selectOwnerJson.put("qotaAr", "")
                                                            selectOwnerJson.put("delvyChange", "N")
                                                            selectOwnerJson.put("thingCl", "")
                                                            selectOwnerJson.put("relate", "")

                                                            selectOwnerArray.put(selectOwnerJson)
                                                        }

                                                        ownerChoiceSelectDialog.dismiss()
                                                        newOwnerAdapterCall(selectOwnerArray)


                                                    } else {
                                                        newOwnerAdapterCall(thingOwnerInfoJson!!)
                                                    }
                                                }
                                            } else {

                                                if(selectOwnerChoiceData!!.size > 0) {
                                                    for(i in 0 until selectOwnerChoiceData!!.size) {
                                                        val selectOwnerData = selectOwnerChoiceData.get(i)

                                                        layoutInflater.inflate(R.layout.fragment_add_select_owner_dialog, null).let { view ->
                                                            val ownerOwnerSelectDialog =
                                                                AddOwnerSelectDialogFragment(view).apply {
                                                                    isCancelable = false
                                                                    show(
                                                                        fragmentActivity.supportFragmentManager,
                                                                        "ownerSelectDialog"
                                                                    )
                                                                }

                                                            view.ownerListInfoView.adapter =
                                                                AddOwnerSelectDialogListAdapter(context!!)
                                                            val ownerInfo =
                                                                thingDataJson!!.getJSONArray("ownerInfo") as JSONArray
                                                            ThingMinrgtObject.addOwnerListInfo = ownerInfo

                                                            for (i in 0 until ownerInfo.length() - 1) {
                                                                (view.ownerListInfoView.adapter as AddOwnerSelectDialogListAdapter).addItem(
                                                                    ownerInfo.getJSONObject(i)
                                                                )
                                                            }

                                                            view.ownerNoText.text = "??????"
                                                            view.ownerDivisionText.text = "?????????"

                                                            view.ownerNameText.text = selectOwnerData.indvdlGrpNm
                                                            val sameNameNoString = checkStringNull(selectOwnerData.sameNameNo)
                                                            if(sameNameNoString.equals("") || sameNameNoString.equals("0")) {
                                                                view.ownerSameNameText.text = "-"

                                                            } else {
                                                                view.ownerSameNameText.text = sameNameNoString
                                                            }

                                                            val inhbtnCprNoString = checkStringNull(selectOwnerData.inhbtntCprNo)
                                                            if(inhbtnCprNoString == "" || inhbtnCprNoString == "-") {
                                                                view.ownerCrpNoText.text = inhbtnCprNoString
                                                            } else {
                                                                view.ownerCrpNoText.text = withIhidNumAsterRisk(inhbtnCprNoString)
                                                            }
                                                            view.addOwnerRgistAddrText.setText(checkStringNull(selectOwnerData.rgistAdres))

                                                            view.ownerDelvyAddrText.text = "${checkStringNull(selectOwnerData.delvyZip)} ${
                                                                checkStringNull(selectOwnerData.delvyAdres)} ${
                                                                checkStringNull(selectOwnerData.delvyAdresDetail)
                                                            } "

                                                            if(selectOwnerData.indvdlGrpSe.equals("1")) {
                                                                view.addOwnerGeoupBankSpotNm.isEnabled = false
                                                            } else {
                                                                view.addOwnerGeoupBankSpotNm.isEnabled = true
                                                            }

                                                            view.cancelBtn.setOnClickListener {
                                                                ownerOwnerSelectDialog.dismiss()
                                                            }

                                                            view.selectInputBtn.setOnClickListener {
                                                                logUtil.d("selectItemData ------------------------")

                                                                val posesnQotaString = view.addOwnerPosesnQotaNum.text.toString() +"/"+ view.addOwnerPosesnQotaDeno.text.toString()
                                                                val rgistAddrString = view.addOwnerRgistAddrText.text.toString()
                                                                val unDcsnOwnarAt = view.addOwnerUnDcsnOwnerAt.isChecked
                                                                val spotNm = view.addOwnerGeoupBankSpotNm.text.toString()

                                                                if (posesnQotaString == "/") {
                                                                    dialogUtil!!.wtnccAlertDialog(
                                                                        """?????? ???????????? ????????? ???????????? ???????????????.""".trimMargin(),
                                                                        builder!!,
                                                                        "?????????"
                                                                    ).show()

                                                                } else if (rgistAddrString == "") {
                                                                    dialogUtil!!.wtnccAlertDialog(
                                                                        """????????? ????????? ?????? ?????? ???????????????.""".trimMargin(),
                                                                        builder!!,
                                                                        "?????????"
                                                                    ).show()
                                                                } else {
                                                                    val recentOwnerInfo = ThingMinrgtObject.addOwnerListInfo as JSONArray
                                                                    val addOwnerUrl =
                                                                        context!!.resources.getString(R.string.mobile_url) + "addThingOwner"

                                                                    val addOwnerJson = JSONObject()
                                                                    val addRequestJson = JSONObject()

                                                                    addOwnerJson.put("indvdlGrpCode", selectOwnerData.indvdlGrpCode)
                                                                    addOwnerJson.put("indvdlGrpSe", selectOwnerData.indvdlGrpSe)
                                                                    addOwnerJson.put("indvdlGrpNm", selectOwnerData.indvdlGrpNm)
                                                                    addOwnerJson.put("posesnQota", posesnQotaString)
                                                                    if(unDcsnOwnarAt) {
                                                                        addOwnerJson.put("unDcsnOwnerAt", "Y")
                                                                    } else {
                                                                        addOwnerJson.put("unDcsnOwnerAt", "N")
                                                                    }

                                                                    addOwnerJson.put("rgistAdres", selectOwnerData.rgistAdres)
                                                                    addOwnerJson.put("register", PreferenceUtil.getString(context!!, "id", "defaual"))
                                                                    addOwnerJson.put("hapyuGroupCode", "")
                                                                    addOwnerJson.put("hapyuAt", "")
                                                                    addOwnerJson.put("qotaAr", "")
                                                                    addOwnerJson.put("delvyChange","N")
                                                                    addOwnerJson.put("plotCode", "")
                                                                    addOwnerJson.put("thingCl", thingDataJson!!.getJSONObject("ThingSearch").getString("thingSmallCl"))
                                                                    addOwnerJson.put("spotNm", spotNm)
                                                                    addRequestJson.put("addOwner", addOwnerJson)
                                                                    addRequestJson.put("recentOwner", recentOwnerInfo)
                                                                    addRequestJson.put("ThingSearch", thingDataJson)



                                                                    HttpUtil.getInstance(context!!)
                                                                        .callUrlJsonWebServer(addRequestJson, progressDialog, addOwnerUrl,
                                                                            object : Callback {
                                                                                override fun onFailure(call: Call, e: IOException) {

                                                                                    logUtil.e("selectInputBtn.setOnClickListener22222 progressDialog dismiss")
                                                                                    dismissProgress()
                                                                                    logUtil.e("fail")
                                                                                }

                                                                                override fun onResponse(call: Call, response: Response) {
                                                                                    val responseString = response.body!!.string()

                                                                                    logUtil.d("addOwner response ---------------------> $responseString")

                                                                                    logUtil.e("selectInputBtn.setOnClickListener22222 progressDialog dismiss")

                                                                                    dismissProgress()

                                                                                    activity!!.runOnUiThread {
                                                                                        val dataJsonObject =
                                                                                            JSONObject(responseString).getJSONObject("list")

                                                                                        recyclerViewAdapter.setJSONArray(
                                                                                            dataJsonObject.getJSONArray(
                                                                                                "ownerInfo"
                                                                                            )
                                                                                        )
                                                                                        recyclerViewAdapter.notifyDataSetChanged()

                                                                                    }

                                                                                    ownerOwnerSelectDialog.dismiss()

                                                                                }

                                                                            })


                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }

                        })
            }
            else -> {

            }
        }

    }

    override fun onNegativeClickListener(dialog: DialogInterface, type: String) {
//        newOwnerAdapterCall(thingOwnerInfoJson!!)

        when(type) {
            "?????????" -> {

            }
            "???????????????" -> {
                val ownerSearch = HashMap<String, String>()
                ownerSearch.put("searchSaupCode", PreferenceUtil.getString(context!!, "saupCode", "defaual"))
                ownerSearch.put("searchName","")
                ownerSearch.put("searchSameNameNo","")
                ownerSearch.put("searchInhbntCprNo","")

                val ownerUrl = context!!.resources.getString(R.string.mobile_url) + "ownerInfo"

                HttpUtil.getInstance(context!!)
                    .callerUrlInfoPostWebServer(ownerSearch, progressDialog, ownerUrl,
                        object: Callback, AddNewOwnerFragment.addNewOwnerSaveInterface {
                            override fun onFailure(call: Call, e: IOException) {
                                progressDialog!!.dismiss()
                                logUtil.e("fail")
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responseString = response.body!!.string()

                                progressDialog!!.dismiss()

                                val ownerInfoJson = JSONObject(responseString).getJSONObject("list").getJSONArray("owner")

                                layoutInflater.inflate(R.layout.fragment_add_owner_dialog, null).let { view ->
                                    adapter = AddOwnerInputAdapter(context!!)

                                    activity?.runOnUiThread {
                                        val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
                                        view.searchViewOwner.run {
                                            setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
                                            setIconifiedByDefault(false)
                                            isSubmitButtonEnabled = true
                                            queryHint ="????????? ?????? ??? ???????????? ??????????????????."
                                        }

                                        view.searchViewOwner.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                                            override fun onQueryTextSubmit(query: String?): Boolean {
                                                return false
                                            }

                                            override fun onQueryTextChange(query: String?): Boolean {
                                                adapter.filter.filter(query)
                                                return true
                                            }
                                        })
                                        adapter.filter.filter("")

                                        val layoutManager = LinearLayoutManager(context)
                                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                                        view.addOwnerListView.layoutManager = layoutManager

                                    }
                                    for(i in 0 until ownerInfoJson.length()) {
                                        adapter.addItem(ownerInfoJson.getJSONObject(i))
                                    }

                                    view.addOwnerListView.adapter = adapter
                                    view.addOwnerTitleText.text = context!!.getString(R.string.wtnncCommAddOwner)


                                    val ownerInfoDialog = AddOwnerDialogFragment(context!!, activity!!, view).apply {
                                        isCancelable = false
                                        show(fragmentActivity.supportFragmentManager, "ownerInfoDialog")
                                    }

                                    view.cancelBtn.setOnClickListener {
                                        ownerInfoDialog.dismiss()
                                    }

                                    view.selectInputBtn.setOnClickListener {
                                        val selectOwnerData = adapter.getSelectItem()

                                        ownerInfoDialog.dismiss()

                                        logUtil.d("selectOwnerData $selectOwnerData")

                                        if(ThingMinrgtObject.thingNewSearch == "Y") {
                                            if(ThingWtnObject.thingNewOwnerInfoJson != null) {
                                                var selectOwnerJson = JSONObject()
                                                selectOwnerJson.put("ownerNm", selectOwnerData.indvdlGrpNm)
                                                selectOwnerJson.put(
                                                    "sameNameNo",
                                                    selectOwnerData.sameNameNo
                                                )
                                                selectOwnerJson.put(
                                                    "delvyAdres",
                                                    selectOwnerData.delvyAdres
                                                )
                                                selectOwnerJson.put("delvyZip", selectOwnerData.delvyZip)
                                                selectOwnerJson.put(
                                                    "delvyAdresDetail",
                                                    selectOwnerData.delvyAdresDetail
                                                )
                                                selectOwnerJson.put(
                                                    "ihidnum",
                                                    selectOwnerData.inhbtntCprNo
                                                )
                                                selectOwnerJson.put(
                                                    "posesnSe",
                                                    selectOwnerData.indvdlGrpSe
                                                )
                                                selectOwnerJson.put(
                                                    "rgistAdres",
                                                    selectOwnerData.rgistAdres
                                                )
                                                selectOwnerJson.put("posesnQota", "")
                                                selectOwnerJson.put("unDcsnOwnerAt", "N")
                                                selectOwnerJson.put(
                                                    "indvdlGrpCode",
                                                    selectOwnerData.indvdlGrpCode
                                                )
                                                selectOwnerJson.put("hapyuAt", "")
                                                selectOwnerJson.put("hapyuGroupCode", "")
                                                selectOwnerJson.put("qotaAr", "")
                                                selectOwnerJson.put("delvyChange", "N")
                                                selectOwnerJson.put("thingCl", "")
                                                selectOwnerJson.put("relate", "")

                                                val ownerJson = ThingWtnObject.thingNewOwnerInfoJson as JSONArray
                                                ownerJson.put(selectOwnerJson)

                                                newOwnerAdapterCall(ownerJson)
                                            } else {

                                                var selectOwnerArray = JSONArray()

                                                var tempJson = JSONObject()
                                                tempJson.put("ownerNm", "")
                                                tempJson.put("sameNameNo", "")
                                                tempJson.put("delvyAdres", "")
                                                tempJson.put("delvyZip", "")
                                                tempJson.put("delvyAdresDetail", "")
                                                tempJson.put("ihidnum", "")
                                                tempJson.put("posesnSe", "")
                                                tempJson.put("rgistAdres", "")
                                                tempJson.put("posesnQota", "")
                                                tempJson.put("unDcsnOwnerAt", "N")
                                                tempJson.put("indvdlGrpCode", "")
                                                tempJson.put("hapyuAt", "")
                                                tempJson.put("hapyuGroupCode", "")
                                                tempJson.put("qotaAr", "")
                                                tempJson.put("delvyChange", "N")
                                                tempJson.put("thingCl", "")
                                                tempJson.put("relate", "")

                                                selectOwnerArray.put(tempJson)

                                                var selectOwnerJson = JSONObject()
                                                selectOwnerJson.put("ownerNm", selectOwnerData.indvdlGrpNm)
                                                selectOwnerJson.put(
                                                    "sameNameNo",
                                                    selectOwnerData.sameNameNo
                                                )
                                                selectOwnerJson.put(
                                                    "delvyAdres",
                                                    selectOwnerData.delvyAdres
                                                )
                                                selectOwnerJson.put("delvyZip", selectOwnerData.delvyZip)
                                                selectOwnerJson.put(
                                                    "delvyAdresDetail",
                                                    selectOwnerData.delvyAdresDetail
                                                )
                                                selectOwnerJson.put(
                                                    "ihidnum",
                                                    selectOwnerData.inhbtntCprNo
                                                )
                                                selectOwnerJson.put(
                                                    "posesnSe",
                                                    selectOwnerData.indvdlGrpSe
                                                )
                                                selectOwnerJson.put(
                                                    "rgistAdres",
                                                    selectOwnerData.rgistAdres
                                                )
                                                selectOwnerJson.put("posesnQota", "")
                                                selectOwnerJson.put("unDcsnOwnerAt", "N")
                                                selectOwnerJson.put(
                                                    "indvdlGrpCode",
                                                    selectOwnerData.indvdlGrpCode
                                                )
                                                selectOwnerJson.put("hapyuAt", "")
                                                selectOwnerJson.put("hapyuGroupCode", "")
                                                selectOwnerJson.put("qotaAr", "")
                                                selectOwnerJson.put("delvyChange", "N")
                                                selectOwnerJson.put("thingCl", "")
                                                selectOwnerJson.put("relate", "")

                                                selectOwnerArray.put(selectOwnerJson)

                                                newOwnerAdapterCall(selectOwnerArray)
                                            }
                                        } else {
                                            logUtil.d("selectOwnerData $selectOwnerData")

                                            layoutInflater.inflate(R.layout.fragment_add_select_owner_dialog, null).let { view ->

                                                val ownerOwnerSelectDialog = AddOwnerSelectDialogFragment(view).apply {
                                                    isCancelable = false
                                                    show(
                                                        fragmentActivity.supportFragmentManager,
                                                        "ownerSelectDialog"
                                                    )
                                                }


                                                view.ownerListInfoView.adapter =
                                                    AddOwnerSelectDialogListAdapter(context!!)
                                                val ownerInfo =
                                                    thingDataJson!!.getJSONArray("ownerInfo") as JSONArray
                                                ThingMinrgtObject.addOwnerListInfo = ownerInfo

                                                for (i in 0 until ownerInfo.length() - 1) {
                                                    (view.ownerListInfoView.adapter as AddOwnerSelectDialogListAdapter).addItem(
                                                        ownerInfo.getJSONObject(i)
                                                    )
                                                }

                                                view.ownerNoText.text = "??????"
                                                view.ownerDivisionText.text = "?????????"

                                                view.ownerNameText.text = selectOwnerData.indvdlGrpNm
                                                val sameNameNoString = checkStringNull(selectOwnerData.sameNameNo)
                                                if(sameNameNoString.equals("") || sameNameNoString.equals("0")) {
                                                    view.ownerSameNameText.text = "-"

                                                } else {
                                                    view.ownerSameNameText.text = sameNameNoString
                                                }

                                                val inhbtnCprNoString = checkStringNull(selectOwnerData.inhbtntCprNo)
                                                if(inhbtnCprNoString == "" || inhbtnCprNoString == "-") {
                                                    view.ownerCrpNoText.text = inhbtnCprNoString
                                                } else {
                                                    view.ownerCrpNoText.text = withIhidNumAsterRisk(inhbtnCprNoString)
                                                }
                                                view.addOwnerRgistAddrText.setText(checkStringNull(selectOwnerData.rgistAdres))

                                                view.ownerDelvyAddrText.text = "${checkStringNull(selectOwnerData.delvyZip)} ${
                                                    checkStringNull(selectOwnerData.delvyAdres)} ${
                                                    checkStringNull(selectOwnerData.delvyAdresDetail)
                                                } "

                                                if(selectOwnerData.indvdlGrpSe.equals("1")) {
                                                    view.addOwnerGeoupBankSpotNm.isEnabled = false
                                                } else {
                                                    view.addOwnerGeoupBankSpotNm.isEnabled = true
                                                }

                                                view.cancelBtn.setOnClickListener {
                                                    ownerOwnerSelectDialog.dismiss()
                                                }

                                                view.selectInputBtn.setOnClickListener {

                                                    logUtil.d("selectItemData ------------------------")

                                                    val posesnQotaString = view.addOwnerPosesnQotaNum.text.toString() +"/"+ view.addOwnerPosesnQotaDeno.text.toString()
                                                    val rgistAddrString = view.addOwnerRgistAddrText.text.toString()
                                                    val unDcsnOwnarAt = view.addOwnerUnDcsnOwnerAt.isChecked
                                                    val spotNm = view.addOwnerGeoupBankSpotNm.text.toString()

                                                    if (posesnQotaString == "/") {
                                                        dialogUtil!!.wtnccAlertDialog(
                                                            """?????? ???????????? ????????? ???????????? ???????????????.""".trimMargin(),
                                                            builder!!,
                                                            "?????????"
                                                        ).show()

                                                    } else if (rgistAddrString == "") {
                                                        dialogUtil!!.wtnccAlertDialog(
                                                            """????????? ????????? ?????? ?????? ???????????????.""".trimMargin(),
                                                            builder!!,
                                                            "?????????"
                                                        ).show()
                                                    } else {
                                                        val recentOwnerInfo = ThingMinrgtObject.addOwnerListInfo as JSONArray
                                                        val addOwnerUrl =
                                                            context!!.resources.getString(R.string.mobile_url) + "addThingOwner"

                                                        val addOwnerJson = JSONObject()
                                                        val addRequestJson = JSONObject()

                                                        addOwnerJson.put("indvdlGrpCode", selectOwnerData.indvdlGrpCode)
                                                        addOwnerJson.put("indvdlGrpSe", selectOwnerData.indvdlGrpSe)
                                                        addOwnerJson.put("indvdlGrpNm", selectOwnerData.indvdlGrpNm)
                                                        addOwnerJson.put("posesnQota", posesnQotaString)
                                                        if(unDcsnOwnarAt) {
                                                            addOwnerJson.put("unDcsnOwnerAt", "Y")
                                                        } else {
                                                            addOwnerJson.put("unDcsnOwnerAt", "N")
                                                        }

                                                        addOwnerJson.put("rgistAdres", selectOwnerData.rgistAdres)
                                                        addOwnerJson.put("register", PreferenceUtil.getString(context!!, "id", "defaual"))
                                                        addOwnerJson.put("hapyuGroupCode", "")
                                                        addOwnerJson.put("hapyuAt", "")
                                                        addOwnerJson.put("qotaAr", "")
                                                        addOwnerJson.put("delvyChange","N")
                                                        addOwnerJson.put("plotCode", "")
                                                        addOwnerJson.put("thingCl", thingDataJson!!.getJSONObject("ThingSearch").getString("thingSmallCl"))
                                                        addOwnerJson.put("spotNm", spotNm)
                                                        addRequestJson.put("addOwner", addOwnerJson)
                                                        addRequestJson.put("recentOwner", recentOwnerInfo)
                                                        addRequestJson.put("ThingSearch", thingDataJson)

                                                        HttpUtil.getInstance(context!!)
                                                            .callUrlJsonWebServer(addRequestJson, progressDialog, addOwnerUrl,
                                                                object : Callback {
                                                                    override fun onFailure(call: Call, e: IOException) {

                                                                        logUtil.e("selectInputBtn.setOnClickListener22222 progressDialog dismiss")
                                                                        dismissProgress()
                                                                        logUtil.e("fail")
                                                                    }

                                                                    override fun onResponse(call: Call, response: Response) {
                                                                        val responseString = response.body!!.string()

                                                                        logUtil.d("addOwner response ---------------------> $responseString")

                                                                        logUtil.e("selectInputBtn.setOnClickListener22222 progressDialog dismiss")

                                                                        dismissProgress()

                                                                        activity!!.runOnUiThread {
                                                                            val dataJsonObject =
                                                                                JSONObject(responseString).getJSONObject("list")

                                                                            recyclerViewAdapter.setJSONArray(
                                                                                dataJsonObject.getJSONArray(
                                                                                    "ownerInfo"
                                                                                )
                                                                            )
                                                                            recyclerViewAdapter.notifyDataSetChanged()

                                                                        }

                                                                        ownerOwnerSelectDialog.dismiss()

                                                                    }

                                                                })
                                                    }
                                                }
                                            }
                                        }



                                    }
                                    view.searchAddOwnerBtn.setOnClickListener {
                                        AddNewOwnerFragment(activity!!, context!!, this).show((context as MapActivity).supportFragmentManager, "addNewOwnerFragment")
                                        ownerInfoDialog.dismiss()
                                    }
                                }
                            }

                            override fun onSaveOwner(dataInfo: JSONObject, grpSe: Int) {
                                progressDialog!!.dismiss()

                                if(ThingMinrgtObject.thingNewSearch.equals("N")) {
                                    layoutInflater.inflate(R.layout.fragment_add_select_owner_dialog, null).let { view ->
                                        val ownerOwnerSelectDialog = AddOwnerSelectDialogFragment(view).apply {
                                            isCancelable = false
                                            show(fragmentActivity.supportFragmentManager, "ownerSelectDialog")
                                        }

                                        view.ownerListInfoView.adapter = AddOwnerSelectDialogListAdapter(context!!)
                                        val ownerInfo = thingDataJson!!.getJSONArray("ownerInfo") as JSONArray
                                        ThingMinrgtObject.addOwnerListInfo = ownerInfo

                                        for(i in 0 until ownerInfo.length() -1) {
                                            (view.ownerListInfoView.adapter as AddOwnerSelectDialogListAdapter).addItem(
                                                ownerInfo.getJSONObject(i)
                                            )
                                        }

                                        view.ownerNoText.text = "??????"
                                        view.ownerDivisionText.text = "?????????"

                                        if (grpSe == 1) {
                                            view.ownerNameText.text = dataInfo.getString("name")
                                        } else {
                                            view.ownerNameText.text = dataInfo.getString("grpNm")
                                        }

                                        val sameNameNoString = checkStringNull(dataInfo.getString("sameNameNo"))
                                        if (sameNameNoString.equals("") || sameNameNoString.equals("0")) {
                                            view.ownerSameNameText.text = "-"
                                        } else {
                                            view.ownerSameNameText.text = sameNameNoString
                                        }

                                        if (grpSe == 1) {
                                            val inhbtntCprNoString = checkStringNull(dataInfo.getString("ihidnum"))
                                            if (inhbtntCprNoString == "" || inhbtntCprNoString == "-") {
                                                view.ownerCrpNoText.text = inhbtntCprNoString
                                            } else {
                                                //val inhbtntCprNoStringSub = inhbtntCprNoString.substring(0, 8)
                                                //view.ownerCrpNoText.text = "$inhbtntCprNoStringSub ******"
                                                view.ownerCrpNoText.text = withIhidNumAsterRisk(inhbtntCprNoString)
                                            }
                                        } else {

                                            val inhbtntCprNoString = checkStringNull(dataInfo.getString("jurirno"))
                                            if (inhbtntCprNoString == "" || inhbtntCprNoString == "-") {
                                                view.ownerCrpNoText.text = inhbtntCprNoString
                                            } else {
                                                //val inhbtntCprNoStringSub = inhbtntCprNoString.substring(0, 8)
                                                //view.ownerCrpNoText.text = "$inhbtntCprNoStringSub ******"
                                                view.ownerCrpNoText.text = withIhidNumAsterRisk(inhbtntCprNoString)
                                            }
                                        }

                                        view.ownerDelvyAddrText.text =
                                            "${checkStringNull(dataInfo.getString("delvyZip"))} ${
                                                checkStringNull(dataInfo.getString("delvyAdres"))
                                            } ${checkStringNull(dataInfo.getString("delvyAdresDetail"))}"

                                        view.cancelBtn.setOnClickListener { ownerOwnerSelectDialog.dismiss() }
                                        view.selectInputBtn.setOnClickListener {
                                            logUtil.d("selectItemData ------------------------")

                                            val posesnQotaString = view.addOwnerPosesnQotaNum.text.toString() +"-"+ view.addOwnerPosesnQotaDeno.text.toString()
                                            val rgistAddrString = view.addOwnerRgistAddrText.text.toString()
                                            val unDcsnOwnarAt = view.addOwnerUnDcsnOwnerAt.isChecked

                                            if (posesnQotaString == "") {
                                                dialogUtil!!.wtnccAlertDialog(
                                                    """?????? ???????????? ????????? ???????????? ???????????????.""".trimMargin(),
                                                    builder!!,
                                                    "????????? ??????"
                                                ).show()

                                            } else if (rgistAddrString == "") {
                                                dialogUtil!!.wtnccAlertDialog(
                                                    """????????? ????????? ?????? ?????? ???????????????.""".trimMargin(),
                                                    builder!!,
                                                    "????????? ??????"
                                                ).show()
                                            } else {
                                                val recentOwnerInfo = ThingMinrgtObject.addOwnerListInfo as JSONArray

                                                val addOwnerUrl =
                                                    context!!.resources.getString(R.string.mobile_url) + "addThingOwner"

                                                val addOwnerJson = JSONObject()
                                                val addRequestJson = JSONObject()

                                                addOwnerJson.put("delvyAdres", checkStringNull(dataInfo.getString("delvyAdres")))
                                                addOwnerJson.put(
                                                    "delvyAdresDetail",
                                                    checkStringNull(dataInfo.getString("delvyAdresDetail"))
                                                )
                                                addOwnerJson.put("delvyZip", checkStringNull(dataInfo.getString("delvyZip")))


                                                if (grpSe == 1) {
                                                    addOwnerJson.put(
                                                        "indvdlGrpCode",
                                                        checkStringNull(dataInfo.getString("onivCode"))
                                                    )
                                                    addOwnerJson.put("indvdlGrpSe", "1")
                                                    addOwnerJson.put("indvdlGrpNm", checkStringNull(dataInfo.getString("name")))
                                                } else {
                                                    addOwnerJson.put(
                                                        "indvdlGrpCode",
                                                        checkStringNull(dataInfo.getString("grpEntrpsCode"))
                                                    )
                                                    addOwnerJson.put("indvdlGrpSe", "2")
                                                    addOwnerJson.put("indvdlGrpNm", checkStringNull(dataInfo.getString("grpNm")))
                                                }

                                                addOwnerJson.put("posesnQota", posesnQotaString)
                                                if (unDcsnOwnarAt) {
                                                    addOwnerJson.put("unDcsnOwnerAt", "Y")
                                                } else {
                                                    addOwnerJson.put("unDcsnOwnerAt", "N")
                                                }
                                                addOwnerJson.put("rgistAdres", rgistAddrString)
                                                addOwnerJson.put("register", PreferenceUtil.getString(context!!, "id", "defaual"))
                                                addOwnerJson.put("hapyuGroupCode", "")
                                                addOwnerJson.put("hapyuAt", "")
                                                addOwnerJson.put("qotaAr", "")
                                                addOwnerJson.put("delvyChange","N")
                                                addOwnerJson.put("plotCode", "")
                                                addOwnerJson.put(
                                                    "thingCl",
                                                    thingDataJson!!.getJSONObject("ThingSearch").getString("thingSmallCl")
                                                )

                                                addRequestJson.put("addOwner", addOwnerJson)
                                                addRequestJson.put("recentOwner", recentOwnerInfo)
                                                addRequestJson.put("ThingSearch", thingDataJson)

                                                HttpUtil.getInstance(context!!)
                                                    .callUrlJsonWebServer(addRequestJson, progressDialog, addOwnerUrl,
                                                        object: Callback {
                                                            override fun onFailure(call: Call, e: IOException) {
                                                                progressDialog!!.dismiss()
                                                                logUtil.e("fail")
                                                            }

                                                            override fun onResponse(call: Call, response: Response) {
                                                                val responseString = response.body!!.string()

                                                                logUtil.d("addOwner response ---------------------->$responseString")

                                                                progressDialog!!.dismiss()

                                                                activity!!.runOnUiThread {
                                                                    val dataJsonObject = JSONObject(responseString).getJSONObject("list")

                                                                    recyclerViewAdapter.setJSONArray(dataJsonObject.getJSONArray("ownerInfo"))

                                                                    recyclerViewAdapter.notifyDataSetChanged()
                                                                }

                                                                ownerOwnerSelectDialog.dismiss()
                                                            }

                                                        })
                                            }
                                        }
                                    }
                                } else {
                                    activity?.runOnUiThread {
                                        val addOwnerData = ThingMinrgtObject.thingOwnerInfoJson as JSONArray

                                        if(grpSe == 1) {
                                            dataInfo.put("posesnSe", "1")
                                            dataInfo.put("ownerNm", dataInfo.getString("name"))
                                            dataInfo.put("indvdlGrpTy", "??????")
                                            dataInfo.put("indvdlGrpCode",dataInfo.getString("onivCode"))
                                        } else {
                                            dataInfo.put("posesnSe", "2")
                                            dataInfo.put("ownerNm", dataInfo.getString("grpNm"))
                                            dataInfo.put("indvdlGrpTy", "??????")
                                            dataInfo.put("indvdlGrpCode",dataInfo.getString("grpEntrpsCode"))
                                        }
                                        dataInfo.put("delvyChange","N")

                                        addOwnerData.put(dataInfo)

                                        ThingWtnObject.thingNewOwnerInfoJson = addOwnerData

                                        newOwnerRecyclerViewAdapter.setJSONArray(addOwnerData)
                                        newOwnerRecyclerViewAdapter.notifyDataSetChanged()

                                        progressDialog!!.dismiss()
                                    }
                                }
                            }

                        })
            }
            else -> {

            }
        }

    }

    fun newOwnerAdapterCall(array: JSONArray) {
        ThingMinrgtObject.thingOwnerInfoJson = array
        ThingWtnObject.thingNewOwnerInfoJson = array
        newOwnerRecyclerViewAdapter = NewOwnerRecyclerViewAdapter(context!!, array, this)
        newOwnerRecyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        newOwnerRecyclerView.adapter = newOwnerRecyclerViewAdapter
    }

    override fun onNewMinusNewOwnerBtnClicked() {
        ThingWtnObject.thingNewOwnerInfoJson = null
        ThingMinrgtObject.thingOwnerInfoJson = null
        newOwnerRecyclerViewAdapter.setJSONArray(thingOwnerInfoJson!!)

        newOwnerRecyclerViewAdapter.notifyDataSetChanged()
    }

    override fun onNewOwnerNewAddBtnClicked() {


        dialogUtil?.run {
            alertDialog(
                "????????? ??????",
                "?????? ?????? ??? ????????? ???????????? ?????????????????????????",
                builder!!,
                "???????????????"
            ).show()
        }
    }

    override fun onNewOwnerViewClicked(position: Int) {
        val addOwnerData = ThingMinrgtObject.thingOwnerInfoJson as JSONArray

        val data = addOwnerData.getJSONObject(position)

        layoutInflater.inflate(R.layout.fragment_add_new_modify_owner_dialog, null).let { view ->

            val ownerAddSelectDialog = AddOwnerSelectDialogFragment(view).apply {
                isCancelable = false
                show(fragmentActivity.supportFragmentManager, "ownerAssSelectDialog")
            }
            view.newOwnerNoText.text = "??????"
            val posesnSeString = checkStringNull(data.getString("posesnSe"))
            view.newOwnerDivisionText.text = when (posesnSeString) {
                "1" -> "??????"
                "2" -> "??????"
                else -> ""
            }
            view.newOwnerNameText.text = checkStringNull(data.getString("ownerNm"))
            val newOwnerSameNameString = checkStringNull(data.getString("sameNameNo"))
            if (newOwnerSameNameString.equals("")) {
                view.newOwnerSameNameText.text = "-"
            } else {
                view.newOwnerSameNameText.text = newOwnerSameNameString
            }
            view.addOwnerRgistAddrText.setText(checkStringNull(data.getString("rgistAdres")))
            val posesnQota = checkStringNull(data.getString("posesnQota"))
            if(posesnQota != "") {
                val posesnQotaSplit = posesnQota.split("/")
                view.newOwnerPosesnQotaNum.setText(posesnQotaSplit[0].toString())
                view.newOwnerPosesnQotaDeno.setText(posesnQotaSplit[1].toString())
            } else {
                view.newOwnerPosesnQotaNum.setText("")
                view.newOwnerPosesnQotaDeno.setText("")
            }
            val newOwnerUnDcsnOwnerString = checkStringNull(data.getString("unDcsnOwnerAt"))
            view.newOwnerUnDcsnOwnerAt.isChecked = when (newOwnerUnDcsnOwnerString) {
                "Y" -> true
                else -> false

            }
            val newOwnerCrpNoString = checkStringNull(data.getString("ihidnum"))
            if (newOwnerCrpNoString.equals("")) {
                view.newOwnerCrpNoText.text = newOwnerCrpNoString
            } else {
                //val newOwnerCrpNoStringSub = newOwnerCrpNoString.substring(0, 8)
                //view.newOwnerCrpNoText.text = "$newOwnerCrpNoStringSub ******"
                view.newOwnerCrpNoText.text = withIhidNumAsterRisk(newOwnerCrpNoString)
            }
            //????????????
            view.newOwnerDelvyAddrText.text =
                "${checkStringNull(data.getString("delvyZip"))} ${checkStringNull(data.getString("delvyAdres"))} ${
                    checkStringNull(data.getString("delvyAdresDetail"))
                }"

//            view.newOwnerRmText.setText(checkStringNull(data.getString("ownerRm")))

            view.cancelBtn.setOnClickListener { ownerAddSelectDialog.dismiss() }

            view.selectInputBtn.setOnClickListener {
                var selectOwnerJson = JSONObject()
                selectOwnerJson.put("ownerNm", data.getString("ownerNm"))
                selectOwnerJson.put("sameNameNo", data.getString("sameNameNo"))
                selectOwnerJson.put("delvyAdres", data.getString("delvyAdres"))
                selectOwnerJson.put("delvyZip", data.getString("delvyZip"))
                selectOwnerJson.put("delvyAdresDetail", data.getString("delvyAdresDetail"))
                selectOwnerJson.put("ihidnum", data.getString("ihidnum"))
                selectOwnerJson.put("posesnSe", data.getString("posesnSe"))
                selectOwnerJson.put("rgistAdres", view.addOwnerRgistAddrText.text.toString())
                val posesnQotaString = view.newOwnerPosesnQotaNum.text.toString() + "/" + view.newOwnerPosesnQotaDeno.text.toString()
                selectOwnerJson.put("posesnQota",posesnQotaString)
                selectOwnerJson.put(
                    "unDcsnOwnerAt", when (view.newOwnerUnDcsnOwnerAt.isChecked) {
                        true -> "Y"
                        else -> "N"
                    }
                )
                selectOwnerJson.put("indvdlGrpCode", data.getString("indvdlGrpCode"))
                selectOwnerJson.put("hapyuAt", "")
                selectOwnerJson.put("hapyuGroupCode", "")
                selectOwnerJson.put("qotaAr", "")
                selectOwnerJson.put("thingCl", "")
                selectOwnerJson.put("delvyChange", "N")
                selectOwnerJson.put("relate", "")

                var ownerJsonArray = ThingMinrgtObject.thingOwnerInfoJson as JSONArray

                ownerJsonArray.put(position, selectOwnerJson)

                ThingWtnObject.thingNewOwnerInfoJson = ownerJsonArray

                newOwnerRecyclerViewAdapter.setJSONArray(ownerJsonArray)
                newOwnerRecyclerViewAdapter.notifyDataSetChanged()

                ownerAddSelectDialog.dismiss()
            }

        }
    }

}