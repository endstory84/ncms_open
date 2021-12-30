/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_add_new_owner_dialog.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.databinding.FragmentAddNewOwnerDialogBinding
import kr.or.kreb.ncms.mobile.util.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class AddNewOwnerFragment(val activity: Activity?, context: Context?,
    val addNewOwnerSaveListener: addNewOwnerSaveInterface) :
    DialogFragment(),
    View.OnClickListener, RadioGroup.OnCheckedChangeListener,
    AdapterView.OnItemSelectedListener{

    private var param1: String? = null
    private var param2: String? = null

    private val toastUtil: ToastUtil = ToastUtil(context!!)

    private val wtnncUtill = WtnncUtil(activity!!, context!!)
    var builder: MaterialAlertDialogBuilder? = null
    var dialogUtil: DialogUtil? = null
    private var progressDialog: AlertDialog? = null
    private var checkName: Boolean = false

    private lateinit var binding: FragmentAddNewOwnerDialogBinding

    var logUtil: LogUtil = LogUtil(AddNewOwnerFragment::class.java.simpleName)

    var selectRaido: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.dialog?.setCanceledOnTouchOutside(false)

        binding = FragmentAddNewOwnerDialogBinding.inflate(inflater, container, false)
        builder = context?.let { MaterialAlertDialogBuilder(it) }!!
        dialogUtil = DialogUtil(context, activity)
        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)

        addNewOwnerSelectRaidoGroup.setOnCheckedChangeListener(this)
    }

    fun init(view: View) {


        wtnncUtill.wtnncSpinnerAdapter(R.array.indvdlClArray, addNewOwnerGubunSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.indvdlDtilClArray, addNewOwnerDetailGubunSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.ownerClArray, addNewBankGubunSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.grpClBankArray, addNewBankDetailGubunSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.ownerClArray, addNewGroupGubunSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.grpClArray, addNewGroupDetailGubunSpinner, this)

        addNewOwnerSave.setOnClickListener(this)
        addNewOwnerCancel.setOnClickListener(this)
        addNewOwnerCheckNameBtn.setOnClickListener(this)
        addNewBankCheckNameBtn.setOnClickListener(this)
        addNewGroupCheckNameBtn.setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()
        getDisplayDistance(dialog, activity, 0.65F, 0.6F)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.addNewOwnerSave -> {
//                addNewOwnerSaveListener.onSaveOwner()

                if(selectRaido != 0) {
                    if(checkName) {
                        val addNewOwnerSaveMap = HashMap<String, String>()
                        val addNewOwnerSaveUrl = context!!.resources.getString(R.string.mobile_url) + "newOwnerSave"
                        when (selectRaido) {
                            1 -> { //개인

                                addNewOwnerSaveMap["saupCode"] = PreferenceUtil.getString(context!!, "saupCode", "defaual")
                                addNewOwnerSaveMap["indvdlCl"] = when (addNewOwnerGubunSpinner.selectedItemPosition) {
                                    1 -> "A036003"
                                    2 -> "A036004"
                                    3 -> "A036005"
                                    4 -> "A036006"
                                    5 -> "A036007"
                                    6 -> "A036008"
                                    else -> ""
                                }
                                addNewOwnerSaveMap.put(
                                    "indvdlDtilCl", when (addNewOwnerDetailGubunSpinner.selectedItemPosition) {
                                        1 -> "A103001"
                                        2 -> "A103002"
                                        else -> ""
                                    }
                                )
                                addNewOwnerSaveMap.put("name", addNewOwnerNameEdit.text.toString())
                                addNewOwnerSaveMap.put("sameNameNo", addNewOwnerSameNameEdit.text.toString())
                                var ihidnumString = addNewOwnerIhidnumEdit1.text.toString() +
                                        addNewOwnerIhidnumEdit2.text.toString() +
                                        addNewOwnerIhidnumEdit3.text.toString()
                                if(ihidnumString.equals("-")) {
                                    ihidnumString = ""
                                }
                                addNewOwnerSaveMap.put("ihidnum", ihidnumString)
                                addNewOwnerSaveMap.put("delvyZip", addNewOwnerDelvyzipEdit.text.toString())
                                addNewOwnerSaveMap.put("delvyAdres", addNewOwnerDelvyaddressEdit.text.toString())
                                addNewOwnerSaveMap.put("delvyAdresDetail", addNewOwnerDelvyaddressdetailEdit.text.toString())
                                addNewOwnerSaveMap.put("selectRaido", selectRaido.toString())
                                addNewOwnerSaveMap.put("register", "12345") // 임시 조사원 아이디
                            }
                            2 -> { //단체(법인)
                                addNewOwnerSaveMap.put("saupCode", PreferenceUtil.getString(context!!, "saupCode", "defaual"))
                                addNewOwnerSaveMap.put("ownerCl",when (addNewGroupGubunSpinner.selectedItemPosition) {
                                    1 -> "A015001"
                                    2 -> "A015002"
                                    3 -> "A015003"
                                    4 -> "A015004"
                                    else -> ""
                                })
                                addNewOwnerSaveMap.put("grpCl",when (addNewGroupDetailGubunSpinner.selectedItemPosition) {
                                    1 -> "A030001"
                                    2 -> "A030002"
                                    3 -> "A030003"
                                    4 -> "A030004"
                                    5 -> "A030005"
                                    6 -> "A030007"
                                    7 -> "A030008"
                                    8 -> "A030009"
                                    else -> ""
                                })
                                addNewOwnerSaveMap.put("grpNm",addNewGroupNameEdit.text.toString())
                                addNewOwnerSaveMap.put("sameNameNo",addNewGroupSameNameEdit.text.toString())
                                var jurirnoString = addNewGroupIhidnumEdit1.text.toString() +
                                        addNewGroupIhidnumEdit2.text.toString() +
                                        addNewGroupIhidnumEdit3.text.toString()
                                if(jurirnoString.equals("-")) {
                                    jurirnoString = ""
                                }
                                addNewOwnerSaveMap.put("jurirno",jurirnoString)
                                addNewOwnerSaveMap.put("delvyZip",addNewGroupDelvyzipEdit.text.toString())
                                addNewOwnerSaveMap.put("delvyAdres",addNewGroupDelvyaddressEdit.text.toString())
                                addNewOwnerSaveMap.put("delvyAdresDetail",addNewGroupDelvyaddressdetailEdit.text.toString())
                                addNewOwnerSaveMap.put("selectRaido", selectRaido.toString())
                                addNewOwnerSaveMap.put("register", "12345") // 임시 조사원 아이디
                            }
                            3 -> { //단체(은행)
                                addNewOwnerSaveMap.put("saupCode", PreferenceUtil.getString(context!!, "saupCode", "defaual"))
                                addNewOwnerSaveMap.put("ownerCl",when (addNewBankGubunSpinner.selectedItemPosition) {
                                    1 -> "A015001"
                                    2 -> "A015002"
                                    3 -> "A015003"
                                    4 -> "A015004"
                                    else -> ""
                                })
                                addNewOwnerSaveMap.put("grpCl",when (addNewBankDetailGubunSpinner.selectedItemPosition) {
                                    1 -> "A030006"
                                    else -> ""
                                })
                                addNewOwnerSaveMap["grpNm"] = addNewBankNameEdit.text.toString()
                                addNewOwnerSaveMap["sameNameNo"] = addNewBankSameNameEdit.text.toString()
                                var jurirnoString = addNewGroupIhidnumEdit1.text.toString() + addNewGroupIhidnumEdit2.text.toString() + addNewGroupIhidnumEdit3.text.toString()
                                if(jurirnoString == "-") {
                                    jurirnoString = ""
                                }
                                addNewOwnerSaveMap.put("jurirno",jurirnoString)
                                addNewOwnerSaveMap.put("spotNm",addNewBankBranchnameEdit.text.toString())
                                addNewOwnerSaveMap.put("delvyZip",addNewBankDelvyzipEdit.text.toString())
                                addNewOwnerSaveMap.put("delvyAdres",addNewBankDelvyaddressEdit.text.toString())
                                addNewOwnerSaveMap.put("delvyAdresDetail",addNewBankDelvyaddressdetailEdit.text.toString())
                                addNewOwnerSaveMap.put("selectRaido", selectRaido.toString())
                                addNewOwnerSaveMap.put("register", "12345") // 임시 조사원 아이디
                            }
                        }
                        HttpUtil.getInstance(context!!)
                            .callerUrlInfoPostWebServer(addNewOwnerSaveMap, progressDialog, addNewOwnerSaveUrl,
                                object: Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        progressDialog!!.dismiss()
                                        logUtil.e("fail")
                                    }

                                    override fun onResponse(call: Call, response: Response) {

                                        val responseString = response.body!!.string()

                                        logUtil.d("addNewOwnerSaveMap response ---------------------> $responseString")

                                        progressDialog!!.dismiss()

                                        val responseJson = JSONObject(responseString).getJSONObject("list").getJSONObject("addOwner")


                                        addNewOwnerSaveListener.onSaveOwner(responseJson, selectRaido)

                                        dialog!!.dismiss()
                                    }

                                })
                    }else {
                        toastUtil.msg_error("이름을 확인 후 저장 해주시기 바람니다.", 500)
                    }
                } else {
                    toastUtil.msg_error("소유자 타입이 선택되지 않았습니다", 500)
                }

//                dialog!!.dismiss()
            }
            R.id.addNewOwnerCancel -> {
                dialog!!.dismiss()
            }
            R.id.addNewOwnerCheckNameBtn ,
            R.id.addNewBankCheckNameBtn,
            R.id.addNewGroupCheckNameBtn -> {
                if(selectRaido != 0) {
                    when (selectRaido) {
                        1 -> {
                            // 개인 이름 확인
                            if(addNewOwnerNameEdit.text.toString().equals("")) {
                                toastUtil.msg_error("개인 소유자 이름을 입력 해주시기 바람니다.", 500)
                            }  else {
                                val confirmOwnerNameMap = HashMap<String, String>()
                                confirmOwnerNameMap.put("name", addNewOwnerNameEdit.text.toString())
                                confirmOwnerNameMap.put("sameNameNo", addNewOwnerSameNameEdit.text.toString())
                                val ihidnumString = addNewOwnerIhidnumEdit1.text.toString() +
                                                addNewOwnerIhidnumEdit2.text.toString() +
                                                addNewOwnerIhidnumEdit3.text.toString()
                                confirmOwnerNameMap.put("ihidnum", ihidnumString)
                                confirmOwnerNameMap.put("saupCode", PreferenceUtil.getString(context!!, "saupCode","default"))

                                val confirmOwnerNameUrl = context!!.resources.getString(R.string.mobile_url) + "confirmOwnerName"

                                HttpUtil.getInstance(context!!)
                                    .callerUrlInfoPostWebServer(confirmOwnerNameMap, progressDialog, confirmOwnerNameUrl,
                                    object: Callback {
                                        override fun onFailure(call: Call, e: IOException) {
                                            progressDialog!!.dismiss()
                                            logUtil.e("fail")
                                        }

                                        override fun onResponse(call: Call, response: Response) {
                                            val responseString = response.body!!.string()

                                            logUtil.d("confirmOwnerName response String -------------------> $responseString")
                                            progressDialog!!.dismiss()

                                            val responseJson = JSONObject(responseString).getJSONObject("list")
                                            if(responseJson.getString("messageNum").equals("1")) {
                                                checkName = false
                                            } else {
                                                checkName = true
                                            }

                                            activity?.runOnUiThread {
                                                dialogUtil!!.confirmDialog(responseJson.getString("message"), builder!!, "확인").show()
                                            }



                                        }

                                    })
                            }

                        }
                        2 -> {
                            // 단체(법인) 이름 확인
                            if(addNewGroupNameEdit.text.toString().equals("")) {
                                toastUtil.msg_error("단체(법인) 소유자 이름을 입력 해주시기 바람니다.", 500)
                            }  else {
                                val confirmGroupNameMap = HashMap<String, String>()
                                confirmGroupNameMap.put("name", addNewGroupNameEdit.text.toString())
                                confirmGroupNameMap.put("sameNameNo", addNewGroupSameNameEdit.text.toString())
                                val ihidnumString = addNewGroupIhidnumEdit1.text.toString() +
                                        addNewGroupIhidnumEdit2.text.toString() +
                                        addNewGroupIhidnumEdit3.text.toString()
                                confirmGroupNameMap.put("ihidnum", ihidnumString)
                                confirmGroupNameMap.put("saupCode", PreferenceUtil.getString(context!!, "saupCode","default"))

                                val confirmGroupNameUrl = context!!.resources.getString(R.string.mobile_url) + "confirmGroupName"

                                HttpUtil.getInstance(context!!)
                                    .callerUrlInfoPostWebServer(confirmGroupNameMap, progressDialog, confirmGroupNameUrl,
                                        object: Callback {
                                            override fun onFailure(call: Call, e: IOException) {
                                                progressDialog!!.dismiss()
                                                logUtil.e("fail")
                                            }

                                            override fun onResponse(call: Call, response: Response) {
                                                val responseString = response.body!!.string()

                                                logUtil.d("confirmGroupName response String -------------------> $responseString")

                                                val responseJson = JSONObject(responseString).getJSONObject("list")

                                                if(responseJson.getString("messageNum").equals("1")) {
                                                    checkName = false
                                                } else {
                                                    checkName = true
                                                }

                                                progressDialog!!.dismiss()

                                                activity?.runOnUiThread {
                                                    dialogUtil!!.confirmDialog(responseJson.getString("message"), builder!!, "확인").show()
                                                }
                                            }

                                        })
                            }
                        }
                        3 -> {
                            // 단체(은행) 이름 확인
                            if(addNewBankNameEdit.text.toString().equals("")) {
                                toastUtil.msg_error("단체(개인) 소유자 이름을 입력 해주시기 바람니다.", 500)
                            } else {
                                val confirmBankNameMap = HashMap<String, String>()
                                confirmBankNameMap.put("name", addNewBankNameEdit.text.toString())
                                confirmBankNameMap.put("sameNameNo", addNewBankSameNameEdit.text.toString())

                                val ihidnumString = addNewBankIhidnumEdit1.text.toString() +
                                        addNewBankIhidnumEdit2.text.toString() +
                                        addNewBankIhidnumEdit3.text.toString()
                                confirmBankNameMap.put("ihidnum", ihidnumString)
                                confirmBankNameMap.put("saupCode", PreferenceUtil.getString(context!!, "saupCode","default"))

                                val confirmBankNameUrl = context!!.resources.getString(R.string.mobile_url) + "confirmBankName"

                                HttpUtil.getInstance(context!!)
                                    .callerUrlInfoPostWebServer(confirmBankNameMap, progressDialog, confirmBankNameUrl,
                                        object: Callback {
                                            override fun onFailure(call: Call, e: IOException) {
                                                progressDialog!!.dismiss()
                                                logUtil.e("fail")
                                            }

                                            override fun onResponse(call: Call, response: Response) {
                                                val responseString = response.body!!.string()

                                                logUtil.d("confirmBankName response String -------------------> $responseString")

                                                val responseJson = JSONObject(responseString).getJSONObject("list")

                                                if(responseJson.getString("messageNum").equals("1")) {
                                                    checkName = false
                                                } else {
                                                    checkName = true
                                                }

                                                progressDialog!!.dismiss()

                                                activity?.runOnUiThread {
                                                    dialogUtil!!.confirmDialog(responseJson.getString("message"), builder!!, "확인").show()
                                                }
                                            }

                                        })
                            }
                        }
                    }

                } else {
                    toastUtil.msg_error("소유자 타입이 선택되지 않았습니다", 500)
                }
            }
        }
    }

    override fun onCheckedChanged(rg: RadioGroup?, id: Int) {
       when(id) {
           R.id.addNewSelectOwner -> {
               addNewOwner.visibility = View.VISIBLE
               addNewBank.visibility = View.GONE
               addNewGroup.visibility = View.GONE
               selectRaido = 1
           }
           R.id.addNewSelectGroup -> {
               addNewOwner.visibility = View.GONE
               addNewBank.visibility = View.GONE
               addNewGroup.visibility = View.VISIBLE
               selectRaido = 2
           }
           R.id.addNewSelectBank -> {
               addNewOwner.visibility = View.GONE
               addNewBank.visibility = View.VISIBLE
               addNewGroup.visibility = View.GONE
               selectRaido = 3
           }
       }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    interface addNewOwnerSaveInterface {
        fun onSaveOwner(dataInfo: JSONObject, grpSe: Int)
    }


}