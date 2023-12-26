package com.example.thefesta.adminbottomnavi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thefesta.R
import com.example.thefesta.admin.adminfesta.admin.adminreport.AdminReportDetail
import com.example.thefesta.databinding.FragmentAdminReportBinding
import com.example.thefesta.databinding.ItemAdminreportDataListBinding
import com.example.thefesta.retrofit.AdminClient
import com.example.thefesta.service.IAdminService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


data class AdminReportData(
    val reportid: Int,
    val reportcontent: String,
    val reporter: String,
    val reportnumber: String,
    val reportdate: String
)
class AdminReport : Fragment() {
    private lateinit var binding: FragmentAdminReportBinding
    private var amount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminReportBinding.inflate(inflater, container, false)
        Log.d("AdminReport", "onCreateView 실행")
        getAdminReportAmount()
        return binding.root

    }

    //신고 list 갯수
    private fun getAdminReportAmount() {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).getReportListCnt()
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        amount = response.body() ?: 0
                        getAdminReportList()
                        Log.d("AdminReport", "amount: ${response.body()}")
                    } else {
                        Log.d("AdminReport", "Failed to get amount: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("AdminReport", "Network request failed", t)
                }
            })
    }


    //신고 list불러오기
    private fun getAdminReportList() {
        val retrofit = AdminClient.retrofit
        val pageNum = 1

        retrofit.create(IAdminService::class.java).adminReportList(pageNum, amount)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    if (response.code() == 404) {
                        Log.d("AdminReport", "400에러 : ${response}")
                    } else if (response.code() == 200) {
                        Log.d("AdminReport", "200성공 : ${response.body()}")

                        val ReportDtoList = response.body()?.get("list") as? List<Map<String, Any>>
                        val dataList = ReportDtoList?.mapNotNull {
                            val reportid = (it["reportid"] as? Double)?.toInt()
                            val reportcontent = it["reportcontent"] as? String
                            val reporter = it["reporter"] as? String
                            val reportnumber = it["reportnumber"] as? String
                            val reportdate = it["reportdate"] as? String

                            if (reportid != null && reportcontent != null && reporter != null && reportnumber != null && reportdate != null) {
                                AdminReportData(
                                    reportid,
                                    reportcontent,
                                    reporter,
                                    reportnumber,
                                    reportdate
                                )
                            } else {
                                null
                            }
                        } ?: emptyList()
                        setupRecyclerView(dataList)
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Log.d("AdminReport", "연결실패")
                }
            })
    }


    // 승인 버튼 클릭시
    fun approveClick(reportid: Int) {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).postReportstateChange(reportid)
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "${response.body()}번이 승인 되었습니다.", Toast.LENGTH_SHORT).show()
                        getAdminReportList()
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
                        getAdminReportList()
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

    private fun setupRecyclerView(dataList: List<AdminReportData>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter =
            AdminReportAdapter(
                dataList,
                { reportcontent, reportid -> navigateToDetailFragment(reportcontent, reportid) },
                { reportid -> approveClick(reportid) },
                { reportid -> deleteClick(reportid) }
            )

        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        )
    }

    private fun navigateToDetailFragment(reportcontent: String, reportid: Int) {
        val detailFragment =
            AdminReportDetail.newInstance(reportcontent, reportid)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container_admin, detailFragment)
            .addToBackStack(null)
            .commit()
    }

}


class AdminReportAdapter(
    private val dataList: List<AdminReportData>,
    private val onItemClicked: (String, Int) -> Unit,
    private val onApproveClicked: (Int) -> Unit,
    private val onDeleteClicked: (Int) -> Unit,
) : RecyclerView.Adapter<AdminReportAdapter.AdminReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminReportViewHolder {
        val binding = ItemAdminreportDataListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminReportViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: AdminReportViewHolder, position: Int) {
        val data = dataList[position]
        val binding = holder.binding
        holder.setData(data)

        binding.adminReportTbody.setOnClickListener {
            onItemClicked(dataList[position].reportcontent, dataList[position].reportid)
        }
        binding.approveClickBtn.setOnClickListener {
            onApproveClicked(data.reportid)
        }
        binding.deleteClickBtn.setOnClickListener {
            onDeleteClicked(data.reportid)
        }

    }

    inner class AdminReportViewHolder(val binding: ItemAdminreportDataListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(data: AdminReportData) {
            binding.apply {
                reportid.text = data.reportid.toString()
                reportcontent.text = data.reportcontent
                reporter.text = data.reporter
                reportnumber.text = data.reportnumber
                reportdate.text = data.reportdate
            }
        }
    }
}