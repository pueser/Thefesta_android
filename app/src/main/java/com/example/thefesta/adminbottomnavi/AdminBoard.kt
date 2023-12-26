package com.example.thefesta.adminbottomnavi

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thefesta.databinding.FragmentAdminBoardBinding
import com.example.thefesta.databinding.ItemAdminboardDataListBinding
import com.example.thefesta.retrofit.AdminClient
import com.example.thefesta.service.IAdminService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


data class AdminBoardData(
    val bno: Int,
    val bid: Int,
    val btitle: String,
    val id: String,
    val bregist: String
) {
    val truncatedBno: Int = bno.toDouble().toInt()

    val boardType: String = when (truncatedBno) {
        1 -> "자유게시판"
        2 -> "리뷰게시판"
        else -> "알 수 없는 게시판"
    }

    val truncatedBid: Int = bid.toDouble().toInt()
    val truncatedBregist: String = bregist.take(10)

}

class AdminBoard : Fragment() {
    private lateinit var binding: FragmentAdminBoardBinding
    private var amount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminBoardBinding.inflate(inflater, container, false)
        getAdminBoardAmount()
        Log.d("AdminBoard", "onCreateView 실행")
        return binding.root
    }

    //게시판 list 갯수
    private fun getAdminBoardAmount() {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).getBoardListCnt()
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        amount = response.body() ?: 0
                        getAdminBoradList()
                        Log.d("AdminBoard", "amount: ${response.body()}")
                    } else {
                        Log.d("AdminBoard", "Failed to get amount: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("AdminBoard", "Network request failed", t)
                }
            })
    }

    //게시판 list불러오기
    private fun getAdminBoradList() {
        val retrofit = AdminClient.retrofit
        val pageNum = 1

        retrofit.create(IAdminService::class.java).getBoardList(pageNum, amount)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    if (response.code() == 404) {
                        Log.d("AdminBoard", "400에러 : ${response}")
                    } else if (response.code() == 200) {
                        Log.d("AdminBoard", "200성공 : ${response.body()}")

                        val boardDtoList = response.body()?.get("list") as? List<Map<String, Any>>
                        val dataList = boardDtoList?.mapNotNull {
                            val bno = (it["bno"] as? Double)?.toInt()
                            val bid = (it["bid"] as? Double)?.toInt()
                            val btitle = it["btitle"] as? String
                            val id = it["id"] as? String
                            val bregist = it["bregist"] as? String

                            if (bno != null && bid != null && btitle != null && id != null && bregist != null) {
                                AdminBoardData(
                                    bno,
                                    bid,
                                    btitle,
                                    id,
                                    bregist
                                )
                            } else {
                                null
                            }
                        } ?: emptyList()
                        setupRecyclerView(dataList)
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Log.d("AdminBoard", "연결실패")
                }
            })
    }


    // 삭제 버튼 클릭시
    fun deleteClick(bid: Int) {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).postBoardDelete(bid)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>

                ) {
                    if (response.isSuccessful) {
                        Log.d("AdminBoard", "200성공 : ${response.body()}")
                        Toast.makeText(requireContext(), "${bid}번 게시글이 삭제 되었습니다.", Toast.LENGTH_SHORT).show()
                        getAdminBoradList()

                    } else {
                        Log.d("AdminBoard", "400에러 : ${response}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d("AdminBoard", "연결실패",t)
                }
            })
    }

    private fun setupRecyclerView(dataList: List<AdminBoardData>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = AdminBoardAdapter(dataList, this@AdminBoard::deleteClick)
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        )
    }
}

class AdminBoardAdapter(
    private val dataList: List<AdminBoardData>,
    private val onDeleteClicked: (Int) -> Unit,
) : RecyclerView.Adapter<AdminBoardAdapter.AdminBoardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminBoardViewHolder {
        val binding = ItemAdminboardDataListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminBoardViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: AdminBoardViewHolder, position: Int) {
        val data = dataList[position]
        val binding = holder.binding
        holder.setData(data)

        binding.deleteClickBtn.setOnClickListener {
            Log.d("AdminBoard", "삭제버튼 클릭함")
            onDeleteClicked(data.bid)
        }
    }

    inner class AdminBoardViewHolder(val binding: ItemAdminboardDataListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(data: AdminBoardData) {
            binding.apply {
                bno.text = data.boardType
                bid.text = data.truncatedBid.toString()
                btitle.text = data.btitle
                id.text = data.id
                bregist.text = data.truncatedBregist
            }
        }
    }
}
