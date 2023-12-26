package com.example.thefesta.admin.adminfesta.admin.adminreport

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.thefesta.R
import com.example.thefesta.adminbottomnavi.AdminReport
import com.example.thefesta.databinding.FragmentAdminReportDetailBinding
import com.example.thefesta.retrofit.AdminClient
import com.example.thefesta.service.IAdminService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminReportDetail : Fragment(){
    private lateinit var binding: FragmentAdminReportDetailBinding
    private var reportcontent: String = ""
    private var reportid: Int = 0

    companion object {
        private const val ARG_reportcontent = "arg_reportcontent"
        private const val ARG_reportid = "arg_reportid"

        fun newInstance(reportcontent: String, reportid : Int): AdminReportDetail {
            val fragment = AdminReportDetail()
            val args = Bundle()
            args.putString(ARG_reportcontent, reportcontent)
            args.putInt(ARG_reportid, reportid)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAdminReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reportcontent = arguments?.getString(ARG_reportcontent).orEmpty()
        reportid = arguments?.getInt(ARG_reportid) ?: 0


        if (reportcontent.isNotBlank() && reportid != 0) {
            binding.adminReportDetailContent.text = reportcontent
            binding.adminReportDetail.text = "${reportid}번 신고내용"

        }
        //승인하기 버튼 클릭했을 때
        binding.adminReportApporveCheackBtn.setOnClickListener {
            approveClick(reportid)
            val fragmentManager = requireActivity().supportFragmentManager

            val adminReportFragment = AdminReport()
            fragmentManager.beginTransaction()
                .replace(R.id.container_admin, adminReportFragment)
                .addToBackStack(null)
                .commit()
        }
        //반려하기 버튼 클릭했을 때
        binding.adminReportDeleteBtn.setOnClickListener {
            deleteClick(reportid)
            val fragmentManager = requireActivity().supportFragmentManager

            val adminReportFragment = AdminReport()
            fragmentManager.beginTransaction()
                .replace(R.id.container_admin, adminReportFragment)
                .addToBackStack(null)
                .commit()
        }
    }


    // 승인 버튼 클릭시
    fun approveClick(reportid: Int) {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).postReportstateChange(reportid)
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "${response.body()}번이 승인 되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("AdminReport", "Failed to delete festival: ${response.code()}")
                        Toast.makeText(requireContext(), "Failed to delete Report", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("AdminReport", "Network request failed", t)
                }
            })
    }

    // 반려 버튼 클릭시
    fun deleteClick(reportid: Int) {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).postMemberReportDelete(reportid)
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "${response.body()}번이 반려 되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("AdminReport", "Failed to delete festival: ${response.code()}")
                        Toast.makeText(requireContext(), "Failed to delete Report", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("AdminReport", "Network request failed", t)
                }
            })
    }
}

