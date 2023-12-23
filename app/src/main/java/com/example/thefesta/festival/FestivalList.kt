package com.example.thefesta.festival

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thefesta.MainActivity
import com.example.thefesta.R
import com.example.thefesta.databinding.FragmentFestivalListBinding
import com.example.thefesta.model.festival.AreacodeDTO
import com.example.thefesta.model.festival.FestivalItemDTO
import com.example.thefesta.model.festival.FestivalResponse
import com.example.thefesta.model.festival.LikeDTO
import com.example.thefesta.retrofit.FestivalClient
import com.example.thefesta.service.IFestivalService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FestivalList : Fragment() {
    private lateinit var binding: FragmentFestivalListBinding
    private val festivalService: IFestivalService =
        FestivalClient.retrofit.create(IFestivalService::class.java)
    private val customAdapter = FestivalCustomAdapter()
    private lateinit var paginationLayout: LinearLayout
    private var page = 1
    private var isLoading = false
    private var isLastPage = false
    private val id = MainActivity.prefs.getString("id", "")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFestivalListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = GridLayoutManager(requireContext(), 2)
        layoutManager.orientation = GridLayoutManager.VERTICAL

        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = customAdapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()

                val totalCount = customAdapter?.itemCount?.minus(1)

                if (lastVisibleItemPosition == totalCount) {
                    loadNextPage()
                }
            }
        })

        fetchData(page = 1)

        initSearchView()
        
        binding.likeList.setOnClickListener {
            val festivalLikeListFragment = FestivalLikeList.newInstance()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, festivalLikeListFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun initSearchView() {
        binding.search.isSubmitButtonEnabled = false
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("searchValue", "query: ${query}")
                if (query.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "검색어를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("searchValue", "query: $query")
                    page = 1
                    fetchData(page, query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun loadNextPage() {
        page++
        fetchData(page = page, keyword = binding.search.query.toString())
    }

    private fun fetchData(page: Int, keyword: String? = null) {
        isLoading = true
        setLoading(true)

        val call: Call<FestivalResponse> =
            festivalService.getFestivalList(pageNum = page, amount = 9, keyword = keyword)

        call.enqueue(object : Callback<FestivalResponse> {
            override fun onResponse(
                call: Call<FestivalResponse>,
                response: Response<FestivalResponse>
            ) {
                if (response.isSuccessful) {
                    val festivalResponse: FestivalResponse? = response.body()
                    val festivalList: List<FestivalItemDTO>? = festivalResponse?.list
                    val areaCodeList: List<AreacodeDTO>? = festivalResponse?.areaCode

                    if (areaCodeList != null) {
                        customAdapter.setAreaCodeListCustom(areaCodeList)
                    }

                    festivalList?.let {
                        if (keyword != null) {
                            Log.d("festivalList", "${festivalList}")
                            Log.d("festivalList", "total : ${festivalResponse.pageMaker.total}")
                            customAdapter.addFestivalList(it, keyword)
                        } else {
                            customAdapter.addFestivalList(it)
                            customAdapter.notifyDataSetChanged()
                        }
                    }
                    festivalResponse?.let {
                        isLastPage = it.pageMaker?.endPage == it.pageMaker?.realEnd
                    }
                } else {
                    Log.e("FestivalList", "Failed to fetch data")
                }

                isLoading = false
                setLoading(false)
            }

            override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                Log.e("FestivalList", "Network request failed", t)
                t.printStackTrace()

                isLoading = false
                setLoading(false)
            }
        })

        customAdapter.setOnItemClickListener(object : FestivalCustomAdapter.OnItemClickListener {
            override fun onItemClick(festival: FestivalItemDTO) {
                val festivalDetailFragment = FestivalDetail.newInstance(festival.contentid)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.container, festivalDetailFragment)
                    .addToBackStack(null)
                    .commit()
            }

        })

        customAdapter.setOnLikeButtonClickListener(object : FestivalCustomAdapter.OnLikeButtonClickListener {
            override fun onLikeButtonClick(festival: FestivalItemDTO, position: Int) {
                Log.d("FestivalLikeBtn", "title : ${festival.title}, contentid : ${festival.contentid}")
                Log.d("FestivalLikeBtn", "likeStatus1 : ${festival.likeStatus}")

                val likeDTO = LikeDTO(
                    lno = 0,
                    contentid = festival.contentid,
                    id = id,
                    title = "",
                    firstimage = ""
                )

                if (id != "") {
                    if (festival.likeStatus) {
                        Log.d("FestivalLikeBtn", "Like DELETE")
                        val deleteCall = festivalService.likeDelete(likeDTO)

                        deleteCall.enqueue(object : Callback<String> {
                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                if (response.isSuccessful) {
                                    val result: String? = response.body()
                                    Log.d("FestivalLikeBtn", "Like DELETE Result: ${result}")
                                    customAdapter.toggleLikeState(position)
                                } else {
                                    Log.e("FestivalLikeBtn", "Failed to delete like")
                                }
                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Log.e("FestivalLikeBtn", "Network request failed", t)
                                t.printStackTrace()
                            }

                        })
                    } else {
                        val insertCall: Call<String> = festivalService.likeInsert(likeDTO)

                        insertCall.enqueue(object : Callback<String> {

                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                if (response.isSuccessful) {
                                    val result: String? = response.body()
                                    Log.d("FestivalLikeBtn", "Like INSERT Result: ${result}")
                                    customAdapter.toggleLikeState(position)
                                } else {
                                    Log.e("FestivalLikeBtn", "Failed to insert like")
                                }
                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Log.e("FestivalLikeBtn", "Network request failed", t)
                                t.printStackTrace()
                            }

                        })
                    }
                } else {
                    Toast.makeText(requireContext(), "좋아요는 회원만 가능합니다.  ", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun updateFestivalList(festivalResponse: FestivalResponse?) {
        customAdapter.festivalList = festivalResponse?.list as MutableList<FestivalItemDTO>?
        customAdapter.notifyDataSetChanged()
    }

    private fun setLoading(loading: Boolean) {
    }
}