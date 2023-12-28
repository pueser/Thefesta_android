package com.example.thefesta.festival

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thefesta.MainActivity
import com.example.thefesta.R
import com.example.thefesta.databinding.FragmentFestivalListBinding
import com.example.thefesta.model.festival.AreacodeDTO
import com.example.thefesta.model.festival.FestivalItemDTO
import com.example.thefesta.model.festival.FestivalResponse
import com.example.thefesta.model.festival.FestivalViewModel
import com.example.thefesta.model.festival.LikeDTO
import com.example.thefesta.model.festival.PageDTO
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
    private var keyString: String? = null
    private var page = 1
    private var total = 1
    private var isLoading = false
    private var isLastPage = false
    private val id = MainActivity.prefs.getString("id", "")
    private val festivalViewModel: FestivalViewModel by viewModels()

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

        val layoutManager = GridLayoutManager(requireContext(), 2) // 한 줄에 2개의 아이템을 나열
        layoutManager.orientation = GridLayoutManager.VERTICAL

        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = customAdapter

        // 페이지네이션을 담을 LinearLayout 초기화
        paginationLayout = view.findViewById(R.id.festivalListPaginationLayout)

        // 초기에 첫 번째 페이지를 불러옵니다.
//        fetchData(page = 1)

        if (festivalViewModel.currentPage != null) {
            page = festivalViewModel.currentPage
        } else {
            // 초기 페이지 설정
            page = 1
        }

        fetchData(page)

        initSearchView()

        createAndAddButton("이전", page - 1) {
            if (page > 1) {
                loadPage(page - 1)
            }
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
                    keyString = query
                    page = 1
                    fetchData(page, keyString)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return true
            }
        })
    }

    private fun loadPage(page: Int) {
        fetchData(page, keyword = binding.search.query.toString())
    }

    // 페이징 버튼 생성 및 추가를 위한 메서드
    private fun createAndAddButton(text: String, targetPage: Int, onClickListener: () -> Unit) {
        val button = Button(requireContext())
        button.text = text
        button.setOnClickListener { onClickListener.invoke() }

        // 버튼 크기 직접 지정
        val layoutParams = LinearLayout.LayoutParams(100, 100)

        // 마지막 버튼이 아닌 경우에만 마진 적용
        if (paginationLayout.childCount < total) {
            layoutParams.setMargins(0, 0, 30, 0)  // 원하는 마진 값으로 수정
        }

        button.layoutParams = layoutParams

        // 버튼의 글씨 크기 설정
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)  // 원하는 크기로 수정

        // 백그라운드 설정
        if (page == targetPage) {
            // 현재 페이지일 때 다른 백그라운드 적용 (예: drawable/festival_current_page_btn.xml)
            button.setBackgroundResource(R.drawable.festival_pagination_sel_btn)
        } else {
            // 다른 페이지일 때의 백그라운드 적용 (예: drawable/festival_pagination_btn.xml)
            button.setBackgroundResource(R.drawable.festival_pagination_btn)
        }

        paginationLayout.addView(button)
    }

    private fun handlePageChange(targetPage: Int) {
        page = targetPage
        festivalViewModel.currentPage = targetPage
        // 페이지 변경 시 동작을 정의합니다.
        fetchData(targetPage, keyword = binding.search.query.toString())
    }

    private fun setupPagination(pageMaker: PageDTO?) {
        paginationLayout.removeAllViews()

        pageMaker?.let {
            if (pageMaker.prev) {
                // "이전" 버튼 추가
                createAndAddButton("<", 0) { handlePageChange(it.startPage - 1) }
            }

            // 페이지 번호 버튼 추가
            for (num in it.startPage..it.endPage) {
                createAndAddButton(num.toString(), num) { handlePageChange(num) }
            }

            if (pageMaker.next) {
                // "다음" 버튼 추가
                createAndAddButton(">", 0) { handlePageChange(it.endPage + 1) }
            }
        }
    }

    private fun fetchData(page: Int, keyword: String? = null) {
        Log.d("ddddddf", "page : ${page}, currentPage : ${festivalViewModel.currentPage}")
        isLoading = true
        setLoading(true)

        val call: Call<FestivalResponse> =
            festivalService.getFestivalList(pageNum = page, amount = 10, keyword = keyword)

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

                    if (festivalList != null) {
                        if (festivalList.size <= 0){
                            binding.searchResult.visibility = View.VISIBLE
                        }
                    }

                    total = festivalResponse?.pageMaker?.total ?:1

                    updateFestivalList(festivalResponse)
                    setupPagination(festivalResponse?.pageMaker)

                    // 마지막 페이지 여부 체크
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
        if (festivalResponse != null) {
            Log.d("dfdf", "${festivalResponse.list}")
            Log.d("dfdf", "${festivalResponse.list?.size}")
        }
        customAdapter.festivalList = festivalResponse?.list
        customAdapter.notifyDataSetChanged()
    }

    private fun setLoading(loading: Boolean) {
    }
}