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
import com.example.thefesta.admin.adminfesta.admin.adminquestion.AdminQuestionDetail
import com.example.thefesta.admin.adminfesta.admin.adminquestion.AdminQuestionRegister
import com.example.thefesta.databinding.FragmentAdminQuestionBinding
import com.example.thefesta.databinding.ItemAdminquestionDataListBinding
import com.example.thefesta.retrofit.AdminClient
import com.example.thefesta.service.IAdminService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class AdminQuestionData(
    val bid: Int,
    val btitle: String,
    val bcontent: String,
    val id: String,
    val bregist: String,
){
    val truncatedBregist: String = bregist.take(10)
}
class AdminQuestion : Fragment() {
    private lateinit var binding: FragmentAdminQuestionBinding
    private var amount: Int = 0

    companion object {
        fun newInstance(): AdminQuestion {
            val fragment = AdminQuestion()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminQuestionBinding.inflate(inflater)
        getAdminQuestionListCnt(binding)
        Log.d("AdminMember", "onCreateView 실행")
        return binding.root
    }

    //문의 list 갯수구하기
    private fun getAdminQuestionListCnt(binding: FragmentAdminQuestionBinding) {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).getAdminQuestionListCnt()
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        amount = response.body() ?: 0
                        Log.d("AdminQuestion", "amount: ${response.body()}")
                        getAdminQuestionList(binding)
                    } else {
                        Log.d("AdminQuestion", "Failed to get amount: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("AdminQuestion", "Network request failed", t)
                }
            })
    }


    //문의 list
    private fun getAdminQuestionList(binding: FragmentAdminQuestionBinding) {
        val retrofit = AdminClient.retrofit
        val pageNum = 1

        var dataList: List<AdminQuestionData>

        retrofit.create(IAdminService::class.java).getAdminQuestionList(pageNum, amount)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    if (response.code() == 404) {
                        Log.d("AdminQuestion", "400에러 : ${response}")
                    } else if (response.code() == 200) {
                        Log.d("AdminQuestion", "200성공 : ${response.body()}")

                        val adminQuestionList = response.body()?.get("list") as? List<Map<String, Any>>
                        Log.d("AdminQuestion", "adminQuestionList: ${adminQuestionList}")
                        dataList = adminQuestionList?.mapNotNull {
                            val bid = (it["bid"] as? Double)?.toInt()
                            val btitle = it["btitle"] as? String
                            val bcontent = it["bcontent"] as? String
                            val id = it["id"] as? String
                            val bregist = it["bregist"] as? String
                            if (bid != null && btitle != null && bcontent != null && id != null && bregist != null) {
                                AdminQuestionData(
                                    bid,
                                    btitle,
                                    bcontent,
                                    id,
                                    bregist
                                )
                            } else {
                                null
                            }
                        } ?: emptyList()
                        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                        binding.recyclerView.adapter = AdminQuestionAdapter(
                            dataList,
                            //this@AdminQuestion::navigateToDetailFragment,
                            { bid, bcontent -> navigateToDetailFragment(bid, bcontent) },
                            { bid -> adminRegisterBtn(bid) },
                            )
                        binding.recyclerView.addItemDecoration(
                            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
                        )

                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Log.d("AdminQuestion", "연결실패")
                }
            })
    }

    private fun adminRegisterBtn(bid: Int) {
        Log.d("AdminQuestion", "adminRegisterBtn 함수 실행")
        val detailFragment = AdminQuestionRegister.newInstance(bid)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container_admin, detailFragment)
            .addToBackStack(null)
            .commit()
    }


    private fun navigateToDetailFragment(bid: Int, bcontent: String) {
        val detailFragment = AdminQuestionDetail.newInstance(bid, bcontent)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container_admin, detailFragment)
            .addToBackStack(null)
            .commit()
    }


}

class AdminQuestionAdapter(
    private val dataList: List<AdminQuestionData>,
    private val onItemClicked: (Int,String) -> Unit,
    private val adminRegisterBtn: (Int) -> Unit,
) : RecyclerView.Adapter<AdminQuestionAdapter.AdminQuestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminQuestionViewHolder {
        val binding = ItemAdminquestionDataListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminQuestionViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: AdminQuestionViewHolder, position: Int) {
        val data = dataList[position]
        val binding = holder.binding
        holder.setData(data)

        binding.adminTbody.setOnClickListener {
            onItemClicked(dataList[position].bid, dataList[position].bcontent)
        }
        binding.questionRegister.setOnClickListener {
            adminRegisterBtn(dataList[position].bid)
        }
    }

    inner class AdminQuestionViewHolder(val binding: ItemAdminquestionDataListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(data: AdminQuestionData) {
            binding.apply {
                bid.text = data.bid.toString()
                btitle.text = data.btitle
                bcontent.text = data.bcontent
                id.text = data.id
                bregist.text = data.truncatedBregist
            }
        }
    }
}
