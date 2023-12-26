package com.example.thefesta.admin.adminfesta.admin.adminmember

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.thefesta.R
import com.example.thefesta.databinding.FragmentAdminMemberReportDetailBinding
import com.example.thefesta.retrofit.AdminClient
import com.example.thefesta.service.IAdminService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminMemberReportDetail : Fragment() {

    private lateinit var binding: FragmentAdminMemberReportDetailBinding
    private var reportid: Int = 0
    private var reportcontent: String = ""
    private var memberId: String = ""
    private var statecode: String = ""
    private var finalaccess: String = ""

    companion object {
        private const val ARG_REPORTID = "arg_reportid"
        private const val ARG_REPORTCONTENT = "arg_reportcontent"
        private const val ARG_ID = "arg_id"
        private const val ARG_STATECODE = "arg_statecode"
        private const val ARG_FINALACCESS = "arg_finalaccess"

        fun newInstance(reportid: Int, reportcontent: String, memberId: String, statecode: String, finalaccess: String): AdminMemberReportDetail {
            val fragment = AdminMemberReportDetail()
            val args = Bundle()
            args.putInt(ARG_REPORTID, reportid)
            args.putString(ARG_REPORTCONTENT, reportcontent)
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

        binding = FragmentAdminMemberReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reportid = arguments?.getInt(ARG_REPORTID) ?: 0
        reportcontent = arguments?.getString(ARG_REPORTCONTENT).orEmpty()
        memberId = arguments?.getString(ARG_ID).orEmpty()
        statecode = arguments?.getString(ARG_STATECODE).orEmpty()
        finalaccess = arguments?.getString(ARG_FINALACCESS).orEmpty()

        if (reportid != 0 && reportcontent.isNotBlank() && memberId.isNotBlank()) {
            binding.adminFestaDetailReportContent.text = reportcontent
            binding.adminMemberDetailReport.text = "${reportid}번 신고내용"
        }

        //승인 버튼 클릭했을 때
        binding.adminMemberDetailReportApporveBtn.setOnClickListener {
            approveClick(reportid, memberId)
        }
       //삭제 버튼 클릭했을 때
        binding.adminMemberDetailReportDeleteBtn.setOnClickListener {
            deleteBtnClick(reportid)
        }
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


                        val fragmentManager = requireActivity().supportFragmentManager

                        val adminMemberDetail = AdminMemberDetail.newInstance(memberId, statecode, finalaccess)
                        fragmentManager.beginTransaction()
                            .replace(R.id.container_admin, adminMemberDetail)
                            .addToBackStack(null)
                            .commit()
                    } else {
                        Log.d("AdminMemberDetail", "Failed to delete question: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("postQuestionDelete", "Network request failed", t)
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

                        val fragmentManager = requireActivity().supportFragmentManager

                        val adminMemberDetail = AdminMemberDetail.newInstance(memberId, statecode, finalaccess)
                        fragmentManager.beginTransaction()
                            .replace(R.id.container_admin, adminMemberDetail)
                            .addToBackStack(null)
                            .commit()
                    } else {
                        Log.d("AdminMemberDetail", "Failed to delete question: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("postQuestionDelete", "Network request failed", t)
                }
            })
    }
}
