package com.example.thefesta.adminbottomnavi

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
import com.example.thefesta.admin.adminfesta.admin.adminmember.AdminMemberDetail
import com.example.thefesta.databinding.FragmentAdminMemberBinding
import com.example.thefesta.databinding.ItemAdminmemberDataListBinding
import com.example.thefesta.retrofit.AdminClient
import com.example.thefesta.service.IAdminService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class AdminMemberData(
    val id: String,
    val statecode: String,
    val reportnum: Int,
    val finalaccess: String,
    val totalreportnum: Int,
    val rn: Int
){
    val formattedStatecode: String
        get() =when (statecode) {
            "1" -> "일반"
            "2" -> "탈퇴"
            "3" -> "재가입가능"
            "4" -> "강퇴"
            else -> "Unknown"
        }
}
class AdminMember : Fragment() {
    private lateinit var binding: FragmentAdminMemberBinding
    private var amount: Int = 0

    companion object {

        fun newInstance(): AdminMember {
            val fragment = AdminMember()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminMemberBinding.inflate(inflater)
        getAdminMemberAmount(binding)
        Log.d("AdminMember", "onCreateView 실행")
        return binding.root
    }

    //회원 총원 구하기
    private fun getAdminMemberAmount(binding: FragmentAdminMemberBinding) {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).getMemberListCnt()
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        amount = response.body() ?: 0
                        Log.d("AdminMember", "amount: ${response.body()}")
                        getAdminFestivalList(binding)
                    } else {
                        Log.d("AdminMember", "Failed to get amount: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("AdminMember", "Network request failed", t)
                }
            })
    }

    //회원 list
    private fun getAdminFestivalList(binding: FragmentAdminMemberBinding) {
        val retrofit = AdminClient.retrofit
        val pageNum = 1

        var dataList: List<AdminMemberData>

        retrofit.create(IAdminService::class.java).adminMemberList(pageNum, amount)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    if (response.code() == 404) {
                        Log.d("AdminMember", "400에러 : ${response}")
                    } else if (response.code() == 200) {
                        Log.d("AdminMember", "200성공 : ${response.body()}")

                        val memberDtoList = response.body()?.get("list") as? List<Map<String, Any>>
                        Log.d("AdminMember", "memberDtoList: ${memberDtoList}")
                        dataList = memberDtoList?.mapNotNull {
                            val id = it["id"] as? String
                            val statecode = it["statecode"] as? String
                            val reportnum = (it["reportnum"] as? Double)?.toInt()
                            val finalaccess = it["finalaccess"] as? String
                            val totalreportnum = (it["totalreportnum"] as? Double)?.toInt()
                            val rn = (it["rn"] as? Double)?.toInt()
                            if (id != null && statecode != null && reportnum != null && finalaccess != null && totalreportnum != null && rn != null) {
                                AdminMemberData(
                                    id,
                                    statecode,
                                    reportnum,
                                    finalaccess,
                                    totalreportnum,
                                    rn
                                )
                            } else {
                                null
                            }
                        } ?: emptyList()
                        Log.d("AdminMember", "DataList size: ${dataList.size}")
                        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                        binding.recyclerView.adapter = AdminMemberAdapter(dataList, this@AdminMember::navigateToDetailFragment)
                        binding.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
                        )

                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Log.d("AdminMember", "연결실패")
                }
            })
    }

    private fun navigateToDetailFragment(id: String, statecode: String, finalaccess: String) {
        val detailFragment = AdminMemberDetail.newInstance(id, statecode, finalaccess)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container_admin, detailFragment)
            .addToBackStack(null)
            .commit()
    }

}


class AdminMemberAdapter(
private val dataList: List<AdminMemberData>,
private val onItemClicked: (String,String,String) -> Unit,
) : RecyclerView.Adapter<AdminMemberAdapter.AdminMemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminMemberViewHolder {
        val binding = ItemAdminmemberDataListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminMemberViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: AdminMemberViewHolder, position: Int) {
        val data = dataList[position]
        val binding = holder.binding
        holder.setData(data)

        binding.adminTbody.setOnClickListener {
            onItemClicked(dataList[position].id, dataList[position].formattedStatecode, dataList[position].finalaccess)
        }

    }

    inner class AdminMemberViewHolder(val binding: ItemAdminmemberDataListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(data: AdminMemberData) {
            binding.apply {
                rn.text = data.rn.toString()
                id.text = data.id
                statecode.text = data.formattedStatecode
                totalreportnum.text = data.reportnum.toString()
                reportnum.text = data.totalreportnum.toString()
                finalaccess.text = data.finalaccess
            }
        }
    }
}