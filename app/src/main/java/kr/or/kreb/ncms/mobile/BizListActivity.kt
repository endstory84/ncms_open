

/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Intent
import android.database.Cursor
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_biz_list_with_drawerlayout.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.or.kreb.ncms.mobile.adapter.BizAdapter
import kr.or.kreb.ncms.mobile.base.BaseActivity
import kr.or.kreb.ncms.mobile.data.Biz
import kr.or.kreb.ncms.mobile.data.SearchKeyword
import kr.or.kreb.ncms.mobile.data.db.NcmsDB
import kr.or.kreb.ncms.mobile.databinding.ActivityBizListWithDrawerlayoutBinding
import kr.or.kreb.ncms.mobile.enums.BizListType
import kr.or.kreb.ncms.mobile.enums.ToastType
import kr.or.kreb.ncms.mobile.listener.NavSetItemListener
import kr.or.kreb.ncms.mobile.util.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * @info 사업 전체
 */

class BizListActivity :
    BaseActivity<ActivityBizListWithDrawerlayoutBinding>(
        R.layout.activity_biz_list_with_drawerlayout,
        BizListActivity::class.java.simpleName
    ),
    AdapterView.OnItemSelectedListener {

    var context = this

    lateinit var layoutManager: LinearLayoutManager
    lateinit var gridManager: GridLayoutManager
    lateinit var adapter: BizAdapter

    lateinit var db: NcmsDB
    lateinit var searchKeyword: SearchKeyword

    var loginId: String? = null
    var searchQuery: String? = null

    override fun initViewStart() {

        dialogUtil = DialogUtil(this, this)
        loginId = intent!!.extras!!.get("id").toString()

        progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(this))

        initUI()

    }

    override fun initDataBinding() {}

    override fun initViewFinal() {}

    override fun onResume() {
        super.onResume()
        setPageCode(Constants.BIZ_LIST_ACT)
        setLocalDB()
    }

    /**
     * 로컬DB
     */
    private fun setLocalDB() {
        db = NcmsDB.getInstance(context)!!

        db.dao().getAll().observe(this) { values ->
            log.d(values.toString())
            if (values.isNotEmpty()) {
                ivChipGroupBizMainDeleteAll.setOnClickListener {
                    db.dao().deleteAll()
                    chipGroupBizMain.removeAllViews()
                    bizMainChipLayout.goneView()
                }
                chipGroupBizMain.removeAllViews()
                for (element in values) {
                    val addChip = Chip(context, null, R.style.Widget_MaterialComponents_Chip_Choice)
                    //addChip.isCheckable = true
                    addChip.setOnClickListener {
                        searchViewBizSelect.setQuery(element.keyword, true)
                        addChip.isChecked = true
                    }
                    addChip.text = element.keyword
                    chipGroupBizMain.addView(addChip)
                }
                bizMainChipLayout.visibleView()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (Intent.ACTION_SEARCH == intent?.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                searchViewBizSelect.setQuery(query, false)
                searchQuery = searchViewBizSelect.query.toString()
            }
        }
    }

    private fun initUI() {

        setSupportActionBar(appToolbar)

        supportActionBar?.run {
            setIcon(R.drawable.ic_kreb_symbol)
            setDisplayUseLogoEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        layout_biz_list_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        setSearchView()

        setBizListAdapter()

        //tvBizMainGuide
        val requireArr = mutableListOf<TextView>(tvBizMainGuide)
        setRequireContent(requireArr)

    }

    /**
     * 서치뷰
     */
    private fun setSearchView() {
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager

        searchViewBizSelect.apply {
            requestFocus()
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setIconifiedByDefault(false)
            isSubmitButtonEnabled = true
            queryHint = "사업명을 입력해주세요."

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {

                    /**
                     * 최근검색어 (Chip, Room Database 인용)
                     */

                    val bizMainKeywordChip = Chip(context, null, R.style.Widget_MaterialComponents_Chip_Choice)

                    bizMainKeywordChip.apply {
                        text = query
                        isClickable = true
                        isFocusable = true

                        setOnClickListener {
                            for (i in 0 until chipGroupBizMain.childCount) {
                                (chipGroupBizMain.getChildAt(i) as Chip).isChecked = false
                            }
                            //isChecked = true
                            searchViewBizSelect.setQuery(query, true)
                        }

                        var isChipVisable = false

                        searchKeyword = SearchKeyword(0, query)

                        if (chipGroupBizMain.childCount == 0) {

                            GlobalScope.launch(Dispatchers.IO) {
                                delay(1000L)
                                db.dao().insert(searchKeyword)
                                log.d("내부 DB keyword QUERY -> insert()")
                            }

                            chipGroupBizMain.addView(bizMainKeywordChip)
                            bizMainChipLayout.visibleView()

                        } else {
                            for (i in 0 until chipGroupBizMain.childCount) {
                                if (query != (chipGroupBizMain.getChildAt(i) as Chip).text) {
                                    isChipVisable = false
                                } else {
                                    isChipVisable = true
                                    break
                                }
                            }
                            if (!isChipVisable) {
                                GlobalScope.launch(Dispatchers.IO) {
                                    delay(1000L)
                                    db.dao().insert(searchKeyword)
                                    log.d("내부 DB keyword QUERY -> insert()")
                                }
                            }
                        }
                    }

                    return true
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    adapter.run { filter.filter(query) }
                    return true
                }
            })

            setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionSelect(position: Int): Boolean {
                    log.d("sugSelect>> $position")
                    return true
                }

                @SuppressLint("Range")
                override fun onSuggestionClick(position: Int): Boolean {
                    val cursor: Cursor = searchViewBizSelect.suggestionsAdapter.getItem(position) as Cursor
                    val suggest1 =
                        cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)) // 선택된 recentQuery String
                    log.d("서치뷰 스트링 -> $suggest1")
                    searchViewBizSelect.setQuery(suggest1, true)
                    return true
                }

            })
        }
    }

    /**
     * 사업의 종류 Adapter
     */
    private fun setBizListAdapter() {

        ArrayAdapter.createFromResource(
            this,
            R.array.biz_confirm_category_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinerBizSelect.adapter = adapter
            spinerBizSelect.onItemSelectedListener = this
        }

        // RecylerView (가로, 세로 교차 레이아웃)
        if (getWindowOrientation(this) == 2) {
            gridManager = GridLayoutManager(this, 2)
            recylerViewBizMain.layoutManager = gridManager
        } else {
            layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recylerViewBizMain.layoutManager = layoutManager
        }

//        settingBizAll(searchQuery)
        reqBizList(BizListType.ALL, searchQuery)

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        log.d("spiner item -> $position")

        val queryString = searchViewBizSelect.query.toString()

        when (parent?.id) {
            R.id.spinerBizSelect -> {
                when (position) {
                    0 -> { // 사업전체
//                        settingBizAll(searchViewBizSelect.query.toString())
                        reqBizList(BizListType.ALL, queryString)
                    }
                    1 -> { // 현장조사
//                        settingBizSearch(searchViewBizSelect.query.toString())
                        reqBizList(BizListType.SEARCH, queryString)
                    }
                    2 -> { // 잔여지지
//                        settingBizRest(searchViewBizSelect.query.toString())
                        reqBizList(BizListType.REST, queryString)
                    }


                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    /**
     * 사업 목록 조회
     */
    fun reqBizList(type: BizListType, query: String?) {
        val searchQuery = if(query === null){
            ""
        } else {
            query
        }

        val bsnsChoiceUrl = context.resources.getString(R.string.mobile_url) + when(type) {
            BizListType.ALL -> "bsnsChoiseList"
            BizListType.SEARCH -> "bsnsChoiseSearchList"
            BizListType.REST -> "bsnsChoiseRestList"
        }


        log.d("====>>>>     reqBizList - Biz Type : ${type}, Value : ${type.value}, URL : ${bsnsChoiceUrl}")

        val bsnsChoiceMap = HashMap<String, String>()
        bsnsChoiceMap.put("searchBsnsPsCl", "")
        bsnsChoiceMap.put("searchBsnsNm", "")
        bsnsChoiceMap.put("searchNcm", "")
        bsnsChoiceMap.put("searchComboBsnsPsCl", "")
        bsnsChoiceMap.put("register", loginId!!) // 로그인 id 임시 등록
        bsnsChoiceMap.put("acntTy", "") // 로그인 id 사용자의 계정 구분

        HttpUtil.getInstance(context)
            .callerUrlInfoPostWebServer(bsnsChoiceMap, progressDialog, bsnsChoiceUrl,
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        dismissProgress()
                        showToast(ToastType.ERROR, R.string.msg_server_bsns_connected_fail, 100)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseString = response.body!!.string()

                        log.d("bsnsChoiceList response $responseString")

                        dismissProgress()

                        try {

                            val dataJSON = JSONObject(responseString).getJSONObject("list").getJSONArray("bsnsChoise") as JSONArray

                            runOnUiThread {
                                val tempDataList = mutableListOf<Biz>()
                                for (i in 0 until dataJSON.length()) {
                                    val dataObject = dataJSON.getJSONObject(i)
                                    tempDataList.add(
                                        Biz(
                                            dataObject.getString("saupCode"),
                                            dataObject.getString("contCode"),
                                            dataObject.getString("bsnsCl"),
                                            dataObject.getString("bsnsClNm"),
                                            dataObject.getString("bsnsNm"),
                                            dataObject.getString("bsnsLocplc"),
                                            dataObject.getString("bsnsPsCl"),
                                            dataObject.getString("bsnsPsClNm"),
                                            dataObject.getString("oclhgBnoraCl"),
                                            dataObject.getString("oclhgBnoraClNm"),
                                            dataObject.getString("bsnsLclasCl"),
                                            dataObject.getString("oclhgBnora"),
                                            dataObject.getString("excAceptncAt"),
                                            dataObject.getString("excUseAt"),
                                            dataObject.getString("excDtls"),
                                            dataObject.getString("ncm"),
                                            dataObject.getString("cntrctDe"),
                                            dataObject,
                                            when(type) {
                                                BizListType.ALL -> null
                                                BizListType.SEARCH, BizListType.REST -> dataObject.getJSONArray("bsnsLandList")
                                            },
                                            when(type) {
                                                BizListType.ALL, BizListType.SEARCH -> null
                                                BizListType.REST -> dataObject.getJSONArray("bsnsThingList")
                                            },
                                            loginId!!
                                        )
                                    )
                                }
                                GlobalScope.launch(Dispatchers.IO) {
                                    delay(500)
                                    runOnUiThread {
                                        adapter = BizAdapter(context, tempDataList, type)
                                        recylerViewBizMain.adapter = adapter
                                        ViewCompat.setNestedScrollingEnabled(recylerViewBizMain, false)
                                        adapter.filter.filter(searchQuery)
                                    }
                                }
                            }
                        }
                        catch(e: Exception) {
                            e.printStackTrace()
                            showToastError()
                        }

                    }
                })
    }


//    /**
//     * 사업전체
//     */
//    fun settingBizAll(query: String?) {
//        val searchQuery = if(query === null){
//            ""
//        } else {
//            query
//        }
//
//        val bsnsChoiceUrl = context.resources.getString(R.string.mobile_url) + "bsnsChoiseList"
//        val bsnsChoiceMap = HashMap<String, String>()
//        bsnsChoiceMap.put("searchBsnsPsCl", "")
//        bsnsChoiceMap.put("searchBsnsNm", "")
//        bsnsChoiceMap.put("searchNcm", "")
//        bsnsChoiceMap.put("searchComboBsnsPsCl", "")
//        bsnsChoiceMap.put("register", loginId!!) // 로그인 id 임시 등록
//        bsnsChoiceMap.put("acntTy", "") // 로그인 id 사용자의 계정 구분
//
//        val progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(this))
//
//        HttpUtil.getInstance(context)
//            .callerUrlInfoPostWebServer(bsnsChoiceMap, progressDialog, bsnsChoiceUrl,
//                object : Callback {
//                    override fun onFailure(call: Call, e: IOException) {
//                        progressDialog.dismiss()
//                        runOnUiThread {
//                            toast.msg_error(R.string.msg_server_bsns_connected_fail, 100)
//                        }
//                    }
//
//                    override fun onResponse(call: Call, response: Response) {
//                        val responseString = response.body!!.string()
//
//                        log.d("bsnsChoiceList response $responseString")
//
//                        val dataJSON =
//                            JSONObject(responseString).getJSONObject("list").getJSONArray("bsnsChoise") as JSONArray
//                        progressDialog.dismiss()
//
//                        runOnUiThread {
//                            val tempDataList = mutableListOf<Biz>()
//                            for (i in 0 until dataJSON.length()) {
//                                val dataObject = dataJSON.getJSONObject(i)
//                                tempDataList.add(
//                                    Biz(
//                                        dataObject.getString("saupCode"),
//                                        dataObject.getString("contCode"),
//                                        dataObject.getString("bsnsCl"),
//                                        dataObject.getString("bsnsClNm"),
//                                        dataObject.getString("bsnsNm"),
//                                        dataObject.getString("bsnsLocplc"),
//                                        dataObject.getString("bsnsPsCl"),
//                                        dataObject.getString("bsnsPsClNm"),
//                                        dataObject.getString("oclhgBnoraCl"),
//                                        dataObject.getString("oclhgBnoraClNm"),
//                                        dataObject.getString("bsnsLclasCl"),
//                                        dataObject.getString("oclhgBnora"),
//                                        dataObject.getString("excAceptncAt"),
//                                        dataObject.getString("excUseAt"),
//                                        dataObject.getString("excDtls"),
//                                        dataObject.getString("ncm"),
//                                        dataObject.getString("cntrctDe"),
//                                        dataObject,
//                                        null,
//                                        null,
//                                        loginId!!
//                                    )
//                                )
//                            }
//                            GlobalScope.launch(Dispatchers.IO) {
//                                delay(500)
//                                runOnUiThread {
//                                    adapter = BizAdapter(context, tempDataList, "all")
//                                    recylerViewBizMain.adapter = adapter
//                                    ViewCompat.setNestedScrollingEnabled(recylerViewBizMain, false)
//                                    adapter.filter.filter(searchQuery)
//                                }
//                            }
//                        }
//                    }
//                })
//    }
//
//    /**
//     * 현장조사
//     */
//    fun settingBizSearch(query: String?) {
//        val searchQuery = if(query === null){
//            ""
//        } else {
//            query
//        }
//        recylerViewBizMain.adapter = null
//        val bsnsChoiceUrl = context.resources.getString(R.string.mobile_url) + "bsnsChoiseSearchList"
//        val bsnsChoiceMap = HashMap<String, String>()
//        bsnsChoiceMap["searchBsnsPsCl"] = ""
//        bsnsChoiceMap["searchBsnsNm"] = ""
//        bsnsChoiceMap["searchNcm"] = ""
//        bsnsChoiceMap["searchComboBsnsPsCl"] = ""
//        bsnsChoiceMap["register"] = loginId!! // 로그인 id 임시 등록
//        bsnsChoiceMap["acntTy"] = "" // 로그인 id 사용자의 계정 구분
//
//        val progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(this))
//
//        HttpUtil.getInstance(context)
//            .callerUrlInfoPostWebServer(bsnsChoiceMap, progressDialog, bsnsChoiceUrl,
//                object : Callback {
//                    override fun onFailure(call: Call, e: IOException) {
//                        progressDialog.dismiss()
//                        toast.msg_error(R.string.msg_server_bsns_connected_fail, 100)
//                    }
//
//                    override fun onResponse(call: Call, response: Response) {
//                        val responseString = response.body!!.string()
//
//                        log.d("bsnsChoiceList response $responseString")
//
//                        val dataJSON =
//                            JSONObject(responseString).getJSONObject("list").getJSONArray("bsnsChoise") as JSONArray
//                        progressDialog.dismiss()
//
//                        runOnUiThread {
//                            val tempDataList = mutableListOf<Biz>()
//                            for (i in 0 until dataJSON.length()) {
//                                val dataObject = dataJSON.getJSONObject(i)
//                                tempDataList.add(
//                                    Biz(
//                                        dataObject.getString("saupCode"),
//                                        dataObject.getString("contCode"),
//                                        dataObject.getString("bsnsCl"),
//                                        dataObject.getString("bsnsClNm"),
//                                        dataObject.getString("bsnsNm"),
//                                        dataObject.getString("bsnsLocplc"),
//                                        dataObject.getString("bsnsPsCl"),
//                                        dataObject.getString("bsnsPsClNm"),
//                                        dataObject.getString("oclhgBnoraCl"),
//                                        dataObject.getString("oclhgBnoraClNm"),
//                                        dataObject.getString("bsnsLclasCl"),
//                                        dataObject.getString("oclhgBnora"),
//                                        dataObject.getString("excAceptncAt"),
//                                        dataObject.getString("excUseAt"),
//                                        dataObject.getString("excDtls"),
//                                        dataObject.getString("ncm"),
//                                        dataObject.getString("cntrctDe"),
//                                        dataObject,
//                                        dataObject.getJSONArray("bsnsLandList"),
//                                        null,
//                                        loginId!!
//                                    )
//                                )
//                            }
//                            GlobalScope.launch(Dispatchers.IO) {
//                                delay(500)
//                                runOnUiThread {
//                                    adapter = BizAdapter(context, tempDataList, "search")
//                                    recylerViewBizMain.adapter = adapter
//                                    ViewCompat.setNestedScrollingEnabled(recylerViewBizMain, false)
//                                    adapter.filter.filter(searchQuery)
//                                }
//                            }
//                        }
//
//
//                    }
//
//                }
//            )
//
//    }
//
//    fun settingBizRest(query: String?) {
//        val searchQuery = if(query === null){
//            ""
//        } else {
//            query
//        }
//        recylerViewBizMain.adapter = null
//        val bsnsChoiceUrl = context.resources.getString(R.string.mobile_url) + "bsnsChoiseRestList"
//        val bsnsChoiceMap = HashMap<String, String>()
//        bsnsChoiceMap.put("searchBsnsPsCl", "")
//        bsnsChoiceMap.put("searchBsnsNm", "")
//        bsnsChoiceMap.put("searchNcm", "")
//        bsnsChoiceMap.put("searchComboBsnsPsCl", "")
//        bsnsChoiceMap.put("register", loginId!!) // 로그인 id 임시 등록
//        bsnsChoiceMap.put("acntTy", "") // 로그인 id 사용자의 계정 구분
//
//        val progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(this))
//
//        HttpUtil.getInstance(context)
//            .callerUrlInfoPostWebServer(bsnsChoiceMap, progressDialog, bsnsChoiceUrl,
//                object : Callback {
//                    override fun onFailure(call: Call, e: IOException) {
//                        progressDialog.dismiss()
//                        toast.msg_error(R.string.msg_server_bsns_connected_fail, 100)
//                    }
//
//                    override fun onResponse(call: Call, response: Response) {
//                        val responseString = response.body!!.string()
//
//                        log.d("bsnsChoiceList response $responseString")
//
//                        val dataJSON =
//                            JSONObject(responseString).getJSONObject("list").getJSONArray("bsnsChoise") as JSONArray
//                        progressDialog.dismiss()
//
//                        runOnUiThread {
//                            val tempDataList = mutableListOf<Biz>()
//                            for (i in 0 until dataJSON.length()) {
//                                val dataObject = dataJSON.getJSONObject(i)
//                                tempDataList.add(
//                                    Biz(
//                                        dataObject.getString("saupCode"),
//                                        dataObject.getString("contCode"),
//                                        dataObject.getString("bsnsCl"),
//                                        dataObject.getString("bsnsClNm"),
//                                        dataObject.getString("bsnsNm"),
//                                        dataObject.getString("bsnsLocplc"),
//                                        dataObject.getString("bsnsPsCl"),
//                                        dataObject.getString("bsnsPsClNm"),
//                                        dataObject.getString("oclhgBnoraCl"),
//                                        dataObject.getString("oclhgBnoraClNm"),
//                                        dataObject.getString("bsnsLclasCl"),
//                                        dataObject.getString("oclhgBnora"),
//                                        dataObject.getString("excAceptncAt"),
//                                        dataObject.getString("excUseAt"),
//                                        dataObject.getString("excDtls"),
//                                        dataObject.getString("ncm"),
//                                        dataObject.getString("cntrctDe"),
//                                        dataObject,
//                                        dataObject.getJSONArray("bsnsLandList"),
//                                        dataObject.getJSONArray("bsnsThingList"),
//                                        loginId!!
//                                    )
//                                )
//                            }
//                            GlobalScope.launch(Dispatchers.IO) {
//                                delay(500)
//                                runOnUiThread {
//                                    adapter = BizAdapter(context, tempDataList, "rest")
//                                    recylerViewBizMain.adapter = adapter
//                                    ViewCompat.setNestedScrollingEnabled(recylerViewBizMain, false)
//                                    adapter.filter.filter(searchQuery)
//                                }
//                            }
//                        }
//                    }
//
//                }
//            )
//    }
}