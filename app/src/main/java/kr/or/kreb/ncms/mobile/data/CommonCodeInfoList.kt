/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

object CommonCodeInfoList {

    const val TAG = "CommonCodeInfoList"

    const val PARAM_NM_ID: String       = "codeId"
    const val PARAM_NM_DC: String       = "codeDc"
    const val PARAM_NM_GROUP_ID: String = "codeGroupId"
    const val PARAM_NM_ORDR: String     = "codeOrdr"

    class CommonCodeInfo(var codeId: String, var codeDc: String, codeOrdr: Int) {

    }

    private var commonCodeMap: HashMap<String, ArrayList<CommonCodeInfo>> = HashMap<String, ArrayList<CommonCodeInfo>>()

    fun addCode(groupCode: String, codeInfo: CommonCodeInfo) {

        val list: ArrayList<CommonCodeInfo>

        if(!commonCodeMap.containsKey(groupCode)) {
            list = ArrayList<CommonCodeInfo>()
        }
        else {
            list = commonCodeMap.get(groupCode)!!
        }

        list.add(codeInfo)

        commonCodeMap.put(groupCode, list)
    }

    fun addCode(groupCode: String, jsonArray: JSONArray) {

        val list: ArrayList<CommonCodeInfo>

        if(!commonCodeMap.containsKey(groupCode)) {
            list = ArrayList<CommonCodeInfo>()
        }
        else {
            list = commonCodeMap.get(groupCode)!!
        }

        for (idx in 0..(jsonArray.length() - 1)) {

            val item = jsonArray.getJSONObject(idx)
            val codeId: String = item.getString(PARAM_NM_ID)
            val codeDc: String = item.getString(PARAM_NM_DC)
            val codeOrdr: Int = item.getInt(PARAM_NM_ORDR)
            val codeInfo = CommonCodeInfo(codeId, codeDc, codeOrdr)
            list.add(codeInfo)
        }

        commonCodeMap.put(groupCode, list)

    }

    fun addCode(jsonObject: JSONObject) {

        val iterator = jsonObject.keys().iterator()

        while(iterator.hasNext()) {
            val key = iterator.next()
            val dataCodeList = jsonObject.getJSONArray(key)

            val list = ArrayList<CommonCodeInfo>()

            // 선택
            val selCodeId = ""
            val selCodeDc = "선택"
            val selCodeOrdr = 0
            val infoSel = CommonCodeInfo(selCodeId, selCodeDc, selCodeOrdr)
            list.add(infoSel)

            for (idx in (0..(dataCodeList.length() - 1))) {
                val item = dataCodeList.getJSONObject(idx)

                val codeId = item.getString(PARAM_NM_ID)
                val codeDc = item.getString(PARAM_NM_DC)
                val codeOrdr = item.getInt(PARAM_NM_ORDR)

                val info = CommonCodeInfo(codeId, codeDc, codeOrdr)
                list.add(info)
            }

            commonCodeMap.put(key, list)
        }

    }

    fun removeAll() {
        val iterator = commonCodeMap.keys.iterator()
        while(iterator.hasNext()) {
            val key = iterator.next()
            val list = commonCodeMap.get(key)
            list!!.clear()
            commonCodeMap.remove(key)
        }
    }

    fun isEmpty() : Boolean {

        val iterator = commonCodeMap.keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            val codeInfoList = commonCodeMap.get(key)
            if (null != codeInfoList && codeInfoList.size > 0) {
                return false
            }
        }

        return true

    }

    fun getCodeDcList(groupCode: String) : List<String> {

        val list = ArrayList<String>()

        if (groupCode.isNotEmpty()) {

            val codeInfoList = commonCodeMap.get(groupCode)
            if(null != codeInfoList) {
                for (idx in (0..(codeInfoList.size - 1))) {
                    val info = codeInfoList.get(idx)
                    list.add(info.codeDc)
                }
            }

        }

        return list

    }

    fun getCodeDcArray(groupCode: String) : Array<String> {

        val list = ArrayList<String>()

        if (groupCode.isNotEmpty()) {

            val codeInfoList = commonCodeMap.get(groupCode)
            if(null != codeInfoList && codeInfoList.isNotEmpty()) {
                for (idx in (0..(codeInfoList.size - 1))) {
                    val info = codeInfoList.get(idx)
                    list.add(info.codeDc)
                }
            }

        }

        return list.toArray(arrayOfNulls<String>(list.size))

    }

    fun getCodeId(groupCode: String, codeDc: String) : String {

        if(groupCode.isNotEmpty() && codeDc.length > 0) {
            val list = commonCodeMap.get(groupCode)
            if (null != list && list.size > 0) {
                for (idx in (0..(list.size - 1))) {
                    if (codeDc.equals(list.get(idx).codeDc)) {
                        return list.get(idx).codeId
                    }
                }
            }
        }

        return ""

    }

    fun getCodeId(groupCode: String, idx: Int) : String {

        if (groupCode.isNotEmpty() && idx >= 0) {
            val list = commonCodeMap.get(groupCode)
            if (null != list) {
                if (list.size >= idx) {
                    return list.get(idx).codeId
                }
                else {
                    return list.get(0).codeId
                }
            }
        }

        return ""
    }

    fun getIdxFromCodeId(groupCode: String, codeId: String): Int {
        return getIdxFromCodeId(groupCode, codeId, 0)
    }

    fun getIdxFromCodeId(groupCode: String, codeId: String, defaultValue: Int): Int {

        if (groupCode.isNotEmpty() && codeId.isNotEmpty()) {

            val list = commonCodeMap.get(groupCode)
            if (null != list) {
                for (idx in (0..(list.size - 1))) {
                    val item = list.get(idx)
                    if (codeId.equals(item.codeId)) {
                        return idx
                    }
                }
            }

        }

        return defaultValue

    }

    fun getCodeSize(groupCode: String): Int {
        val list = commonCodeMap.get(groupCode)
        if (null != list) {
            return list.size
        }

        return 0
    }

    fun print() {

        val iterator = commonCodeMap.keys.iterator()

        while(iterator.hasNext()) {

            val key = iterator.next()
            val list = commonCodeMap.get(key)

            Log.d(TAG, "====>>>>        KEY : $key")

            for (idx in (0..(list!!.size - 1))) {
                val item = list.get(idx)
                val id = item.codeId
                val dc = item.codeDc
                Log.d(TAG, "====>>>>            idx : $idx, id : $id, dc : $dc")
            }
            Log.d(TAG, "")
        }
    }


}



