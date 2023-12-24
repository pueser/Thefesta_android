package com.example.thefesta.festival

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.thefesta.MainActivity
import com.example.thefesta.R
import com.example.thefesta.databinding.FragmentFestivalDetailBinding
import com.example.thefesta.food.FoodList
import com.example.thefesta.model.festival.FestivalImgItemDTO
import com.example.thefesta.model.festival.FestivalItemDTO
import com.example.thefesta.model.festival.FestivalReplyDTO
import com.example.thefesta.model.festival.FestivalReplyResponse
import com.example.thefesta.model.festival.FestivalResponse
import com.example.thefesta.model.festival.PageDTO
import com.example.thefesta.model.member.MemberDTO
import com.example.thefesta.retrofit.FestivalClient
import com.example.thefesta.service.IFestivalService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FestivalDetail : Fragment() {

    private lateinit var binding: FragmentFestivalDetailBinding
    private val festivalService: IFestivalService =
        FestivalClient.retrofit.create(IFestivalService::class.java)
    private lateinit var paginationLayout: LinearLayout
    private var userInfo: MemberDTO? = null
    private var customAdapter = FestivalReplyAdapter(emptyList(), userInfo)
    private var page = 1
    private var endPage = 1
    private var total = 1
    private val id = MainActivity.prefs.getString("id", "")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFestivalDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contentid = arguments?.getString(ARG_CONTENT_ID)
        paginationLayout = view.findViewById(R.id.replyPaginationLayout)

        // FoodList 프래그먼트를 추가
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.foodListContainer, FoodList().apply {
                arguments = Bundle().apply { putString("contentid", contentid) }
            })
            .commit()

        if (id != "") {
            getUserInfo(id)
        }

        contentid?.let {
            val call: Call<FestivalResponse> = festivalService.getFesivalDetail(contentid = it)

            call.enqueue(object : Callback<FestivalResponse> {
                override fun onResponse(
                    call: Call<FestivalResponse>,
                    response: Response<FestivalResponse>
                ) {
                    if (response.isSuccessful) {
                        val festivalResponse: FestivalResponse? = response.body()
                        val fetivalImgList: List<FestivalImgItemDTO>? = festivalResponse?.fiList
                        val festivalInfo: FestivalItemDTO? = festivalResponse?.fDto

                        fetivalImgList?.let {
                            setupImageSlider(it)
                        }

                        updateFestivalDetail(festivalInfo)

                        Log.d(
                            "FestivalInfo",
                            "contentid: ${it}, title: ${festivalInfo?.title}, sponsor1: ${festivalInfo?.sponsor1}, ${festivalInfo?.agelimit}"
                        )

                        fetivalImgList?.forEach { fImg ->
                            Log.d(
                                "FestivalImg",
                                "ffileno: ${fImg.ffileno}, originimgurl: ${fImg.originimgurl}, serialnum: ${fImg.serialnum}"
                            )
                        }

                        replyList(contentid, page)

                        if (userInfo != null) {
                            Glide.with(binding.userImg.context).load(userInfo!!.profileImg)
                                .into(binding.userImg)
                            binding.userNick.text = userInfo!!.nickname
                        }

                        binding.replyBtn.setOnClickListener {
                            if (id != "") {
                                val replyContent = binding.replyContent.text.toString()

                                handleReplyInsert(contentid, replyContent)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "회원만 댓글 등록이 가능합니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }
                }

                override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                    Log.e("FestivalInfo", "Network request failed", t)
                    t.printStackTrace()
                }
            })
        }

    }

    private fun getUserInfo(id: String) {
        Log.d("FestivalGetUserInfo", "회원 정보 가져오기")
        val mDto = MemberDTO(
            id = id,
            nickname = null,
            password = null,
            statecode = null,
            profileImg = null,
            agreement = null,
            joindate = null,
            finalaccess = null,
            withdrawdate = null,
            reportnum = null,
            updatedate = null,
            changeList = null
        )
        val call: Call<MemberDTO> = festivalService.selMember(mDto)
        call.enqueue(object : Callback<MemberDTO> {
            override fun onResponse(call: Call<MemberDTO>, response: Response<MemberDTO>) {
                if (response.isSuccessful) {
                    val memberResponse: MemberDTO? = response.body()
                    Log.d("FestivalGetUserInfo", "회원 정보 가져오기 성공 : ${memberResponse}")
                    userInfo = memberResponse
                    customAdapter = FestivalReplyAdapter(emptyList(), userInfo)
                } else {
                    Log.d("FestivalGetUserInfo", "회원 정보 가져오기 실패")
                }
            }

            override fun onFailure(call: Call<MemberDTO>, t: Throwable) {
                Log.e("FestivalGetUserInfo", "Network request failed", t)
                t.printStackTrace()
            }
        })
    }

    private fun replyList(contentid: String, page: Int) {
        val replyListCall: Call<FestivalReplyResponse> =
            festivalService.getReplyList(page = page, contentid = contentid)

        replyListCall.enqueue(object : Callback<FestivalReplyResponse> {
            override fun onResponse(
                call: Call<FestivalReplyResponse>,
                response: Response<FestivalReplyResponse>
            ) {
                if (response.isSuccessful) {

                    Log.d("FestivalDetailReply", "댓글 목록 가져오기 성공")
                    val festivalResponse: FestivalReplyResponse? = response.body()
                    val replyList: List<FestivalReplyDTO>? = festivalResponse?.list
                    Log.d("FestivalDetailReply", "댓글 목록 : ${replyList}")

                    endPage = festivalResponse?.pageMaker?.realEnd ?: 1
                    total = festivalResponse?.pageMaker?.total ?: 1

                    setupPagination(festivalResponse?.pageMaker)
                    showReplies(replyList)

                    // 댓글 삭제
                    customAdapter.setOnDeleteButtonClickListener(object :
                        FestivalReplyAdapter.OnDeleteButtonClickListener {
                        override fun onDeleteButtonClick(frno: Int) {
                            Log.d("FestivalDeleteBtn", "frno : ${frno}")
                            val alertDialog = AlertDialog.Builder(requireContext())
                                .setTitle("알림")
                                .setMessage("댓글을 삭제하시겠습니까?")
                                .setPositiveButton("확인") { _, _ ->
                                    val call: Call<String> = festivalService.replyDelete(frno)
                                    call.enqueue(object : Callback<String> {
                                        override fun onResponse(
                                            call: Call<String>,
                                            response: Response<String>
                                        ) {
                                            if (response.isSuccessful) {
                                                Log.d("FestivalDeleteBtn", "댓글 삭제 완료")
                                                Toast.makeText(
                                                    requireContext(),
                                                    "댓글이 삭제되었습니다.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                replyList(contentid, page)
                                            } else {
                                                Log.d("FestivalDeleteBtn", "댓글 삭제 실패")
                                            }
                                        }

                                        override fun onFailure(call: Call<String>, t: Throwable) {
                                            Log.e("FestivalDeleteBtn", "Network request failed", t)
                                            t.printStackTrace()
                                        }
                                    })
                                }
                                .setNegativeButton("취소", { _, _ ->
                                    Toast.makeText(
                                        requireContext(),
                                        "댓글 삭제가 취소되었습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })

                            alertDialog.show()
                        }
                    })

                    // 댓글 저장
                    customAdapter.setOnModifySubmitButtonClickListener(object :
                        FestivalReplyAdapter.OnModifySubmitButtonClickListener {
                        override fun onModifySubmitButtonClick(frDTO: FestivalReplyDTO) {
                            Log.d("FestivalModifySubmitBtn", "저장 버튼 클릭 : ${frDTO}")
                            val call: Call<String> = festivalService.replyModify(frDTO)
                            call.enqueue(object : Callback<String> {
                                override fun onResponse(
                                    call: Call<String>,
                                    response: Response<String>
                                ) {
                                    if (response.isSuccessful) {
                                        Log.d("FestivalModifySubmitBtn", "댓글 저장 완료")
                                        replyList(contentid, page)
                                    } else {
                                        Log.d("FestivalModifySubmitBtn", "댓글 저장 실패")
                                    }
                                }

                                override fun onFailure(call: Call<String>, t: Throwable) {
                                    Log.e("FestivalModifySubmitBtn", "Network request failed", t)
                                    t.printStackTrace()
                                }
                            })
                        }
                    })

                    // 댓글 신고
                    customAdapter.setOnReportButtonClickListener(object :
                        FestivalReplyAdapter.OnReportButtonClickListener {
                        override fun onReportButtonClick(frDTO: FestivalReplyDTO) {
                            if (id != "") {
                                Log.d("FestivalReportBtn", "신고 버튼 클릭 : ${frDTO}")
                                val festivalReplyReportFragment =
                                    FestivalReplyReport.newInstance(id, frDTO.id, frDTO.frno)
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.container, festivalReplyReportFragment)
                                    .addToBackStack(null)
                                    .commit()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "회원만 댓글 신고가 가능합니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    })

                }
            }

            override fun onFailure(call: Call<FestivalReplyResponse>, t: Throwable) {
                Log.e("FestivalDetailReply", "댓글 목록 가져오기 실패", t)
                t.printStackTrace()
            }

        })
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

    private fun handlePageChange(page: Int) {
        val contentid = arguments?.getString(ARG_CONTENT_ID).toString()
        replyList(contentid, page)
    }

    private fun showReplies(replyList: List<FestivalReplyDTO>?) {
        customAdapter = FestivalReplyAdapter(replyList ?: emptyList(), userInfo)

        val recyclerView: RecyclerView = binding.replyRecyclerView
        recyclerView.adapter = customAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        customAdapter.notifyDataSetChanged()
    }

    private fun handleReplyInsert(contentid: String, replyContent: String) {
        val rDto = FestivalReplyDTO(
            frno = 0,
            contentid = contentid,
            id = id,
            nickname = "user2",
            frcontent = replyContent,
            profileImg = "",
            frregist = null,
            fredit = null
        )

        val call: Call<String> = festivalService.insertReply(rDto)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Log.d("ReplyInsert", "댓글이 등록되었습니다.")
                    hideKeyboard()

                    val currentPage = endPage
                    val currentTotal = total
                    val remainder = currentTotal % 10
                    Log.d("ReplyInsert", "댓글이 등록 : ${currentPage}.")

                    if (remainder == 0) {
                        Log.d("ReplyInsert", "댓글 등록 true : ${currentPage}.")
                        replyList(contentid, currentPage + 1)
                    } else {
                        Log.d("ReplyInsert", "댓글 등록 false : ${currentPage}.")
                        replyList(contentid, currentPage)
                    }

                    binding.replyContent.text.clear()
                    binding.replyContent.clearFocus()
                } else {
                    Log.d("ReplyInsert", "댓글 등록 실패")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("ReplyInsert", "Network request failed", t)
                t.printStackTrace()
            }
        })
    }

    private fun hideKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus
        view?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    private fun setupImageSlider(festivalImgList: List<FestivalImgItemDTO>) {
        val viewPager: ViewPager2 = binding.viewPager
        if (!festivalImgList.isNullOrEmpty()) {
            binding.originimgurl.visibility = View.GONE
            val adapter = FestivalImageSliderAdapter(festivalImgList)
            viewPager.adapter = adapter
        } else {
            binding.viewPager.visibility = View.GONE
        }
    }

    private fun updateFestivalDetail(festivalItemDTO: FestivalItemDTO?) {
        Log.d("FestivalDetail", "Binding is null: ${binding == null}")

        binding.title.text = festivalItemDTO?.title ?: ""
        binding.eventstartdate.text = festivalDate(festivalItemDTO?.eventstartdate) ?: ""
        binding.eventenddate.text = festivalDate(festivalItemDTO?.eventenddate) ?: ""
        binding.addr1.text = fm(festivalItemDTO?.addr1) ?: ""
        binding.eventintro.text = fm(festivalItemDTO?.eventintro) ?: ""
        binding.eventtext.text = fm(festivalItemDTO?.eventtext) ?: ""
        binding.homepage.text = fm(festivalItemDTO?.homepage) ?: ""
        binding.agelimit.text = fm(festivalItemDTO?.agelimit) ?: ""
        binding.sponsor1.text = fm(festivalItemDTO?.sponsor1) ?: ""
        binding.sponsor1tel.text = fm(festivalItemDTO?.sponsor1tel) ?: ""
        binding.sponsor2.text = fm(festivalItemDTO?.sponsor2) ?: ""
        binding.sponsor2tel.text = fm(festivalItemDTO?.sponsor2tel) ?: ""
        binding.usetimefestival.text = fm(festivalItemDTO?.usetimefestival) ?: ""
        binding.playtime.text = fm(festivalItemDTO?.playtime) ?: ""

        binding.addr1Layout.visibility =
            if (festivalItemDTO?.addr1.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.eventintroLayout.visibility =
            if (festivalItemDTO?.eventintro.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.eventtextLayout.visibility =
            if (festivalItemDTO?.eventtext.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.homepageLayout.visibility =
            if (festivalItemDTO?.homepage.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.agelimitLayout.visibility =
            if (festivalItemDTO?.agelimit.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.sponsor1Layout.visibility =
            if (festivalItemDTO?.sponsor1.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.sponsor1telLayout.visibility =
            if (festivalItemDTO?.sponsor1tel.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.sponsor2Layout.visibility =
            if (festivalItemDTO?.sponsor2.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.sponsor2telLayout.visibility =
            if (festivalItemDTO?.sponsor2tel.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.usetimefestivalLayout.visibility =
            if (festivalItemDTO?.usetimefestival.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.playtimeLayout.visibility =
            if (festivalItemDTO?.playtime.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    fun festivalDate(date: String?): String {
        if (!date.isNullOrEmpty()) {
            val y = date.substring(0, 4)
            val m = date.substring(4, 6)
            val d = date.substring(6, 8)

            return "${y}년 ${m}월 ${d}일"
        }

        return ""
    }

    fun fm(item: String?): String {
        if (!item.isNullOrEmpty()) {
            val clean = item.replace(Regex("(<([^>]+)>|홈페이지)"), "")
            return clean
        }
        return ""
    }

    companion object {
        private const val ARG_CONTENT_ID = "contentid"

        fun newInstance(contentId: String): FestivalDetail {
            val fragment = FestivalDetail()
            val args = Bundle()
            args.putString(ARG_CONTENT_ID, contentId)
            fragment.arguments = args
            return fragment
        }
    }
}