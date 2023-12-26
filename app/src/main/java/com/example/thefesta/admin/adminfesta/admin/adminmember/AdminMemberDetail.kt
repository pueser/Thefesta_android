package com.example.thefesta.admin.adminfesta.admin.adminmember

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thefesta.R
import com.example.thefesta.databinding.FragmentAdminMemberDetailBinding
import com.example.thefesta.databinding.ItemAdminmemberdetailDataListBinding
import com.example.thefesta.retrofit.AdminClient
import com.example.thefesta.service.IAdminService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.AlertDialog
import android.widget.PopupMenu
import android.widget.Toast
import com.example.thefesta.adminbottomnavi.AdminMember
import com.example.thefesta.model.member.MemberDTO

data class AdminMemberDetailData(
    val reportid: Int,
    val reportcontent: String,
    val reporter: String,
    val rbrno: Double?,
    val rfrno: Double?,
    val rbid: Double?,
    val reportdate: String
) {
    val formattedReportnumber: String
        get() {
            return when {
                rbrno == 0.0 && rfrno == 0.0 -> "게시글 코드"
                rbrno == 0.0 && rbid == 0.0 -> "축제 댓글코드"
                rfrno == 0.0 && rbid == 0.0 -> "게시글 댓글코드"
                else -> "Unknown Code"
            }
        }
}


private var memberId: String = ""
private var statecode: String = ""
private var finalaccess: String = ""
private var statecodeChange: String= ""
private var newStatecode: String= ""

class AdminMemberDetail : Fragment() {
    private lateinit var binding: FragmentAdminMemberDetailBinding
    private var amount: Int = 0

    companion object {
        private const val ARG_ID = "arg_id"
        private const val ARG_STATECODE = "arg_statecode"
        private const val ARG_FINALACCESS = "arg_finalaccess"

        fun newInstance(memberId: String, statecode: String, finalaccess: String): AdminMemberDetail {
            val fragment = AdminMemberDetail()
            val args = Bundle()
            args.putString(ARG_ID, memberId)
            args.putString(ARG_STATECODE, statecode)
            args.putString(ARG_FINALACCESS, finalaccess)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminMemberDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        memberId = arguments?.getString(ARG_ID).orEmpty()
        statecode = arguments?.getString(ARG_STATECODE).orEmpty()
        finalaccess = arguments?.getString(ARG_FINALACCESS).orEmpty()

        if (memberId.isNotBlank() && statecode.isNotBlank() && finalaccess.isNotBlank()) {
            binding.reportId.text = "신고대상 : $memberId"
            binding.memberState.text = "회원상태 : $statecode"
            binding.finalaccess.text = "최근 접속일 : $finalaccess"

            getAdminMemberDetailAmount(binding, memberId)
        }
        binding.memberState.setOnClickListener {
                showPopupMenu(it)
        }

        binding.saveBtn.setOnClickListener {
            val mDto = MemberDTO(memberId, statecode)
            saveBtnClick(mDto)
        }

    }

    //statecode 변경 팝업창
    private fun showPopupMenu(view: View) {

        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.admin_member_state_menu, popupMenu.menu)

        for (i in 0 until popupMenu.menu.size()) {
            popupMenu.menu.getItem(i).isEnabled = false
        }

        when (statecode) {
            "일반" -> {
                if(newStatecode == "강퇴"){
                    popupMenu.menu.findItem(R.id.menu_normal).isEnabled = true
                }else{
                    popupMenu.menu.findItem(R.id.menu_expel).isEnabled = true
                }
            }
            "탈퇴" -> {
                if(newStatecode == "재가입가능"){
                    popupMenu.menu.findItem(R.id.menu_withdraw).isEnabled = true
                }else{
                    popupMenu.menu.findItem(R.id.menu_rejoinable).isEnabled = true
                }
            }
            "강퇴" -> {
                if(newStatecode == "재가입가능"){
                    popupMenu.menu.findItem(R.id.menu_expel).isEnabled = true
                }else{
                    popupMenu.menu.findItem(R.id.menu_rejoinable).isEnabled = true
                }
            }
            "재가입" -> {
            }
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->

                when (menuItem.itemId) {
                    R.id.menu_expel -> {
                        // Handle 강퇴 option
                        statecodeChange = "4"
                        updateMemberStateUI("강퇴")
                        true
                    }
                    R.id.menu_rejoinable -> {
                        // Handle 재가입가능 option
                        statecodeChange = "3"
                        updateMemberStateUI("재가입가능")
                        true
                    }
                    R.id.menu_normal -> {
                        // Handle 일반 option
                        statecodeChange = "1"
                        updateMemberStateUI("일반")
                        true
                    }
                    R.id.menu_withdraw -> {
                        // Handle 탈퇴 option
                        statecodeChange = "2"
                        updateMemberStateUI("탈퇴")
                        true
                    }
                    else -> false
                }
        }

        popupMenu.show()
    }


    //menuItem클릭시 statecode 화면상 변경
    private fun updateMemberStateUI(statecodeChangeTwo: String) {
        newStatecode = statecodeChangeTwo
        binding.memberState.text = "회원상태 : $newStatecode"
        Log.d("AdminMemberDetail", "statecode: $statecode")
        Log.d("AdminMemberDetail", "newStatecode: $newStatecode")
        Log.d("AdminMemberDetail", "statecodeChange: $statecodeChange")
    }


    //meberDetailList 갯수
    private fun getAdminMemberDetailAmount(binding: FragmentAdminMemberDetailBinding, memberId: String) {
        val retrofit = AdminClient.retrofit
        Log.d("AdminMemberDetail", "memberId: ${memberId}")

        retrofit.create(IAdminService::class.java).getMemberDetailCnt(memberId)
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        amount = response.body() ?: 0
                        getAdminMemberDetailList(binding)
                        Log.d("AdminMemberDetail", "amount: ${response.body()}")
                    } else {
                        Log.d("AdminMemberDetail", "Failed to get amount: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("AdminMemberDetail", "Network request failed", t)
                }
            })
    }

    //memberDetailList 구하기
    private fun getAdminMemberDetailList(binding: FragmentAdminMemberDetailBinding) {
        val retrofit = AdminClient.retrofit
        val pageNum = 1

        var dataList: List<AdminMemberDetailData>

        retrofit.create(IAdminService::class.java).getMemberDetailList(memberId, pageNum, amount)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    if (response.code() == 404) {
                        Log.d("AdminMemberDetail", "400에러 : ${response}")
                    } else if (response.code() == 200) {
                        Log.d("AdminMemberDetail", "200성공 : ${response.body()}")
                        val memberDetailDtoList =
                            response.body()?.get("list") as? List<Map<String, Any>>
                        dataList = memberDetailDtoList?.mapNotNull {
                            val reportid = (it["reportid"] as? Double)?.toInt()
                            val reportcontent = it["reportcontent"] as? String
                            val reporter = it["reporter"] as? String
                            val reportdate = it["reportdate"] as? String
                            val rbrno = (it["rbrno"] as? Double)
                            val rfrno = (it["rfrno"] as? Double)
                            val rbid = (it["rbid"] as? Double)
                            if (reportid != null && reportcontent != null && reporter != null && reportdate != null) {
                                AdminMemberDetailData(
                                    reportid,
                                    reportcontent,
                                    reporter,
                                    rbrno!!,
                                    rfrno!!,
                                    rbid!!,
                                    reportdate
                                )
                            } else {
                                null
                            }
                        } ?: emptyList()

                        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                        binding.recyclerView.adapter = AdminMemberDetailAdapter(
                            dataList,
                            { reportId, reportContent, memberId, statecode, finalaccess -> navigateToDetailFragment(reportId, reportContent.toString(), memberId.toString(),
                                statecode.toString(),
                                finalaccess.toString()
                            ) },
                            { reportid, memberId -> approveClick(reportid, memberId) },
                            { reportid -> deleteBtnClick(reportid) }
                        )
                        binding.recyclerView.addItemDecoration(
                            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
                        )
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Log.d("AdminMemberDetail", "연결실패")
                }
            })
    }

    // 삭제 버튼 클릭시
    private fun deleteBtnClick(reportid: Int) {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).postMemberReportDelete(reportid)
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.code() == 200) {
                        Log.d("AdminMemberDetail", "200: ${response.body()}")
                        Toast.makeText(requireContext(), "${response.body()}번이 삭제 되었습니다.", Toast.LENGTH_SHORT).show()
                        getAdminMemberDetailList(binding)
                    } else {
                        Log.d("AdminMemberDetail", "Failed to delete question: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("postQuestionDelete", "Network request failed", t)
                }
            })
    }

    // 승인 버튼 클릭시(해당 user 신고누적갯수 확인)
    private fun approveClick(reportid: Int, memberId : String) {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).postMemberReportnumRead(reportid, memberId)
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.code() == 200) {
                        Log.d("AdminMemberDetail", "200: ${response.body()}")
                        val reportNum = response.body()
                        if (reportNum == 4) {
                            showConfirmationDialog(reportid, memberId)
                        }else{
                            handleExpulsion(reportid, memberId)
                        }
                    } else {
                        Log.d("AdminMemberDetail", "Failed to delete question: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("postQuestionDelete", "Network request failed", t)
                }
            })
    }

    //승인 버튼 클릭시 신고누적이 4회인경우 alret창 띄우기
    private fun showConfirmationDialog(reportid: Int, memberId : String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
            .setMessage("회원의 현재 신고 누적 갯수가 4회 입니다. 한 번 더 승인하시면 회원은 강퇴 처리되며 남은 신고들은 삭제됩니다. 승인 하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                handleExpulsion(reportid, memberId)
            }
            .setNegativeButton("취소") { _, _ ->
                Toast.makeText(requireContext(), "취소 되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    //해당 user reportnum count
    private fun handleExpulsion(reportid: Int, memberId : String) {
        val retrofit = AdminClient.retrofit
        Log.d("AdminMemberDetail", "handleExpulsion.reportid: ${reportid}, handleExpulsion.id: ${memberId}")
        retrofit.create(IAdminService::class.java).postMemberReportnumCnt(reportid, memberId)
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.code() == 200) {
                        Log.d("AdminMemberDetail", "200: ${response.body()}")
                        Toast.makeText(requireContext(), "${response.body()}번이 승인 되었습니다.", Toast.LENGTH_SHORT).show()
                        getAdminMemberDetailList(binding)
                    } else {
                        Log.d("AdminMemberDetail", "Failed to delete question: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("postQuestionDelete", "Network request failed", t)
                }
            })
    }

    // 저장 버튼 클릭시
    private fun saveBtnClick(mDto: MemberDTO) {
        mDto.statecode = statecodeChange
        mDto.id = memberId
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).postUpdateState(mDto)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.code() == 200) {
                        Log.d("AdminMemberDetail", "저장 200: ${response.body()}")
                        Toast.makeText(requireContext(), "변경사항이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        val fragmentManager = requireActivity().supportFragmentManager

                        val AdminMember = AdminMember.newInstance()
                        fragmentManager.beginTransaction()
                            .replace(R.id.container_admin, AdminMember)
                            .addToBackStack(null)
                            .commit()
                    } else {
                        Log.d("AdminMemberDetail", "Failed to delete question: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.d("postQuestionDelete", "Network request failed", t)
                }
            })
    }


    private fun navigateToDetailFragment(reportid: Int, reportcontent: String, memberId: String, statecode: String, finalaccess: String) {
        val detailFragment = AdminMemberReportDetail.newInstance(
            reportid,
            reportcontent,
            memberId,
            statecode,
            finalaccess
        )
        parentFragmentManager.beginTransaction()
            .replace(R.id.container_admin, detailFragment)
            .addToBackStack(null)
            .commit()
    }

}

class AdminMemberDetailAdapter(
    private val dataList: List<AdminMemberDetailData>,
    private val onItemClicked: (Int, Any?, Any?, Any?, Any?) -> Unit,
    private val onApproveClicked: (Int, String) -> Unit,
    private val onDeleteClicked: (Int) -> Unit
) : RecyclerView.Adapter<AdminMemberDetailAdapter.AdminMemberDetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminMemberDetailViewHolder {
        val binding = ItemAdminmemberdetailDataListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminMemberDetailViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: AdminMemberDetailViewHolder, position: Int) {
        val data = dataList[position]
        val binding = holder.binding
        holder.setData(data)

        binding.adminMemberTbody.setOnClickListener {
            onItemClicked(dataList[position].reportid, dataList[position].reportcontent, memberId, statecode, finalaccess)
        }
        binding.approveBtn.setOnClickListener {
            onApproveClicked(data.reportid, memberId)
        }
        binding.deleteBtn.setOnClickListener {
            onDeleteClicked(data.reportid)
        }
    }

    inner class AdminMemberDetailViewHolder(val binding: ItemAdminmemberdetailDataListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(data: AdminMemberDetailData) {
            binding.apply {
                reportid.text = data.reportid.toString()
                reportcontent.text = data.reportcontent
                reporter.text = data.reporter
                reportnumber.text = data.formattedReportnumber
                reportdate.text = data.reportdate
            }
        }
    }
}