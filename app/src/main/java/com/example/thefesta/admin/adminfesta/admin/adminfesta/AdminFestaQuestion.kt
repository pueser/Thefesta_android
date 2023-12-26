package com.example.thefesta.admin.adminfesta.admin.adminfesta

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
import com.example.thefesta.databinding.FragmentAdminFestaQuestionBinding
import androidx.fragment.app.FragmentManager


import com.example.thefesta.databinding.ItemAdminfestaquestionDataListBinding

import com.example.thefesta.retrofit.AdminClient
import com.example.thefesta.service.IAdminService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class FestivalQuestionData(
    val questionid: String,
    val questioncontent: String,
    val id: String,
    val questiondate: String
){
    val formattedQuestionid: String
        get() = when {
            questionid == "0.0" -> "0번"
            else -> "${questionid.toDouble().toInt()}번"
        }
}
private var contentId: String = ""

class AdminFestaQuestion : Fragment() {
    private lateinit var binding: FragmentAdminFestaQuestionBinding
    private lateinit var itembinding: ItemAdminfestaquestionDataListBinding
    private var amount: Int = 0

    companion object {
        private const val ARG_CONTENT_ID = "arg_content_id"

        fun newInstance(contentId: String): AdminFestaQuestion {
            val fragment = AdminFestaQuestion()
            val args = Bundle()
            args.putString(ARG_CONTENT_ID, contentId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminFestaQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentId = arguments?.getString(ARG_CONTENT_ID).orEmpty()
        if (contentId.isNotBlank()) {
            getAdminFestivalAmount(contentId)
        }


    }

    //해당 축제 건의 갯수 불러오기
    private fun getAdminFestivalAmount(contentId: String) {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).getquestionListAmount(contentId)
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        amount = response.body() ?: 0
                        getQuestionList()
                    } else {
                        Log.d("adminFestivalCnt", "Failed to get amount: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("adminFestivalCnt", "Network request failed", t)
                }
            })
    }

    //해당 축제 건의 list 불러오기
    private fun getQuestionList() {
        val retrofit = AdminClient.retrofit
        val pageNum = 1
        Log.d("adminQuestion", "amount = ${amount}")
        retrofit.create(IAdminService::class.java).getquestionList(pageNum, amount, contentId)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    if (response.isSuccessful) {
                        Log.d("adminQuestion", "200성공 = ${response.body()}")

                        val responseData = response.body()
                        val questionDtoList = responseData?.get("list") as? List<Map<String, Any>>
                        val dataList = questionDtoList?.mapNotNull {
                            val questionid = it["questionid"]?.toString()
                            val questioncontent = it["questioncontent"]?.toString()
                            val id = it["id"]?.toString()
                            val questiondate = it["questiondate"]?.toString()
                            if (questionid != null && questioncontent != null && id != null && questiondate != null) {
                                FestivalQuestionData(questionid, questioncontent, id, questiondate)

                            } else {
                                null
                            }
                        } ?: emptyList()
                        Log.d("adminQuestion", "dataList = ${dataList}")
                        setupRecyclerView(dataList)
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Log.d("adminQuestion", "연결실패")
                }
            })
    }




    private fun setupRecyclerView(dataList: List<FestivalQuestionData>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter =
            AdminFestaQuestionAdapter(dataList, parentFragmentManager,
                { questioncontent, questionid, contentId ->
                    navigateToDetailFragment(questioncontent, questionid.toString(), contentId.toString())
                },
                { questionId -> CheackBtnClick(questionId) }

            )
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
    }

    private fun navigateToDetailFragment(questioncontent: String, questionid: String, contentId : String) {
        val detailFragment =
            AdminFestaQuestionDetail.newInstance(questioncontent, questionid, contentId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container_admin, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    // 확인 버튼 클릭시
    private fun CheackBtnClick(questionId: String) {
        Log.d("CheackBtnClick", "questionId: ${questionId.toDouble().toInt()}")
        val retrofit = AdminClient.retrofit
        retrofit.create(IAdminService::class.java).postQuestionDelete(questionId.toDouble().toInt().toString())
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.code() == 200) {
                        Log.d("CheackBtnClick", "200: ${response.body()}")
                        val responseText = response.body()
                        if (responseText != null) {
                            val intValue = responseText.toInt()
                            Toast.makeText(requireContext(), "${intValue}번이 확인 되었습니다.", Toast.LENGTH_SHORT).show()
                            val fragmentManager = requireActivity().supportFragmentManager

                            val adminFestaQuestionFragment = newInstance(contentId)
                            fragmentManager.beginTransaction()
                                .replace(R.id.container_admin, adminFestaQuestionFragment)
                                .addToBackStack(null)
                                .commit()

                        } else {
                            Log.d("postQuestionDelete", "Response body is null")
                        }
                    } else {
                        Log.d("postQuestionDelete", "Failed to delete question: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("postQuestionDelete", "Network request failed", t)
                }
            })
    }


}

class AdminFestaQuestionAdapter(
    private val dataList: List<FestivalQuestionData>,
    private val fragmentManager: FragmentManager,
    private val onItemClicked: (String, Any?, Any?) -> Unit,
    private val CheackBtnClick: (String) -> Unit
) : RecyclerView.Adapter<AdminFestaQuestionAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemAdminfestaquestionDataListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = dataList[position]
        holder.setData(data)
    }

    inner class MyViewHolder(val binding: ItemAdminfestaquestionDataListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.adminFestaQuestionTbody.setOnClickListener {
                onItemClicked(dataList[adapterPosition].questioncontent, dataList[adapterPosition].questionid, contentId)
            }
            binding.festaCheckBtn.setOnClickListener {
                val questionId = dataList[adapterPosition].questionid
                CheackBtnClick(questionId)
            }
        }

        fun setData(data: FestivalQuestionData) {
            Log.d("adminQuestion", "data = $data")
            binding.apply {
                festaQuestionNumber.text = data.formattedQuestionid
                festaQuestionContent.text = data.questioncontent
                festaQuestionWriter.text = data.id
                festaQuestionDay.text = data.questiondate
            }
        }
    }
}