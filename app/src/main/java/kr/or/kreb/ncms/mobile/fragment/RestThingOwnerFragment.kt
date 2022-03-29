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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_add_choice_owner_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_owner_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_owner_dialog.view.cancelBtn
import kotlinx.android.synthetic.main.fragment_add_owner_dialog.view.selectInputBtn
import kotlinx.android.synthetic.main.fragment_add_owner_relate.*
import kotlinx.android.synthetic.main.fragment_add_owner_relate.view.*
import kotlinx.android.synthetic.main.fragment_add_select_owner_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_select_owner_dialog.view.addOwnerRgistAddrText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerCrpNoText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerDelvyAddrText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerDivisionText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerNameText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerSameNameText
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.*
import kr.or.kreb.ncms.mobile.data.ThingWtnObject
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.util.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class RestThingOwnerFragment(val fragmentActivity: FragmentActivity) : Fragment(),
        BaseOwnerRecyclerViewAdapter.onItemClickDelvyAddrBtnListener,
        BaseOwnerRecyclerViewAdapter.onItemClickaddRelateBtnListener,
        BaseOwnerRecyclerViewAdapter.onItemClickaddOwnerBtnListener,
        NewOwnerRecyclerViewAdapter.onItemClickAddOwnerBtnListener,
        NewOwnerRecyclerViewAdapter.onItemClickAddOwnerViewListener,
        DialogUtil.ClickListener
{

//    private lateinit var restThingRecyclerViewAdapter: RestThingOwnerRecyclerViewAdapter
//    private lateinit var restThingNewOnwerRecyclerViewAdapter: RestThingNewOwnerRecyclerViewAdapter
    private lateinit var restThingRecyclerViewAdapter: OwnerRecyclerViewAdapter
    private lateinit var restThingNewOnwerRecyclerViewAdapter: NewOwnerRecyclerViewAdapter
    private var logUtil: LogUtil = LogUtil("RestThingOwnerFragment")
    private var progressDialog: AlertDialog? = null
    var builder: MaterialAlertDialogBuilder? = null
    var dialogUtil: DialogUtil? = null
    var restThingDataJson: JSONObject? = null
    var restThingOwnerInfoJson: JSONArray? = null

    private lateinit var adapter: AddOwnerInputAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_add_owner_relate, null)
        dialogUtil = DialogUtil(context, activity)
        builder = context?.let { MaterialAlertDialogBuilder(it) }!!
        dialogUtil!!.setClickListener(this)
        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))

        init(view)

        return view
    }

    fun init(view: View) {

        var dataString = requireActivity().intent!!.extras!!.get("ThingInfo") as String

        restThingDataJson = JSONObject(dataString!!)

        var dataJson = JSONObject(dataString)

        restThingOwnerInfoJson = dataJson.getJSONArray("ownerInfo") as JSONArray

        view.ownerRecyclerView.visibleView()
        view.newOwnerRecyclerView.goneView()
        restThingRecyclerViewAdapter = OwnerRecyclerViewAdapter(context!!, BizEnum.REST_THING, restThingOwnerInfoJson!!, this, this, this)
        view.ownerRecyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        view.ownerRecyclerView.adapter = restThingRecyclerViewAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDelvyAddrClick(data: JSONObject) {
        logUtil.d("onDelvyAddrClick data >>>>>>>>>>>>>>>>>>>>> $data")
    }

    override fun onAddRelateBtnClick(data: JSONObject) {
        logUtil.d("onAddRelateBtnClick >>>>>>>>>>>>> $data")

        val ownerData = data

        val ownerSearch = HashMap<String, String>()
        ownerSearch.put("searchSaupCode", PreferenceUtil.getString(context!!, "saupCode", "defaual"))

        val ownerUrl = context!!.getString(R.string.mobile_url) + "ownerInfo"

        HttpUtil.getInstance(context!!)
            .callerUrlInfoPostWebServer(ownerSearch, progressDialog, ownerUrl,
                object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        progressDialog!!.dismiss()
                        logUtil.e("fail")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseString = response.body!!.string()

                        logUtil.d("ownerInfore response -------------------> $responseString")

                        val ownerInfoJson = JSONObject(responseString).getJSONObject("list").getJSONArray("owner")

                        layoutInflater.inflate(R.layout.fragment_add_owner_dialog, null).let{ view ->
                            view.addOwnerTitleText.text = context!!.getString(R.string.wtnncCommAddRelate)

                            adapter = AddOwnerInputAdapter(context!!)

                            activity?.runOnUiThread {

                                val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
                                view.searchViewOwner.run {
                                    setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
                                    setIconifiedByDefault(false)
                                    isSubmitButtonEnabled = true
                                    queryHint ="관계자 성명 및 기관명을 입력해주세요."
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

                            val ownerInfoDialog = AddOwnerDialogFragment(context!!, activity!!, view).apply{
                                isCancelable = false
                                show(fragmentActivity.supportFragmentManager, "ownerInfoDialog")
                            }
                            view.cancelBtn.setOnClickListener {
                                ownerInfoDialog.dismiss()
                            }

                            view.selectInputBtn.setOnClickListener {
                                val selectOwnerdata = adapter.getSelectItem()

                                ownerInfoDialog.dismiss()

                                layoutInflater.inflate(R.layout.fragment_add_select_relate_dialog, null).let { view ->
                                    val ownerRelateSelectDialog = AddOwnerSelectDialogFragment(view).apply {
                                        isCancelable = false
                                        show(fragmentActivity.supportFragmentManager, "ownerRelateSelectDialog")
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
                                        logUtil.d("selectItemData size ------------------>${selectOwnerdata.toString()}")

                                        val pcnRightRelateString = view.ownerPcnRightRelate.text.toString()

                                        if(pcnRightRelateString == "") {
                                            dialogUtil!!.wtnccAlertDialog("""관계인 권리관례가 입력되지 않았습니다.""".trimMargin(), builder!!, "관계자추가").show()

                                        } else {
                                            val addRelateUrl = context!!.resources.getString(R.string.mobile_url) + "addThingRelate"

                                            val relateAddJson = JSONObject()
                                            val relateAddData = JSONObject()

                                            relateAddData.put("delvyAdres", selectOwnerdata.delvyAdres)
                                            relateAddData.put("delvyAdresDetail", selectOwnerdata.delvyAdresDetail)
                                            relateAddData.put("delvyZip", selectOwnerdata.delvyZip)
                                            relateAddData.put("indvdlGrpCode", selectOwnerdata.indvdlGrpCode)
                                            relateAddData.put("indvdlGrpNm", selectOwnerdata.indvdlGrpNm)
                                            relateAddData.put("indvdlGrpSe", selectOwnerdata.indvdlGrpSe)
                                            relateAddData.put("indvdlGrpSeNm", selectOwnerdata.indvdlGrpSeNm)
                                            relateAddData.put("inhbtntCprNo", selectOwnerdata.inhbtntCprNo)
                                            relateAddData.put("sameNameNo", selectOwnerdata.sameNameNo)
                                            relateAddData.put("pcnRightRelate", pcnRightRelateString)
                                            val restThingData = restThingDataJson!!.getJSONObject("ThingSearch").getString("thingSmallCl")
                                            relateAddData.put("thingCl", when(restThingData) {
                                                "A023001","A023004","A023006","A023007", "A023008","A023009","A023010","A023011" -> "A106005" //지장물
                                                "A023005" ->"A106009" //수목
                                                "A023003", "A023002" -> "A106010" //건축물
                                                else -> ""
                                            })
                                            relateAddData.put("register",PreferenceUtil.getString(context!!, "id", "defaual"))

                                            relateAddJson.put("addRelate", relateAddData)
                                            relateAddJson.put("ownerInfo", ownerData)
                                            relateAddJson.put("ThingSearch", restThingDataJson)

                                            HttpUtil.getInstance(context!!)
                                                .callUrlJsonWebServer(relateAddJson, progressDialog, addRelateUrl,
                                                    object: Callback {
                                                        override fun onFailure(call: Call, e: IOException) {
                                                            progressDialog!!.dismiss()
                                                            logUtil.d("fail")

                                                        }

                                                        override fun onResponse(call: Call, response: Response) {

                                                            val responseString = response.body!!.string()

                                                            logUtil.d("addRelate response ---------------> $responseString")

                                                            progressDialog!!.dismiss()

                                                            activity!!.runOnUiThread {
                                                                val dataJsonObject = JSONObject(responseString).getJSONObject("list")

                                                                restThingRecyclerViewAdapter.setJSONArray(dataJsonObject.getJSONArray("ownerInfo"))

                                                                restThingRecyclerViewAdapter.notifyDataSetChanged()
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

    override fun onAddOwnerBtnClick() {
        logUtil.d("onAddOwnerBtnClick >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")

        val ownerSearch = HashMap<String, String>()

        ownerSearch.put("searchSaupCode",PreferenceUtil.getString(context!!, "saupCode", "defaual"))
        ownerSearch.put("searchName", "")
        ownerSearch.put("searchSameNameNo","")
        ownerSearch.put("searchInhbtntCprNo","")

        val ownerUrl = context!!.getString(R.string.mobile_url) + "ownerInfo"

        HttpUtil.getInstance(context!!)
            .callerUrlInfoPostWebServer(ownerSearch, progressDialog, ownerUrl,
                object: Callback, AddNewOwnerFragment.addNewOwnerSaveInterface {
                    override fun onFailure(call: Call, e: IOException) {
                        progressDialog!!.dismiss()
                        logUtil.e("fail")
                    }


                    override fun onResponse(call: Call, response: Response) {

                        val responseString = response.body!!.string()
                        logUtil.d("ownerInfo Response ----------------------> $responseString")

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
                                    queryHint ="소유자 성명 및 기관명을 입력해주세요."
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

                            view.searchAddOwnerBtn.setOnClickListener {

                                AddNewOwnerFragment(activity!!, context!!, this).show((context as MapActivity).supportFragmentManager, "addNewOwnerFragment")
                                ownerInfoDialog.dismiss()
                            }
                        }
                    }
                    override fun onSaveOwner(dataInfo: JSONObject, grpSe: Int) {

                        progressDialog!!.dismiss()

                        layoutInflater.inflate(R.layout.fragment_add_select_owner_dialog, null)
                            .let { view ->
                                val ownerOwnerSelectDialog = AddOwnerSelectDialogFragment(view).apply {
                                    isCancelable = false
                                    show(fragmentActivity.supportFragmentManager, "ownerSelectDialog")
                                }

                                view.ownerListInfoView.adapter = AddOwnerSelectDialogListAdapter(context!!)
                                val ownerInfo = restThingDataJson!!.getJSONArray("ownerInfo") as JSONArray
                                ThingWtnObject.addOwnerListInfo = ownerInfo

                                for (i in 0 until ownerInfo.length() - 1) {
                                    (view.ownerListInfoView.adapter as AddOwnerSelectDialogListAdapter).addItem(
                                        ownerInfo.getJSONObject(i)
                                    )
                                }

                                view.ownerNoText.text = "추가"
                                view.ownerDivisionText.text = "소유자"

                                if(grpSe == 1) {
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

                                if(grpSe == 1) {
                                    val inhbtntCprNoString = checkStringNull(dataInfo.getString("ihidnum"))
                                    if (inhbtntCprNoString == ""|| inhbtntCprNoString == "-") {
                                        view.ownerCrpNoText.text = inhbtntCprNoString
                                    } else {
                                        //val inhbtntCprNoStringSub = inhbtntCprNoString.substring(0, 8)
                                        //view.ownerCrpNoText.text = "$inhbtntCprNoStringSub ******"
                                        view.ownerCrpNoText.text = withIhidNumAsterRisk(inhbtntCprNoString)
                                    }
                                } else {

                                    val inhbtntCprNoString = checkStringNull(dataInfo.getString("jurirno"))
                                    if (inhbtntCprNoString == ""|| inhbtntCprNoString == "-") {
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

                                    logUtil.d("selectItemData ------------------->>>>>>>>>>>>>>>>>>>>>>>>>> ")

                                    val posesnQotaString = view.addOwnerPosesnQotaNum.text.toString() +"-"+ view.addOwnerPosesnQotaDeno.text.toString()
                                    val rgistAddrString = view.addOwnerRgistAddrText.text.toString()
                                    val unDcsnOwnarAt = view.addOwnerUnDcsnOwnerAt.isChecked

                                    if (posesnQotaString == "") {
                                        dialogUtil!!.wtnccAlertDialog(
                                            """추가 소유자의 지분이 입력되지 않았습니다.""".trimMargin(),
                                            builder!!,
                                            "소유자 추가"
                                        ).show()

                                    } else if (rgistAddrString == "") {
                                        dialogUtil!!.wtnccAlertDialog(
                                            """공부상 주소가 입력 되지 않았습니다.""".trimMargin(),
                                            builder!!,
                                            "공부상 주소"
                                        ).show()

                                    } else {
                                        val recentOwnerInfo = ThingWtnObject.addOwnerListInfo
                                        val addOwnerUrl =
                                            context!!.getString(R.string.mobile_url) + "addThingOwner"

                                        val addOwnerJson = JSONObject()
                                        val addRequestJson = JSONObject()

                                        addOwnerJson.put("delvyAdres",checkStringNull( dataInfo.getString("delvyAdres")))
                                        addOwnerJson.put(
                                            "delvyAdresDetail",
                                            checkStringNull(dataInfo.getString("delvyAdresDetail"))
                                        )
                                        addOwnerJson.put("delvyZip", checkStringNull(dataInfo.getString("delvyZip")))


                                        if(grpSe == 1) {
                                            addOwnerJson.put("indvdlGrpCode", checkStringNull(dataInfo.getString("onivCode")))
                                            addOwnerJson.put("indvdlGrpSe", "1")
                                            addOwnerJson.put("indvdlGrpNm", checkStringNull(dataInfo.getString("name")))
                                        } else {
                                            addOwnerJson.put("indvdlGrpCode", checkStringNull(dataInfo.getString("grpEntrpsCode")))
                                            addOwnerJson.put("indvdlGrpSe","2")
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
                                        addOwnerJson.put("plotCode", "")
                                        val thingData = restThingDataJson!!.getJSONObject("ThingSearch")
                                            .getString("thingSmallCl")
                                        addOwnerJson.put(
                                            "thingCl", when (thingData) {
                                                "A023001", "A023004", "A023006", "A023007", "A023008", "A023009", "A023010", "A023011" -> "A106005" //지장물
                                                "A023005" -> "A106009" //수목
                                                "A023003", "A023002" -> "A106010" //건축물
                                                else -> ""
                                            }
                                        )

                                        addRequestJson.put("addOwner", addOwnerJson)
                                        addRequestJson.put("recentOwner", recentOwnerInfo)
                                        addRequestJson.put("ThingSearch", restThingDataJson)

                                        HttpUtil.getInstance(context!!)
                                            .callUrlJsonWebServer(addRequestJson,
                                                progressDialog,
                                                addOwnerUrl,
                                                object : Callback {
                                                    override fun onFailure(call: Call, e: IOException) {
                                                        progressDialog!!.dismiss()
                                                        logUtil.e("fail")
                                                    }

                                                    override fun onResponse(
                                                        call: Call,
                                                        response: Response
                                                    ) {
                                                        val responseString = response.body!!.string()

                                                        logUtil.d("addOwner response ------------------> $responseString")

                                                        progressDialog!!.dismiss()

                                                        activity!!.runOnUiThread {

                                                            val dataJsonObject = JSONObject(responseString).getJSONObject("list")

                                                            restThingRecyclerViewAdapter.setJSONArray(
                                                                dataJsonObject.getJSONArray("ownerInfo")
                                                            )

                                                            restThingRecyclerViewAdapter.notifyDataSetChanged()
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

    fun getOwnerData() {
        val tempInt = restThingNewOnwerRecyclerViewAdapter.getItemCount()
        logUtil.d("tempInt ------------------------> $tempInt")
    }

    fun checkStringNull(nullString: String): String = if (nullString == "null") "" else { nullString }

    override fun onPositiveClickListener(dialog: DialogInterface, type: String) {
        val choiceOwnerInfoUrl = context!!.resources.getString(R.string.mobile_url) + "choiceThingOwnerInfo"
        val choiceData = HashMap<String, String>()

        val ladData = restThingDataJson!!.getJSONObject("ThingSearch")
        choiceData.put("saupCode",ladData.getString("saupCode"))
        choiceData.put("incrprLnm",ladData.getString("incrprLnm"))

        HttpUtil.getInstance(context!!)
            .callerUrlInfoPostWebServer(choiceData, progressDialog, choiceOwnerInfoUrl,
                object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        logUtil.d("fail")
                        progressDialog?.dismiss()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseString = response.body!!.string()

                        logUtil.d("choiceOwnerInfo Response --------------> $responseString")

                        progressDialog?.dismiss()

                        val ownerData = JSONObject(responseString).getJSONObject("list") as JSONObject

                        val ownerInfo = ownerData.getJSONArray("ownerInfo")
                        logUtil.d("thingOwner -------------------------> ${ownerInfo.toString()}")

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
                                    newOwnerAdapterCall(restThingOwnerInfoJson!!)
                                }
                                view.selectInputBtn.setOnClickListener {

                                    val selectOwnerChoiceData =
                                        (view.addChoiceOwnerListView.adapter as AddChoiceOwnerAdapter).getSelectItem()

                                    if (selectOwnerChoiceData!!.size > 0) {

                                        var selectOwnerArray = JSONArray()
                                        var tempJson = JSONObject()
                                        tempJson.put("ownerNm", "")
                                        tempJson.put("sameNameNo", "")
                                        tempJson.put("delvyAdres", "")
                                        tempJson.put("delvyZip", "")
                                        tempJson.put("delvyAdresDetail","")
                                        tempJson.put("ihidnum", "")
                                        tempJson.put("posesnSe", "")
                                        tempJson.put("rgistAdres", "")
                                        tempJson.put("posesnQota", "")
                                        tempJson.put("unDcsnOwnerAt", "N")
                                        tempJson.put("indvdlGrpCode", "")
                                        tempJson.put("hapyuAt","")
                                        tempJson.put("hapyuGroupCode","")
                                        tempJson.put("qotaAr","")
                                        tempJson.put("thingCl","")
                                        tempJson.put("relate", "")

                                        selectOwnerArray.put(tempJson)

                                        for (i in 0 until selectOwnerChoiceData.size) {
                                            var selectOwnerJson = JSONObject()
                                            selectOwnerJson.put("ownerNm", selectOwnerChoiceData.get(i).indvdlGrpNm)
                                            selectOwnerJson.put("sameNameNo", selectOwnerChoiceData.get(i).sameNameNo)
                                            selectOwnerJson.put("delvyAdres", selectOwnerChoiceData.get(i).delvyAdres)
                                            selectOwnerJson.put("delvyZip", selectOwnerChoiceData.get(i).delvyZip)
                                            selectOwnerJson.put("delvyAdresDetail",selectOwnerChoiceData.get(i).delvyAdresDetail)
                                            selectOwnerJson.put("ihidnum", selectOwnerChoiceData.get(i).inhbtntCprNo)
                                            selectOwnerJson.put("posesnSe", selectOwnerChoiceData.get(i).indvdlGrpSe)
                                            selectOwnerJson.put("rgistAdres", selectOwnerChoiceData.get(i).rgistAdres)
                                            selectOwnerJson.put("posesnQota", "")
                                            selectOwnerJson.put("unDcsnOwnerAt", "N")
                                            selectOwnerJson.put("indvdlGrpCode", selectOwnerChoiceData.get(i).indvdlGrpCode)
                                            selectOwnerJson.put("hapyuAt","")
                                            selectOwnerJson.put("hapyuGroupCode","")
                                            selectOwnerJson.put("qotaAr","")
                                            selectOwnerJson.put("thingCl","")
                                            selectOwnerJson.put("relate", "")

                                            selectOwnerArray.put(selectOwnerJson)
                                        }

                                        ownerChoiceSelectDialog.dismiss()
                                        newOwnerAdapterCall(selectOwnerArray)


                                    } else {
                                        newOwnerAdapterCall(restThingOwnerInfoJson!!)
                                    }

                                }
                            }



                        }
                    }

                })


    }

    override fun onNegativeClickListener(dialog: DialogInterface, type: String) {
        newOwnerAdapterCall(restThingOwnerInfoJson!!)
    }


    fun newOwnerAdapterCall(array: JSONArray) {
        ThingWtnObject.thingOwnerInfoJson = array
        restThingNewOnwerRecyclerViewAdapter = NewOwnerRecyclerViewAdapter(context!!, array, this, this)
        newOwnerRecyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        newOwnerRecyclerView.adapter = restThingNewOnwerRecyclerViewAdapter
    }

    override fun onAddNewOwnerBtnClick() {
    }

    override fun onAddNewOnwerViewClick(position: Int) {
    }

}