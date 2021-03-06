/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_add_owner_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_owner_dialog.view.cancelBtn
import kotlinx.android.synthetic.main.fragment_add_owner_dialog.view.selectInputBtn
import kotlinx.android.synthetic.main.fragment_add_owner_relate.view.*
import kotlinx.android.synthetic.main.fragment_add_select_owner_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerCrpNoText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerDelvyAddrText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerDivisionText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerNameText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerSameNameText
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.AddOwnerInputAdapter
import kr.or.kreb.ncms.mobile.adapter.AddOwnerSelectDialogListAdapter
import kr.or.kreb.ncms.mobile.adapter.BaseOwnerRecyclerViewAdapter
import kr.or.kreb.ncms.mobile.adapter.OwnerRecyclerViewAdapter
import kr.or.kreb.ncms.mobile.base.BaseFragment
import kr.or.kreb.ncms.mobile.data.LandInfoObject
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.util.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class LandOwnerFragment(val fragmentActivity: FragmentActivity) : BaseFragment(),
    BaseOwnerRecyclerViewAdapter.OnOwnerEventListener {

//    private lateinit var recyclerViewAdapter: LandOwnerRecyclerViewAdapter
//    private lateinit var recyclerViewAdapter: OwnerRecyclerViewAdapter
//
//    private var logUtil: LogUtil = LogUtil("LandOwnerFragment")
//    private var progressDialog: AlertDialog? = null
//    var builder: MaterialAlertDialogBuilder? = null
//    var dialogUtil: DialogUtil? = null
    var landDataJson: JSONObject? = null
    var dcsnAt: String? = "N"

    private lateinit var adapter: AddOwnerInputAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_add_owner_relate, null)

        dialogUtil = DialogUtil(context, activity)
        builder = context?.let { MaterialAlertDialogBuilder(it) }!!
        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))

        init(view)

        return view
    }

    fun init(view: View) {

        val dataString = requireActivity().intent!!.extras!!.get("LandInfo") as String?

        landDataJson = JSONObject(dataString!!)

        val landOwnerInfoJson = landDataJson!!.getJSONArray("ownerInfo") as JSONArray
        dcsnAt = landDataJson!!.getJSONObject("LandInfo").getString("dcsnAt")

        LandInfoObject.landOwnerInfoJson = landOwnerInfoJson

        recyclerViewAdapter = OwnerRecyclerViewAdapter(context!!, BizEnum.LAD, landOwnerInfoJson, dcsnAt!!, this)
        view.ownerRecyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        view.ownerRecyclerView.adapter = recyclerViewAdapter




    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

//    override fun onDelvyAddrClicked(data: JSONObject) {
//        logUtil.d("onDelvyAddrClick data >>>>>>>>>>>>>>>>>>>>>> $data")
//    }

    override fun onAddRelateBtnClicked(data: JSONObject) {

//        val dcsnAt = landDataJson!!.getString("dcsnAt")

//        val dcsnAt = landDataJson!!.getJSONObject("LandInfo").getString("dcsnAt")

        if(dcsnAt == "Y") {
            activity?.runOnUiThread {
                toast.msg_error(R.string.msg_search_dcsc_at_resut, 100)
            }
        } else {
            logUtil.d("onAddRelateBtnClick >>>>>>>>>>>>>>>>>>>>>>>> $data")

            val ownerData = data

            val ownerSearch = HashMap<String, String>()
            ownerSearch.put("searchSaupCode",PreferenceUtil.getString(context!!, "saupCode", "defaual"))
            ownerSearch.put("searchName", "")
            ownerSearch.put("searchSameNameNo","")
            ownerSearch.put("searchInhbtntCprNo","")

            val ownerUrl = context!!.getString(R.string.mobile_url) + "ownerInfo"

            HttpUtil.getInstance(context!!)
                .callerUrlInfoPostWebServer(ownerSearch, progressDialog, ownerUrl,
                    object:Callback, AddNewOwnerFragment.addNewOwnerSaveInterface  {
                        override fun onFailure(call: Call, e: IOException) {
                            progressDialog!!.dismiss()
                            logUtil.e("fail")
                        }

                        override fun onResponse(call: Call, response: Response) {

                            val responseString = response.body!!.string()
                            logUtil.d("onwerInfore Response ---------------------> $responseString")

                            progressDialog!!.dismiss()

                            val ownerInfoJson = JSONObject(responseString).getJSONObject("list").getJSONArray("owner")

                            layoutInflater.inflate(R.layout.fragment_add_owner_dialog, null).let{ view ->

                                // TODO: 2021-11-04 ?????? ???????????? -> ?????????????????? (+ ?????????????????? ??????)
                                view.addOwnerTitleText.text = context!!.getString(R.string.wtnncCommAddRelate)
                                adapter = AddOwnerInputAdapter(context!!)

                                activity?.runOnUiThread {

                                    val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
                                    view.searchViewOwner.apply {
                                        setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
                                        setIconifiedByDefault(false)
                                        isSubmitButtonEnabled = true
                                        queryHint ="????????? ?????? ??? ???????????? ??????????????????."

                                        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                                            override fun onQueryTextSubmit(query: String?): Boolean {

                                                //val searchRecentSuggestions = SearchRecentSuggestions(context, SearchHistoryProvider.AUTHORITY, SearchHistoryProvider.MODE)
                                                //searchRecentSuggestions.saveRecentQuery(query, null)
                                                //searchRecentSuggestions.clearHistory()
                                                return true
                                            }

                                            override fun onQueryTextChange(query: String?): Boolean { adapter.filter.filter(query); return true }
                                        })

                                        setOnSuggestionListener(object : SearchView.OnSuggestionListener {

                                            override fun onSuggestionSelect(position: Int): Boolean { logUtil.d("sugSelect>> $position"); return true }

                                            @SuppressLint("Range")
                                            override fun onSuggestionClick(position: Int): Boolean {
                                                val cursor: Cursor = view.searchViewOwner.suggestionsAdapter.getItem(position) as Cursor
                                                val suggest1 = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)) // ????????? recentQuery String
                                                logUtil.d("????????? ????????? -> $suggest1")
                                                view.searchViewOwner.setQuery(suggest1, true)
                                                return true
                                            }

                                        })

                                    }

                                    adapter.filter.filter("")

                                    val layoutManager = LinearLayoutManager(context)
                                    layoutManager.orientation = LinearLayoutManager.VERTICAL
                                    view.addOwnerListView.layoutManager = layoutManager
                                }

                                for(i in 0 until ownerInfoJson.length()) {
                                    adapter.addItem(ownerInfoJson.getJSONObject(i))
                                }

                                view.addOwnerListView.adapter = adapter
                                view.addOwnerTitleText.text = context!!.getString(R.string.wtnncCommAddRelate)
                                view.searchAddOwnerBtn.text = context!!.getString(R.string.wtnncCommAddRelateNew)

                                // ???????????? ???

                                val ownerInfoDialog = AddOwnerDialogFragment(context!!, activity!!, view).apply{
                                    isCancelable = false
                                    show(fragmentActivity.supportFragmentManager,"ownerInfoDialog")
                                }
                                view.cancelBtn.setOnClickListener {
                                    ownerInfoDialog.dismiss()
                                }
                                view.selectInputBtn.setOnClickListener {
                                    val selectOwnerdata = adapter.getSelectItem()

                                    ownerInfoDialog.dismiss()

                                    layoutInflater.inflate(R.layout.fragment_add_select_relate_dialog, null).let{view ->

                                        val ownerRelateSelectDialog = AddOwnerSelectDialogFragment(
                                            view
                                        ).apply {
                                            isCancelable = false
                                            show(fragmentActivity.supportFragmentManager,"ownerRelateSeleectDialog")

                                        }

                                        view.ownerDivisionText.text = selectOwnerdata.indvdlGrpSeNm
                                        view.ownerNameText.text = selectOwnerdata.indvdlGrpNm

                                        val sameNameNoString = checkStringNull(selectOwnerdata.sameNameNo)
                                        if(sameNameNoString == "") {
                                            view.ownerSameNameText.text = "-"
                                        } else {
                                            view.ownerSameNameText.text = sameNameNoString
                                        }

                                        val crpNoString = checkStringNull(selectOwnerdata.inhbtntCprNo)
                                        if(crpNoString.equals("")) {
                                            view.ownerCrpNoText.text = crpNoString
                                        } else {
                                            //val crpNoStringSub = crpNoString.substring(0,8)
                                            val crpNoStringSub = withIhidNumAsterRisk(crpNoString)
                                            view.ownerCrpNoText.text = crpNoStringSub
                                        }

                                        view.ownerDelvyAddrText.text = "${checkStringNull(selectOwnerdata.delvyZip)} ${checkStringNull(selectOwnerdata.delvyAdres)} ${checkStringNull(selectOwnerdata.delvyAdresDetail)}"

                                        view.cancelBtn.setOnClickListener {
                                            ownerRelateSelectDialog.dismiss()
                                        }
                                        view.selectInputBtn.setOnClickListener {

                                            logUtil.d("selectItemData size -----------------------> ${selectOwnerdata.toString()}")

                                            val pcnRightRelateString = view.ownerPcnRightRelate.text.toString()

                                            if(pcnRightRelateString == "") {
                                                dialogUtil!!.wtnccAlertDialog("""????????? ??????????????? ???????????? ???????????????.""".trimMargin(), builder!!, "???????????????").show()

                                            } else {

                                                val addRelateUrl = context!!.resources.getString(R.string.mobile_url) + "addLandRelate"

                                                val relateAddJson = JSONObject()
                                                val relateAdddata = JSONObject()

                                                relateAdddata.put("delvyAdres", selectOwnerdata.delvyAdres)
                                                relateAdddata.put("delvyAdresDetail", selectOwnerdata.delvyAdresDetail)
                                                relateAdddata.put("delvyZip", selectOwnerdata.delvyZip)
                                                relateAdddata.put("indvdlGrpCode", selectOwnerdata.indvdlGrpCode)
                                                relateAdddata.put("indvdlGrpNm", selectOwnerdata.indvdlGrpNm)
                                                relateAdddata.put("indvdlGrpSe", selectOwnerdata.indvdlGrpSe)
                                                relateAdddata.put("indvdlGrpSeNm", selectOwnerdata.indvdlGrpSeNm)
                                                relateAdddata.put("inhbtntCprNo", selectOwnerdata.inhbtntCprNo)
                                                relateAdddata.put("sameNameNo", selectOwnerdata.sameNameNo)
                                                relateAdddata.put("pcnRightRelate", pcnRightRelateString)
                                                relateAdddata.put("ladWtnOwnerCode", ownerData.getString("ladWtnOwnerCode"))
                                                relateAdddata.put("register",PreferenceUtil.getString(context!!, "id", "defaual"))

                                                relateAddJson.put("addRelate", relateAdddata)
                                                relateAddJson.put("ownerInfo", ownerData)
                                                relateAddJson.put("landInfo", landDataJson)

                                                HttpUtil.getInstance(context!!)
                                                    .callUrlJsonWebServer(relateAddJson, progressDialog, addRelateUrl,
                                                        object : Callback {
                                                            override fun onFailure(call: Call, e: IOException) {
                                                                progressDialog!!.dismiss()
                                                                logUtil.d("fail")
                                                            }

                                                            override fun onResponse(call: Call, response: Response) {
                                                                val responseString = response.body!!.string()

                                                                logUtil.d("addRelate response --------------> $responseString")

                                                                progressDialog!!.dismiss()

                                                                activity!!.runOnUiThread{
                                                                    val dataJsonObject = JSONObject(responseString).getJSONObject("list")

                                                                    recyclerViewAdapter.setJSONArray(dataJsonObject.getJSONArray("ownerInfo"))

                                                                    recyclerViewAdapter.notifyDataSetChanged()
                                                                }

                                                                ownerRelateSelectDialog.dismiss()
                                                            }

                                                        })
                                            }
                                        }
                                    }
                                }
                                view.searchAddOwnerBtn.setOnClickListener {
                                    logUtil.d("searchAddOwnerBtn <><><><><><><><><><><><><><><><><><><><><><><><><>")

                                    AddNewOwnerFragment(activity!!, context!!, this).show((context as MapActivity).supportFragmentManager, "addNewOwnerFragment")
                                    ownerInfoDialog.dismiss()
                                }
                            }
                        }

                        override fun onSaveOwner(dataInfo: JSONObject, ownerType: Int) {
                            layoutInflater.inflate(R.layout.fragment_add_select_relate_dialog, null).let{view ->
                                val ownerRelateSelectDialog = AddOwnerSelectDialogFragment(view).apply {
                                    isCancelable = false
                                    show(fragmentActivity.supportFragmentManager, "ownerRelateSeleectDialog")
                                }

                                if(ownerType == 1) {
                                    view.ownerDivisionText.text = "??????"
                                    val crpNoString = checkStringNull(dataInfo.getString("ihidnum"))
                                    if(crpNoString.equals("")) {
                                        view.ownerCrpNoText.text = crpNoString
                                    } else {
                                        //val crpNoStringSub = crpNoString.substring(0,8)
                                        val crpNoStringSub = withIhidNumAsterRisk(crpNoString)
                                        view.ownerCrpNoText.text = crpNoStringSub
                                    }
                                    view.addOwnerRelateBankAt.isEnabled = false
                                    view.ownerRelateBankSpotNm.isEnabled = false
                                } else {
                                    view.ownerDivisionText.text = "??????"
                                    val crpNoString = checkStringNull(dataInfo.getString("jurirno"))
                                    if(crpNoString.equals("")) {
                                        view.ownerCrpNoText.text = crpNoString
                                    } else {
                                        //val crpNoStringSub = crpNoString.substring(0,8)
                                        val crpNoStringSub = withIhidNumAsterRisk(crpNoString)
                                        view.ownerCrpNoText.text = crpNoStringSub
                                    }
                                    view.addOwnerRelateBankAt.isEnabled = true
                                    view.ownerRelateBankSpotNm.isEnabled = true
                                }

                                view.ownerNameText.text = checkStringNull(dataInfo.getString("name"))
                                val sameNameNoString = checkStringNull(dataInfo.getString("sameNameNo"))
                                if(sameNameNoString == "") {
                                    view.ownerSameNameText.text = "-"
                                } else {
                                    view.ownerSameNameText.text = sameNameNoString
                                }

                                view.ownerDelvyAddrText.text = "${checkStringNull(dataInfo.getString("delvyZip"))} ${checkStringNull(dataInfo.getString("delvyAdres"))} ${checkStringNull(dataInfo.getString("delvyAdresDetail"))}"

                                view.selectInputBtn.setOnClickListener {

                                    val pcnRightRelateString = view.ownerPcnRightRelate.text.toString()

                                    if(pcnRightRelateString == "") {
                                        dialogUtil!!.wtnccAlertDialog("""????????? ??????????????? ???????????? ???????????????.""".trimMargin(), builder!!, "???????????????").show()

                                    } else {
                                        val addRelateUrl = context!!.resources.getString(R.string.mobile_url) + "addLandRelate"

                                        val relateAddJson = JSONObject()
                                        val relateAdddata = JSONObject()

                                        relateAdddata.put("delvyAdres", checkStringNull(dataInfo.getString("delvyAdres")))
                                        relateAdddata.put("delvyAdresDetail", checkStringNull(dataInfo.getString("delvyAdresDetail")))
                                        relateAdddata.put("delvyZip", checkStringNull(dataInfo.getString("delvyZip")))
                                        if(ownerType == 1) {
                                            relateAdddata.put("indvdlGrpCode", checkStringNull(dataInfo.getString("onivCode")))
                                            relateAdddata.put("indvdlGrpNm", checkStringNull(dataInfo.getString("name")))
                                            relateAdddata.put("indvdlGrpSe", "1")
                                        } else {
                                            relateAdddata.put("indvdlGrpCode", checkStringNull(dataInfo.getString("grpEntrpsCode")))
                                            relateAdddata.put("indvdlGrpNm", checkStringNull(dataInfo.getString("grpNm")))
                                            relateAdddata.put("indvdlGrpSe", "2")
                                        }

                                        relateAdddata.put("sameNameNo", checkStringNull(dataInfo.getString("sameNameNo")))
                                        relateAdddata.put("pcnRightRelate", pcnRightRelateString)
                                        relateAdddata.put("ladWtnOwnerCode", ownerData.getString("ladWtnOwnerCode"))
                                        relateAdddata.put("spotNm", view.ownerRelateBankSpotNm.text.toString())
                                        relateAdddata.put("register",PreferenceUtil.getString(context!!, "id", "defaual"))

                                        relateAddJson.put("addRelate", relateAdddata)
                                        relateAddJson.put("ownerInfo", ownerData)
                                        relateAddJson.put("landInfo", landDataJson)

                                        HttpUtil.getInstance(context!!)
                                            .callUrlJsonWebServer(relateAddJson, progressDialog, addRelateUrl,
                                                object : Callback {
                                                    override fun onFailure(call: Call, e: IOException) {
                                                        progressDialog!!.dismiss()
                                                        logUtil.d("fail")
                                                    }

                                                    override fun onResponse(call: Call, response: Response) {
                                                        val responseString = response.body!!.string()

                                                        logUtil.d("addRelate response --------------> $responseString")

                                                        progressDialog!!.dismiss()

                                                        activity!!.runOnUiThread{
                                                            val dataJsonObject = JSONObject(responseString).getJSONObject("list")

                                                            recyclerViewAdapter.setJSONArray(dataJsonObject.getJSONArray("ownerInfo"))

                                                            recyclerViewAdapter.notifyDataSetChanged()
                                                        }

                                                        ownerRelateSelectDialog.dismiss()
                                                    }

                                                })
                                    }

                                }
                                view.cancelBtn.setOnClickListener {
                                    ownerRelateSelectDialog.dismiss()

                                }


                            }
                        }

                    })
        }


    }

//    override fun onMinusNewOwnerBtnClicked() {
////        TODO("Not yet implemented")
//    }

    override fun onAddNewOwnerBtnClicked() {
//        val dcsnAt = landDataJson!!.getJSONObject("LandInfo").getString("dcsnAt")

        if(dcsnAt == "Y") {
            activity?.runOnUiThread {
                toast.msg_error(R.string.msg_search_dcsc_at_resut, 100)
            }
        } else {
            logUtil.d("onAddOwnerBtnClick >>>>>>>>>>>>>>>>>>>>>>>>>>>")

            val ownerSearch = HashMap<String, String>()
            ownerSearch.put("searchSaupCode",PreferenceUtil.getString(context!!, "saupCode", "defaual"))
            ownerSearch.put("searchName", "")
            ownerSearch.put("searchSameNameNo","")
            ownerSearch.put("searchInhbtntCprNo","")

            val ownerUrl = context!!.getString(R.string.mobile_url) + "ownerInfo"

            HttpUtil.getInstance(context!!)
                .callerUrlInfoPostWebServer(ownerSearch, progressDialog, ownerUrl,
                    object:Callback, AddNewOwnerFragment.addNewOwnerSaveInterface {
                        override fun onFailure(call: Call, e: IOException) {
                            progressDialog!!.dismiss()
                            logUtil.e("fail")
                        }

                        override fun onResponse(call: Call, response: Response) {

                            val responseString = response.body!!.string()
                            logUtil.d("ownerInfo Response -------------------> $responseString")

                            progressDialog!!.dismiss()

                            val ownerInfoJson = JSONObject(responseString).getJSONObject("list").getJSONArray("owner")

                            layoutInflater.inflate(R.layout.fragment_add_owner_dialog, null).let{ view ->

                                adapter = AddOwnerInputAdapter(context!!)
                                activity?.runOnUiThread {

                                    val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
                                    view.searchViewOwner.run {
                                        setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
                                        setIconifiedByDefault(false)
                                        isSubmitButtonEnabled = true
                                        queryHint ="????????? ?????? ??? ???????????? ??????????????????."
                                    }

                                    view.searchViewOwner.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
                                view.searchAddOwnerBtn.text = context!!.getString(R.string.wtnncCommAddOwnerNew)

                                // ?????? ???

                                val ownerInfoDialog = AddOwnerDialogFragment(context!!, activity!!, view).apply {
                                    isCancelable = false
                                    show(fragmentActivity.supportFragmentManager, "ownerInfoDialog")
                                }

                                view.cancelBtn.setOnClickListener {
                                    ownerInfoDialog.dismiss()
                                }

                                view.selectInputBtn.setOnClickListener {

                                    val selectOwnerData = (view.addOwnerListView.adapter as AddOwnerInputAdapter).getSelectItem()

                                    ownerInfoDialog.dismiss()

                                    layoutInflater.inflate(R.layout.fragment_add_select_owner_dialog, null).let{view ->
                                        val ownerOwnerSelectDialog = AddOwnerSelectDialogFragment(
                                            view
                                        ).apply {
                                            isCancelable = false
                                            show(fragmentActivity.supportFragmentManager, "ownerSelectDialog")
                                        }

                                        view.ownerListInfoView.adapter = AddOwnerSelectDialogListAdapter(context!!)
                                        val ownerInfo = landDataJson!!.getJSONArray("ownerInfo") as JSONArray
                                        LandInfoObject.addOwnerListInfo = ownerInfo

                                        for(i in 0 until ownerInfo.length()-1) {
                                            (view.ownerListInfoView.adapter as AddOwnerSelectDialogListAdapter)
                                                .addItem(ownerInfo.getJSONObject(i))
                                        }

                                        view.ownerNoText.text = "??????"
                                        view.ownerDivisionText.text = "?????????"
                                        view.ownerNameText.text = selectOwnerData.indvdlGrpNm

                                        val sameNameNoString = checkStringNull(selectOwnerData.sameNameNo)
                                        if(sameNameNoString.equals("")) {
                                            view.ownerSameNameText.text = "-"
                                        } else {
                                            view.ownerSameNameText.text = sameNameNoString
                                        }


                                        val inhbtntCprNoString = checkStringNull(selectOwnerData.inhbtntCprNo)
                                        if(inhbtntCprNoString == "") {
                                            view.ownerCrpNoText.text = inhbtntCprNoString
                                        } else {
                                            val inhbtntCprNoStringSub = inhbtntCprNoString.substring(0,8)
                                            view.ownerCrpNoText.text = "$inhbtntCprNoStringSub ******"
                                        }

                                        view.ownerDelvyAddrText.text = "${checkStringNull(selectOwnerData.delvyZip)} ${checkStringNull(selectOwnerData.delvyAdres)} ${checkStringNull(selectOwnerData.delvyAdresDetail)}"

                                        if(selectOwnerData.indvdlGrpSe.equals("1")) {
//                                            view.addOwnerGroupBankAt.isEnabled = false
                                            view.addOwnerGeoupBankSpotNm.isEnabled = false
                                        } else {
//                                            view.addOwnerGroupBankAt.isEnabled = true
                                            view.addOwnerGeoupBankSpotNm.isEnabled = true
                                        }

                                        view.cancelBtn.setOnClickListener { ownerOwnerSelectDialog.dismiss() }
                                        view.selectInputBtn.setOnClickListener {
                                            logUtil.d("selectItemData------------------------<>><><><><><><><><")

                                            val posesnQotaString = view.addOwnerPosesnQotaNum.text.toString() +"-"+ view.addOwnerPosesnQotaDeno.text.toString()
                                            val rgistAddrString = view.addOwnerRgistAddrText.text.toString()
                                            val unDcsnOwnarAt = view.addOwnerUnDcsnOwnerAt.isChecked

                                            if(posesnQotaString == "") {
                                                dialogUtil!!.wtnccAlertDialog("""?????? ???????????? ????????? ???????????? ???????????????.""".trimMargin(), builder!!, "????????? ??????").show()

                                            } else if(rgistAddrString == "") {
                                                dialogUtil!!.wtnccAlertDialog("""?????? ???????????? ????????? ???????????? ???????????????.""".trimMargin(), builder!!, "????????? ??????").show()

                                            } else {
                                                val recentOwnerInfo = LandInfoObject.addOwnerListInfo
                                                val addOwnerUrl = context!!.getString(R.string.mobile_url) + "addLandOwner"

                                                val addOwnerJson = JSONObject()
                                                val requestJson = JSONObject()

                                                addOwnerJson.put("delvyAdres", selectOwnerData.delvyAdres)
                                                addOwnerJson.put("delvyAdresDetail", selectOwnerData.delvyAdresDetail)
                                                addOwnerJson.put("delvyZip", selectOwnerData.delvyZip)
                                                addOwnerJson.put("indvdlGrpCode", selectOwnerData.indvdlGrpCode)
                                                addOwnerJson.put("indvdlGrpSe", selectOwnerData.indvdlGrpSe)
                                                addOwnerJson.put("indvdlGrpNm", selectOwnerData.indvdlGrpNm)
                                                addOwnerJson.put("posesnQota",posesnQotaString)
                                                if(unDcsnOwnarAt) {
                                                    addOwnerJson.put("unDcsnOwnerAt","Y")
                                                } else {
                                                    addOwnerJson.put("unDcsnOwnerAt","N")
                                                }
                                                addOwnerJson.put("rgistAdres",rgistAddrString)
                                                addOwnerJson.put("register",PreferenceUtil.getString(context!!, "id", "defaual"))
                                                addOwnerJson.put("hapyuGroupCode","")
                                                addOwnerJson.put("hapyuAt","")
                                                addOwnerJson.put("qotaAr","")
                                                addOwnerJson.put("plotCode","")

                                                requestJson.put("addOwner", addOwnerJson)
                                                requestJson.put("recentOwner", recentOwnerInfo)
                                                requestJson.put("landInfo", landDataJson)

                                                logUtil.d("requestJson ->>>>>>>>>>>>>>>>>>>>>>>>>>>>>: ${requestJson.toString()}")

                                                HttpUtil.getInstance(context!!)
                                                    .callUrlJsonWebServer(requestJson, progressDialog, addOwnerUrl,
                                                        object: Callback {
                                                            override fun onFailure(call: Call, e: IOException) {
                                                                progressDialog!!.dismiss()
                                                                logUtil.d("fail")
                                                            }

                                                            override fun onResponse(call: Call, response: Response) {
                                                                val responseString = response.body!!.string()

                                                                logUtil.d("addOwner response ------------------> $responseString")

                                                                progressDialog!!.dismiss()

//                                                            val newOwnerData = JSONArray(responseString)

                                                                activity!!.runOnUiThread{
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


                                }
                                view.searchAddOwnerBtn.setOnClickListener {



                                    logUtil.d("searchAddOwnerBtn <><><><><><><><><><><><><><><><><><><><><><><><><>")

                                    AddNewOwnerFragment(activity!!, context!!, this).show((context as MapActivity).supportFragmentManager, "addNewOwnerFragment")
                                    ownerInfoDialog.dismiss()
                                }
                            }

                        }

                        override fun onSaveOwner(dataInfo: JSONObject, ownerType: Int) {

                            progressDialog!!.dismiss()
                            logUtil.d("12312312312312312312312312312312312312")


                            layoutInflater.inflate(R.layout.fragment_add_select_owner_dialog, null).let{view ->
                                val ownerOwnerSelectDialog = AddOwnerSelectDialogFragment(view).apply {
                                    isCancelable = false
                                    show(fragmentActivity.supportFragmentManager, "ownerSelectDialog")
                                }

                                view.ownerListInfoView.adapter = AddOwnerSelectDialogListAdapter(context!!)
                                val ownerInfo = landDataJson!!.getJSONArray("ownerInfo") as JSONArray
                                LandInfoObject.addOwnerListInfo = ownerInfo

                                for(i in 0 until ownerInfo.length()-1) {
                                    (view.ownerListInfoView.adapter as AddOwnerSelectDialogListAdapter).addItem(ownerInfo.getJSONObject(i))
                                }

                                view.ownerNoText.text = "??????"
                                view.ownerDivisionText.text = "?????????"
                                if(ownerType == 1) {
                                    view.ownerNameText.text = dataInfo.getString("name")
                                } else {
                                    view.ownerNameText.text = dataInfo.getString("grpNm")
                                }


                                val sameNameNoString = checkStringNull(dataInfo.getString("sameNameNo"))
                                if(sameNameNoString.equals("0")) {
                                    view.ownerSameNameText.text = "-"
                                } else {
                                    view.ownerSameNameText.text = sameNameNoString
                                }


//                            val inhbtntCprNoString = checkStringNull(dataInfo.getString("ihidnum"))
//                            if(inhbtntCprNoString == "") {
//                                view.ownerCrpNoText.text = inhbtntCprNoString
//                            } else {
//                                val inhbtntCprNoStringSub = inhbtntCprNoString.substring(0,8)
//                                view.ownerCrpNoText.text = "$inhbtntCprNoStringSub ******"
//                            }
                                if(ownerType == 1) {
                                    val inhbtntCprNoString = checkStringNull(dataInfo.getString("ihidnum"))
                                    if (inhbtntCprNoString == "") {
                                        view.ownerCrpNoText.text = inhbtntCprNoString
                                    } else {
                                        //val inhbtntCprNoStringSub = inhbtntCprNoString.substring(0, 8)
                                        //view.ownerCrpNoText.text = "$inhbtntCprNoStringSub ******"
                                        view.ownerCrpNoText.text = withIhidNumAsterRisk(inhbtntCprNoString)
                                    }
//                                view.addOwnerbaknAtLinear.setE
//                                view.addOwnerUnDcsnOwnerAt.isEnabled = false
//                                view.addOwnerBankAt.isEnabled = false
//                                    view.addOwnerGroupBankAt.isEnabled = false
                                    view.addOwnerGeoupBankSpotNm.isEnabled = false
                                } else {

                                    val inhbtntCprNoString = checkStringNull(dataInfo.getString("jurirno"))
                                    if (inhbtntCprNoString == "") {
                                        view.ownerCrpNoText.text = inhbtntCprNoString
                                    } else {
                                        //val inhbtntCprNoStringSub = inhbtntCprNoString.substring(0, 8)
                                        //view.ownerCrpNoText.text = "$inhbtntCprNoStringSub ******"
                                        view.ownerCrpNoText.text = withIhidNumAsterRisk(inhbtntCprNoString)
                                    }
//                                    view.addOwnerGroupBankAt.isEnabled = true
                                    view.addOwnerGeoupBankSpotNm.isEnabled = true
                                }


                                view.ownerDelvyAddrText.text = "${checkStringNull(dataInfo.getString("delvyZip"))} ${checkStringNull(dataInfo.getString("delvyAdres"))} ${checkStringNull(dataInfo.getString("delvyAdresDetail"))}"

                                view.cancelBtn.setOnClickListener { ownerOwnerSelectDialog.dismiss() }
                                view.selectInputBtn.setOnClickListener {
                                    logUtil.d("selectItemData------------------------<>><><><><><><><><")

                                    val posesnQotaString = view.addOwnerPosesnQotaNum.text.toString() +"-"+ view.addOwnerPosesnQotaDeno.text.toString()
                                    val rgistAddrString = view.addOwnerRgistAddrText.text.toString()
                                    val unDcsnOwnarAt = view.addOwnerUnDcsnOwnerAt.isChecked

                                    if(posesnQotaString == "") {
                                        dialogUtil!!.wtnccAlertDialog("""?????? ???????????? ????????? ???????????? ???????????????.""".trimMargin(), builder!!, "????????? ??????").show()

                                    } else if(rgistAddrString == "") {
                                        dialogUtil!!.wtnccAlertDialog("""????????? ????????? ?????? ?????? ???????????????.""".trimMargin(), builder!!, "????????? ??????").show()

                                    } else {
                                        val recentOwnerInfo = LandInfoObject.addOwnerListInfo
                                        val addOwnerUrl = context!!.getString(R.string.mobile_url) + "addLandOwner"

                                        val addOwnerJson = JSONObject()
                                        val requestJson = JSONObject()

                                        addOwnerJson.put("delvyAdres", checkStringNull(dataInfo.getString("delvyAdres")))
                                        addOwnerJson.put("delvyAdresDetail", checkStringNull(dataInfo.getString("delvyAdresDetail")))
                                        addOwnerJson.put("delvyZip", checkStringNull(dataInfo.getString("delvyZip")))
                                        if(ownerType == 1) {
                                            addOwnerJson.put("indvdlGrpCode", checkStringNull(dataInfo.getString("onivCode")))
                                            addOwnerJson.put("indvdlGrpSe", "1")
                                            addOwnerJson.put("indvdlGrpNm", checkStringNull(dataInfo.getString("name")))
                                        } else {
                                            addOwnerJson.put("indvdlGrpCode", checkStringNull(dataInfo.getString("grpEntrpsCode")))
                                            addOwnerJson.put("indvdlGrpSe","2")
                                            addOwnerJson.put("indvdlGrpNm", checkStringNull(dataInfo.getString("grpNm")))
                                        }

                                        addOwnerJson.put("posesnQota",posesnQotaString)
                                        if(unDcsnOwnarAt) {
                                            addOwnerJson.put("unDcsnOwnerAt","Y")
                                        } else {
                                            addOwnerJson.put("unDcsnOwnerAt","N")
                                        }
                                        addOwnerJson.put("rgistAdres",rgistAddrString)
                                        addOwnerJson.put("register",PreferenceUtil.getString(context!!, "id", "defaual"))
                                        addOwnerJson.put("hapyuGroupCode","")
                                        addOwnerJson.put("hapyuAt","")
                                        addOwnerJson.put("qotaAr","")
                                        addOwnerJson.put("plotCode","")

                                        requestJson.put("addOwner", addOwnerJson)
                                        requestJson.put("recentOwner", recentOwnerInfo)
                                        requestJson.put("landInfo", landDataJson)

                                        logUtil.d("requestJson ->>>>>>>>>>>>>>>>>>>>>>>>>>>>>: ${requestJson.toString()}")

                                        HttpUtil.getInstance(context!!)
                                            .callUrlJsonWebServer(requestJson, progressDialog, addOwnerUrl,
                                                object: Callback {
                                                    override fun onFailure(call: Call, e: IOException) {
                                                        progressDialog!!.dismiss()
                                                        logUtil.d("fail")
                                                    }

                                                    override fun onResponse(call: Call, response: Response) {
                                                        val responseString = response.body!!.string()

                                                        logUtil.d("addOwner response ------------------> $responseString")

                                                        progressDialog!!.dismiss()

//                                                            val newOwnerData = JSONArray(responseString)

                                                        activity!!.runOnUiThread{
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

                        }

                    })
        }


    }

   

}