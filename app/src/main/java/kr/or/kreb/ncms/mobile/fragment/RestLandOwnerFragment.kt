/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.app.SearchManager
import android.content.Context
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
import kotlinx.android.synthetic.main.fragment_add_select_owner_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerCrpNoText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerDelvyAddrText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerDivisionText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerNameText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerSameNameText
import kotlinx.android.synthetic.main.fragment_restland_owner.view.*
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.*
import kr.or.kreb.ncms.mobile.adapter.BaseOwnerRecyclerViewAdapter
import kr.or.kreb.ncms.mobile.adapter.OwnerRecyclerViewAdapter
import kr.or.kreb.ncms.mobile.base.BaseFragment
import kr.or.kreb.ncms.mobile.data.LandInfoObject
import kr.or.kreb.ncms.mobile.data.RestLandInfoObject
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.util.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class RestLandOwnerFragment(val fragmentActivity: FragmentActivity) : BaseFragment(),
    BaseOwnerRecyclerViewAdapter.OnOwnerEventListener {

//    private lateinit var recyclerViewAdapter: RestLandOwnerRecyclerViewAdapter
//    private lateinit var recyclerViewAdapter: OwnerRecyclerViewAdapter
//
//    private var logUtil: LogUtil = LogUtil("RestLandOwnerFragment")
//    private var progressDialog: AlertDialog? = null
//    var builder: MaterialAlertDialogBuilder? = null
//    var dialogUtil: DialogUtil? = null
    var landDataJson: JSONObject? = null

    private lateinit var adapter: AddOwnerInputAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_restland_owner, null)

        dialogUtil = DialogUtil(context, activity)
        builder = context?.let { MaterialAlertDialogBuilder(it) }!!
        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))

        init(view)

        return view
    }

    fun init(view: View) {

        var dataString = requireActivity().intent!!.extras!!.get("LandInfo") as String?

        landDataJson = JSONObject(dataString)

        val landOwnerInfoJson = landDataJson!!.getJSONArray("ownerInfo") as JSONArray

        RestLandInfoObject.landOwnerInfoJson = landOwnerInfoJson

        recyclerViewAdapter = OwnerRecyclerViewAdapter(
            context!!,
            BizEnum.REST_LAD,
            landOwnerInfoJson,
            this
        )
        view.ownerRecyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        view.ownerRecyclerView.adapter = recyclerViewAdapter

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDelvyAddrClicked(data: JSONObject) {

    }

    override fun onAddRelateBtnClicked(data: JSONObject) {

        logUtil.d("onAddRelateBtnClick >>>>>>>>>>>>>>>>>>>>>>>> $data")

        val ownerData = data

        val ownerSearch = HashMap<String, String>()
        ownerSearch.put("searchSaupCode",PreferenceUtil.getString(context!!, "bizCode", "defaual"))
        ownerSearch.put("searchName", "")
        ownerSearch.put("searchSameNameNo","")
        ownerSearch.put("searchInhbtntCprNo","")

        val ownerUrl = context!!.getString(R.string.mobile_url) + "ownerInfo"

        HttpUtil.getInstance(context!!)
            .callerUrlInfoPostWebServer(ownerSearch, progressDialog, ownerUrl,
                object:Callback {
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

                            // TODO: 2021-11-04 기존 리스트뷰 -> 리사이클러뷰 (+ 검색필터기능 추가)
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
                            view.addOwnerTitleText.text = context!!.getString(R.string.wtnncCommAddRelate)
                            view.searchAddOwnerBtn.text = context!!.getString(R.string.wtnncCommAddRelateNew)

                            // 추가라인 끝

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
                                            dialogUtil!!.wtnccAlertDialog("""관계인 권리관례가 입력되지 않았습니다.""".trimMargin(), builder!!, "관계자추가").show()

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

                        }

                    }

                })

    }

//    override fun onMinusNewOwnerBtnClicked() {
//    }

    override fun onAddNewOwnerBtnClicked() {
        logUtil.d("onAddOwnerBtnClick >>>>>>>>>>>>>>>>>>>>>>>>>>>")

        val ownerSearch = HashMap<String, String>()
        ownerSearch.put("searchSaupCode",PreferenceUtil.getString(context!!, "bizCode", "defaual"))
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
                                    queryHint ="소유자 성명 및 기관명을 입력해주세요."
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

                            // 추가 끝

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

                                    view.ownerNoText.text = "추가"
                                    view.ownerDivisionText.text = "소유자"
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
                                        //val inhbtntCprNoStringSub = inhbtntCprNoString.substring(0,8)
                                        val inhbtntCprNoStringSub = withIhidNumAsterRisk(inhbtntCprNoString)
                                        view.ownerCrpNoText.text = inhbtntCprNoStringSub
                                    }

                                    view.ownerDelvyAddrText.text = "${checkStringNull(selectOwnerData.delvyZip)} ${checkStringNull(selectOwnerData.delvyAdres)} ${checkStringNull(selectOwnerData.delvyAdresDetail)}"

                                    view.cancelBtn.setOnClickListener { ownerOwnerSelectDialog.dismiss() }
                                    view.selectInputBtn.setOnClickListener {
                                        logUtil.d("selectItemData------------------------<>><><><><><><><><")

                                        val posesnQotaString = view.addOwnerPosesnQotaNum.text.toString() +"-"+ view.addOwnerPosesnQotaDeno.text.toString()
                                        val rgistAddrString = view.addOwnerRgistAddrText.text.toString()
                                        val unDcsnOwnarAt = view.addOwnerUnDcsnOwnerAt.isChecked

                                        if(posesnQotaString == "") {
                                            dialogUtil!!.wtnccAlertDialog("""추가 소유자의 지분이 입력되지 않았습니다.""".trimMargin(), builder!!, "소유자 추가").show()

                                        } else if(rgistAddrString == "") {
                                            dialogUtil!!.wtnccAlertDialog("""추가 소유자의 지분이 입력되지 않았습니다.""".trimMargin(), builder!!, "소유자 추가").show()

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

                            view.ownerNoText.text = "추가"
                            view.ownerDivisionText.text = "소유자"
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
                            } else {

                                val inhbtntCprNoString = checkStringNull(dataInfo.getString("jurirno"))
                                if (inhbtntCprNoString == "") {
                                    view.ownerCrpNoText.text = inhbtntCprNoString
                                } else {
                                    //val inhbtntCprNoStringSub = inhbtntCprNoString.substring(0, 8)
                                    //view.ownerCrpNoText.text = "$inhbtntCprNoStringSub ******"
                                    view.ownerCrpNoText.text = withIhidNumAsterRisk(inhbtntCprNoString)
                                }
                            }


                            view.ownerDelvyAddrText.text = "${checkStringNull(dataInfo.getString("delvyZip"))} ${checkStringNull(dataInfo.getString("delvyAdres"))} ${checkStringNull(dataInfo.getString("delvyAdresDetail"))}"

                            view.cancelBtn.setOnClickListener { ownerOwnerSelectDialog.dismiss() }
                            view.selectInputBtn.setOnClickListener {
                                logUtil.d("selectItemData------------------------<>><><><><><><><><")

                                val posesnQotaString = view.addOwnerPosesnQotaNum.text.toString() +"-"+ view.addOwnerPosesnQotaDeno.text.toString()
                                val rgistAddrString = view.addOwnerRgistAddrText.text.toString()
                                val unDcsnOwnarAt = view.addOwnerUnDcsnOwnerAt.isChecked

                                if(posesnQotaString == "") {
                                    dialogUtil!!.wtnccAlertDialog("""추가 소유자의 지분이 입력되지 않았습니다.""".trimMargin(), builder!!, "소유자 추가").show()

                                } else if(rgistAddrString == "") {
                                    dialogUtil!!.wtnccAlertDialog("""공부상 주소가 입력 되지 않았습니다.""".trimMargin(), builder!!, "소유자 추가").show()

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

    override fun showOwnerPopup() {
//        TODO("Not yet implemented")
    }
}