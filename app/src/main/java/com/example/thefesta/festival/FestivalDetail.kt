package com.example.thefesta.festival

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.forEach
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
import com.example.thefesta.model.member.MemberChangeDTO
import com.example.thefesta.model.member.MemberDTO
import com.example.thefesta.retrofit.FestivalClient
import com.example.thefesta.service.IFestivalService
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FestivalDetail : Fragment() {

    private lateinit var binding: FragmentFestivalDetailBinding
    private val festivalService: IFestivalService = FestivalClient.retrofit.create(IFestivalService::class.java)
    private lateinit var paginationLayout: LinearLayout
    private var userInfo: MemberDTO? = null
    private var customAdapter = FestivalReplyAdapter(emptyList(), userInfo)
    private var page = 1
    private var endPage = 1
    private var total = 1
    private val id = MainActivity.prefs.getString("id", "")
    private var fDtoItemNum = 0

    //음식점 추천 프래그먼트 위치 저장 및 복원
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val layoutParams = binding.foodListContainer.layoutParams as RelativeLayout.LayoutParams
        outState.putInt("foodListFragment_topMargin", layoutParams.topMargin)
        outState.putInt("foodListFragment_bottomMargin", layoutParams.bottomMargin)
        outState.putInt("foodListFragment_leftMargin", layoutParams.leftMargin)
        outState.putInt("foodListFragment_rightMargin", layoutParams.rightMargin)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {

            val topMargin = savedInstanceState.getInt("foodListFragment_topMargin", 0)
            val bottomMargin = savedInstanceState.getInt("foodListFragment_bottomMargin", 0)
            val leftMargin = savedInstanceState.getInt("foodListFragment_leftMargin", 0)
            val rightMargin = savedInstanceState.getInt("foodListFragment_rightMargin", 0)

            val layoutParams = binding.foodListContainer.layoutParams as RelativeLayout.LayoutParams
            layoutParams.topMargin = topMargin
            layoutParams.bottomMargin = bottomMargin
            layoutParams.leftMargin = leftMargin
            layoutParams.rightMargin = rightMargin

            binding.foodListContainer.layoutParams = layoutParams
        }
    }

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

                        val nonNullItemCount = listOf(
                            festivalInfo?.title,
                            festivalInfo?.addr1,
                            festivalInfo?.eventstartdate,
                            festivalInfo?.eventenddate,
                            festivalInfo?.homepage,
                            festivalInfo?.sponsor1,
                            festivalInfo?.sponsor1tel,
                            festivalInfo?.sponsor2,
                            festivalInfo?.sponsor2tel,
                            festivalInfo?.agelimit,
                            festivalInfo?.playtime,
                            festivalInfo?.usetimefestival,
                        ).count { it != null}

                        fDtoItemNum = nonNullItemCount

                        Log.d("FestivalInfocnt", "NonNullItemCount: $nonNullItemCount")

                        updateFestivalDetail(festivalInfo)

                        Log.d("FestivalInfo",
                            "contentid: ${it}, title: ${festivalInfo?.title}, sponsor1: ${festivalInfo?.sponsor1}, ${festivalInfo?.agelimit}")

                        fetivalImgList?.forEach { fImg ->
                            Log.d("FestivalImg",
                                "ffileno: ${fImg.ffileno}, originimgurl: ${fImg.originimgurl}, serialnum: ${fImg.serialnum}")
                        }

                        replyList(contentid, page)

                        if (userInfo != null) {
                            Glide.with(binding.userImg.context).load(userInfo!!.profileImg).into(binding.userImg)
                            binding.userNick.text = userInfo!!.nickname
                        }

                        if (id == "") {
                            binding.replyContent.setHint("로그인 시 이용가능합니다.")
                            binding.replyContent.isFocusable = false
                            binding.replyContent.isClickable = false
                            binding.replyContent.setOnClickListener{
                                Toast.makeText(requireContext(), "회원만 댓글 등록이 가능합니다.", Toast.LENGTH_SHORT).show()
                            }
                        }


                        binding.replyBtn.setOnClickListener {
                            if (id != "") {
                                val replyContent = binding.replyContent.text.toString()

                                handleReplyInsert(contentid, replyContent)
                            } else {
                                Toast.makeText(requireContext(), "회원만 댓글 등록이 가능합니다.", Toast.LENGTH_SHORT).show()
                            }

                        }

                    }
                }

                override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                    Log.e("FestivalGetUserInfo", "Network request failed", t)
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
        val replyListCall: Call<FestivalReplyResponse> = festivalService.getReplyList(page = page, contentid = contentid)

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

                    endPage = festivalResponse?.pageMaker?.realEnd ?:1
                    total = festivalResponse?.pageMaker?.total ?:1

                    setupPagination(festivalResponse?.pageMaker)
                    showReplies(replyList)

                    // 댓글 삭제
                    customAdapter.setOnDeleteButtonClickListener(object :FestivalReplyAdapter.OnDeleteButtonClickListener {
                        override fun onDeleteButtonClick(frno: Int) {
                            Log.d("FestivalDeleteBtn", "frno : ${frno}")
                            val alertDialog = AlertDialog.Builder(requireContext())
                                .setTitle("알림")
                                .setMessage("댓글을 삭제하시겠습니까?")
                                .setPositiveButton("확인") { _, _ ->
                                    val call: Call<String> = festivalService.replyDelete(frno)
                                    call.enqueue(object : Callback<String> {
                                        override fun onResponse(call: Call<String>, response: Response<String>) {
                                            if (response.isSuccessful) {
                                                Log.d("FestivalDeleteBtn", "댓글 삭제 완료")
                                                Toast.makeText(requireContext(), "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
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
                                    Toast.makeText(requireContext(), "댓글 삭제가 취소되었습니다.", Toast.LENGTH_SHORT).show()
                                })

                            alertDialog.show()
                        }
                    })

                    // 댓글 저장
                    customAdapter.setOnModifySubmitButtonClickListener(object :FestivalReplyAdapter.OnModifySubmitButtonClickListener {
                        override fun onModifySubmitButtonClick(frDTO: FestivalReplyDTO) {
                            Log.d("FestivalModifySubmitBtn", "저장 버튼 클릭 : ${frDTO}")
                            val call: Call<String> = festivalService.replyModify(frDTO)
                            call.enqueue(object : Callback<String> {
                                override fun onResponse(call: Call<String>, response: Response<String>) {
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
                    customAdapter.setOnReportButtonClickListener(object : FestivalReplyAdapter.OnReportButtonClickListener {
                        override fun onReportButtonClick(frDTO: FestivalReplyDTO) {
                            if (id != "") {
                                Log.d("FestivalReportBtn", "신고 버튼 클릭 : ${frDTO}")
                                val festivalReplyReportFragment = FestivalReplyReport.newInstance(id, frDTO.id, frDTO.frno)
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.container, festivalReplyReportFragment)
                                    .addToBackStack(null)
                                    .commit()
                            } else {
                                Toast.makeText(requireContext(), "회원만 댓글 신고가 가능합니다.", Toast.LENGTH_SHORT).show()
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

    private fun createAndAddButton(text: String, currentPage: Int, onClickListener: () -> Unit) {
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
        if (page == currentPage) {
            // 현재 페이지일 때 다른 백그라운드 적용 (예: drawable/festival_current_page_btn.xml)
            button.setBackgroundResource(R.drawable.festival_pagination_sel_btn)
        } else {
            // 다른 페이지일 때의 백그라운드 적용 (예: drawable/festival_pagination_btn.xml)
            button.setBackgroundResource(R.drawable.festival_pagination_btn)
        }

        paginationLayout.addView(button)
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

    private fun handlePageChange(pageNum: Int) {
        val contentid = arguments?.getString(ARG_CONTENT_ID).toString()
        page = pageNum
        // 페이지 변경 시 동작을 정의합니다.
        replyList(contentid, pageNum)
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

                    // 댓글 등록 후 현재 페이지의 댓글 수를 확인
                    val currentPage = endPage
                    val currentTotal = total
                    val remainder = currentTotal % 10
                    Log.d("ReplyInsert", "댓글이 등록 : ${currentPage}.")

                    if (remainder == 0) {
                        Log.d("ReplyInsert", "댓글 등록 true : ${currentPage}.")
                        // 현재 페이지가 가득 차 있으면 다음 페이지로 이동
                        replyList(contentid, currentPage + 1)
                        handlePageChange(currentPage + 1)
                    } else {
                        Log.d("ReplyInsert", "댓글 등록 false : ${currentPage}.")
                        // 현재 페이지에 여유가 있으면 현재 페이지로 다시 로드
                        replyList(contentid, currentPage)
                        handlePageChange(currentPage)
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
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus
        view?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    private fun setupImageSlider(festivalImgList: List<FestivalImgItemDTO>) {
        val viewPager: ViewPager2 = binding.viewPager
        if (!festivalImgList.isNullOrEmpty()) {
            binding.originimgurl.visibility = View.GONE
            val adapter = FestivalImageSliderAdapter(festivalImgList)
            viewPager.adapter = adapter

            // DotsIndicator와 ViewPager2를 연결
            val dotsIndicator: DotsIndicator = binding.dotsIndicator
            dotsIndicator.setViewPager2(viewPager)

        } else {
            binding.viewPager.visibility = View.GONE
        }
    }

    private fun updateFestivalDetail(festivalItemDTO: FestivalItemDTO?) {
        Log.d("FestivalDetail", "Binding is null: ${binding == null}" )

        festivalItemDTO?.let {
            updateIfNotEmpty("축제명", it.title)
            updateIfNotEmpty("주소", it.addr1)
            updateIfNotEmpty("시작일", festivalDate(it.eventstartdate))
            updateIfNotEmpty("종료일", festivalDate(it.eventenddate))
            updateIfNotEmpty("홈페이지", fm(it.homepage, isHomepage = true))
            updateIfNotEmpty("주최자 정보", fm(it.sponsor1))
            updateIfNotEmpty("주최자 연락처", fm(it.sponsor1tel))
            updateIfNotEmpty("주관사 정보", fm(it.sponsor2))
            updateIfNotEmpty("주관사 연락처", fm(it.sponsor2tel))
            updateIfNotEmpty("관람가능연령", fm(it.agelimit))
            updateIfNotEmpty("축제시간", fm(it.playtime))
            updateIfNotEmpty("이용요금", fm(it.usetimefestival))
            updateIfNotEmpty("행사소개", fm(it.eventintro))
            updateIfNotEmpty("행사내용", fm(it.eventtext))
        }
    }

    private fun updateIfNotEmpty(label: String, value: String?) {
        if (!value.isNullOrEmpty()) {
            if (label.equals("행사소개") || label.equals("행사내용")) {
                festivalIntro(label, value)
            } else {
                festivalInfo(label, value)
            }
        }
    }

    private fun festivalInfo(title: String, content: String) {
        // TextView 추가를 위한 TableLayout 가져오기
        val infotableLayout = binding.festivalInfoTableLayout
        val introtableLayout = binding.festivalIntroTableLayout

        // 동적으로 TableRow 생성
        val tableRow = TableRow(requireContext())
        tableRow.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        // 동적으로 TextView 생성 (제목)
        tableRow.addView(createTextView(title, 16, 280, false, textColor = Color.BLACK, isFirstTextView = true))

        // 동적으로 TextView 생성 (내용)
        if (title.equals("홈페이지")) {
            // 홈페이지인 경우, URL이면 링크로 처리
            tableRow.addView(createTextView(content, 16, ViewGroup.LayoutParams.WRAP_CONTENT, isLink = true, textColor = Color.BLACK))
        } else {
            // 그 외의 경우는 일반 텍스트로 처리
            tableRow.addView(createTextView(content, 16, ViewGroup.LayoutParams.WRAP_CONTENT, isLink = false, textColor = Color.BLACK))
        }

        // TableRow에 폰트 패밀리 지정
        val typeface = ResourcesCompat.getFont(requireContext(), R.font.notoregular)
        tableRow.forEach { view ->
            if (view is TextView) {
                view.setTypeface(Typeface.create(typeface, Typeface.NORMAL))
            }
        }

        // TableRow를 TableLayout에 추가
        if (title.equals("행사소개", ignoreCase = true) || title.equals("행사내용", ignoreCase = true)) {
            introtableLayout.addView(tableRow)
            applyBackgroundIfNeeded(tableRow, introtableLayout, "info")
        } else {
            infotableLayout.addView(tableRow)
            applyBackgroundIfNeeded(tableRow, infotableLayout, "info")
        }
    }

    private fun festivalIntro(title: String, content: String) {
        val introtableLayout = binding.festivalIntroTableLayout

        // 제목에 대한 TableRow 생성 및 추가
        val titleRow = createAndAddRow(introtableLayout, title, 16, 280, false, Color.BLACK)

        // 내용에 대한 TableRow 생성 및 추가
        val contentRow = createAndAddRow(introtableLayout, content, 16, ViewGroup.LayoutParams.WRAP_CONTENT, false, Color.BLACK)
    }

    private fun applyBackgroundIfNeeded(row: TableRow, tableLayout: TableLayout, title: String) {
        if (title.equals("intro") && tableLayout.childCount % 2 == 0) {
            row.setBackgroundResource(R.drawable.festival_table_first)
        } else if (title.equals("info") && tableLayout.childCount % 2 == 1){
            row.setBackgroundColor(Color.parseColor("#fafafa"))
        }

        val rowIndex = tableLayout.indexOfChild(row)

        if (title.equals("info") && rowIndex == 0) {
            row.setBackgroundResource(if (title.equals("info")) R.drawable.festival_table_first else 0)
        } else if (title.equals("info") && fDtoItemNum == tableLayout.childCount) {
            row.setBackgroundResource(if (title.equals("info")) R.drawable.festival_table_last else 0)
        }
    }

    private fun createAndAddRow(tableLayout: TableLayout, text: String, textSize: Int, width: Int, isTitle: Boolean, textColor: Int) {
        val row = TableRow(requireContext())
        row.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        val textView = createTextView(text, textSize, width, isTitle, 0, textColor)
        row.addView(textView)

        val typeface = ResourcesCompat.getFont(requireContext(), R.font.notoregular)
        textView.setTypeface(Typeface.create(typeface, Typeface.NORMAL))

        applyBackgroundIfNeeded(row, tableLayout, "intro")
        tableLayout.addView(row)
    }

    private fun createTextView(text: String, textSize: Int, width: Int, isLink: Boolean = false, marginBottom: Int = 0, textColor: Int, isFirstTextView: Boolean = false): TextView {
        val textView = TextView(requireContext())
        val params = TableRow.LayoutParams(
            width,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, marginBottom)
        textView.layoutParams = params
        textView.text = text
        textView.textSize = textSize.toFloat()
        textView.setTextColor(textColor)
        textView.setPadding(15, 5, 15, 5)

        if (isLink) {
            textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            textView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(text))
                startActivity(intent)
            }
            textView.movementMethod = LinkMovementMethod.getInstance()
        }

        if (isFirstTextView) {
            textView.setBackgroundResource(R.drawable.festival_table)
        }

        return textView
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

    fun fm(item: String?, isHomepage: Boolean = false): String {
        if (!item.isNullOrEmpty()) {
            if (isHomepage) {
                val clean = item.replace(Regex("(홈페이지|<([^>]+)>|\\s*)"), "")
                return clean
            } else {
                val clean = item.replace(Regex("(<([^>]+)>|홈페이지)"), "")
                return clean
            }
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