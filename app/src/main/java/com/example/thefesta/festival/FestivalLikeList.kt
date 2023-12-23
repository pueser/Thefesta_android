package com.example.thefesta.festival

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thefesta.MainActivity
import com.example.thefesta.R
import com.example.thefesta.databinding.FragmentFestivalLikeListBinding
import com.example.thefesta.model.festival.FestivalLikeListResponse
import com.example.thefesta.model.festival.LikeDTO
import com.example.thefesta.model.festival.PageDTO
import com.example.thefesta.retrofit.FestivalClient
import com.example.thefesta.service.IFestivalService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FestivalLikeList : Fragment() {
    private lateinit var binding: FragmentFestivalLikeListBinding
    private val festivalService: IFestivalService = FestivalClient.retrofit.create(IFestivalService::class.java)
    private lateinit var paginationLayout: LinearLayout
    private var customAdapter = FestivalLikeListAdapter(emptyList())
    private val id = MainActivity.prefs.getString("id", "")
    private var page = 1
    private var setPage = 1
    private var endPage = 1
    private var total = 1
    var selectedContentIds = customAdapter.getSelectedContentIds()
    var likeListSize = 0
    var allBtn = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFestivalLikeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        paginationLayout = view.findViewById(R.id.likeListPaginationLayout)

        likeList(id, page)
    }

    private fun likeList(id: String, page: Int) {
        val likeListCall: Call<FestivalLikeListResponse> = festivalService.getLikeList(page = page, id = id)
        likeListCall.enqueue(object : Callback<FestivalLikeListResponse> {
            override fun onResponse(
                call: Call<FestivalLikeListResponse>,
                response: Response<FestivalLikeListResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d("FestivalLikeList", "좋아요 목록 가져오기 성공")
                    val likeListResponse: FestivalLikeListResponse? = response.body()
                    val likeList: List<LikeDTO>? = likeListResponse?.list
                    Log.d("FestivalLikeList", "좋아요 목록 : ${likeList}")
                    Log.d("FestivalLikeList", "페이지 : ${likeListResponse?.pageMaker}")

                    likeListSize = likeList!!.size

                    endPage = likeListResponse?.pageMaker?.realEnd ?:1
                    total = likeListResponse?.pageMaker?.total ?:1

                    setupPagination(likeListResponse?.pageMaker)
                    showReplies(likeList)

                    customAdapter.setOnFestivalInfoClickListener(object : FestivalLikeListAdapter.OnFestivalInfoClickListener {
                        override fun onFestivalInfoClick(contentid: String) {
                            val festivalDetailFragment = FestivalDetail.newInstance(contentid)
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.container, festivalDetailFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    })

                    binding.likeListAllCheckBox.setOnClickListener{
                        var allBox = binding.likeListAllCheckBox.isChecked

                        if (allBox) {
                            allBtn = true
                        } else {
                            allBtn = false
                        }

                        allCheck(allBox)
                    }

                    binding.likeListAllCheckBtn.setOnClickListener{
                        if (allBtn) {
                            allBtn = false
                            binding.likeListAllCheckBox.isChecked = false
                        } else {
                            allBtn = true
                            binding.likeListAllCheckBox.isChecked = true
                        }
                        allCheck(allBtn)
                    }

                    customAdapter.setOnLikeCheckBoxClickListener(object : FestivalLikeListAdapter.OnLikeCheckBoxClickListener {
                        override fun onLikeCheckBoxClick() {

                            selectedContentIds = customAdapter.getSelectedContentIds()

                            if (likeList.size == selectedContentIds.size) {
                                binding.likeListAllCheckBox.isChecked = true
                            } else {
                                binding.likeListAllCheckBox.isChecked = false
                            }

                            Log.d("FestivalLikeList", "좋아요 setOnLikeCheckBoxClickListener 선택 목록 : ${selectedContentIds}")
                        }
                    })

                    binding.likeListDeleteBtn.setOnClickListener {
                        likeDelete(selectedContentIds)

                    }

                    if (likeListSize <= 0) {
                        Log.d("FestivalLikeList", "likeListSize : ${likeListSize}")
                        binding.replyRecyclerView.visibility = View.GONE
                        binding.likeListEmpty.visibility = View.VISIBLE
                    } else {
                        Log.d("FestivalLikeList", "likeListSize : ${likeListSize}")
                        binding.replyRecyclerView.visibility = View.VISIBLE
                        binding.likeListEmpty.visibility = View.GONE
                    }
                } else {
                    Log.d("FestivalLikeList", "좋아요 목록 가져오기 실패")
                }
            }

            override fun onFailure(call: Call<FestivalLikeListResponse>, t: Throwable) {
                Log.d("FestivalLikeList", "좋아요 목록 가져오기 실패")
                t.printStackTrace()
            }

        })
    }

    private fun allCheck(result: Boolean) {
        if (result) {
            customAdapter.selectAll(true)
            selectedContentIds = customAdapter.getSelectedContentIds()
        } else {
            customAdapter.selectAll(false)
            selectedContentIds = customAdapter.getSelectedContentIds()
        }
    }

    private fun likeDelete(contentids: List<String>) {
        contentids.forEach { contentid ->
            val likeDTO = LikeDTO(
                lno = 0,
                contentid = contentid,
                id = id,
                title = "",
                firstimage = ""
            )

            Log.d("FestivalLikeList", "Like DELETE")
            val deleteCall = festivalService.likeDelete(likeDTO)
            deleteCall.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val result: String? = response.body()
                        Log.d("FestivalLikeList", "Like DELETE Result: ${result}")

                        if (likeListSize == contentids.size && setPage > 1) {
                            Log.d("likeListNum", "true likeListSize : ${likeListSize}, setPage : ${setPage}")
                            likeList(id, page)
                        } else {
                            Log.d("likeListNum", "false ${likeListSize}")
                            likeList(id, setPage)
                        }
                    } else {
                        Log.e("FestivalLikeList", "Failed to delete like")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("FestivalLikeBtn", "Network request failed", t)
                    t.printStackTrace()
                }
            })
        }
    }

    private fun setupPagination(pageMaker: PageDTO?) {
        paginationLayout.removeAllViews()

        pageMaker?.let {
            for (num in it.startPage..it.endPage) {
                val button = Button(requireContext())
                button.text = num.toString()
                button.setOnClickListener { _ ->
                    handlePageChange(num)
                }
                Log.d("Pagination", "Adding button for page $num")
                paginationLayout.addView(button)
            }
        }
    }

    private fun handlePageChange(pageNum: Int) {
        binding.likeListAllCheckBox.isChecked = false
        setPage = pageNum
        Log.d("likeListNum", "page : ${setPage}")
        likeList(id, pageNum)
    }

    private fun showReplies(likeList: List<LikeDTO>?) {
        customAdapter = FestivalLikeListAdapter(likeList ?: emptyList())

        val recyclerView: RecyclerView = binding.replyRecyclerView
        recyclerView.adapter = customAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        customAdapter.notifyDataSetChanged()
    }

    companion object {
        fun newInstance() =
            FestivalLikeList().apply {
                arguments = Bundle().apply {

                }
            }
    }
}