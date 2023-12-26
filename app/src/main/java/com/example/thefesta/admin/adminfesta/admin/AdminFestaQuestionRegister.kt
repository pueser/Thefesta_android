package com.example.thefesta.admin.adminfesta.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.thefesta.MainActivity
import com.example.thefesta.R
import com.example.thefesta.admin.adminfesta.admin.adminmember.AdminMemberDetail
import com.example.thefesta.adminbottomnavi.AdminQuestion
import com.example.thefesta.databinding.FragmentAdminFestaQuestionregisterBinding
import com.example.thefesta.festival.FestivalDetail
import com.example.thefesta.model.admin.QuestionDTO
import com.example.thefesta.retrofit.AdminClient
import com.example.thefesta.service.IAdminService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AdminFestaQuestionRegister : Fragment() {
    private lateinit var binding: FragmentAdminFestaQuestionregisterBinding
    private var id: String = ""
    private var contentid: String = ""
    private var title: String = ""
    private var questioncontent: String = ""
    companion object {
        private const val ARG_CONTENTID = "arg_contentid"
        private const val ARG_TITLE = "arg_title"

        fun newInstance(contentid: String, title : String): AdminFestaQuestionRegister {
            val fragment = AdminFestaQuestionRegister()
            val args = Bundle()
            args.putString(ARG_CONTENTID, contentid)
            args.putString(ARG_TITLE, title)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminFestaQuestionregisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentid = arguments?.getString(AdminFestaQuestionRegister.ARG_CONTENTID).orEmpty()
        title = arguments?.getString(AdminFestaQuestionRegister.ARG_TITLE).orEmpty()
        Log.d("AdminFestaQuestionRegister", "contentid: $contentid")
        Log.d("AdminFestaQuestionRegister", "title: $title")

        id = MainActivity.prefs.getString("id", "")
        if (contentid.isNotBlank() && title.isNotBlank()) {
            binding.adminFestaQuestionRegister.text = "축제 : $title"
        }

        binding.adminRegisterBtn.setOnClickListener {
            questioncontent = binding.adminFestaQuestionRegisterContent.text.toString()
            Log.d("AdminFestaQuestionRegister", "questioncontent: ${questioncontent}")
            Log.d("AdminFestaQuestionRegister", "id: ${id}")
            Log.d("AdminFestaQuestionRegister", "contentid ${contentid}")
            val questionDto = QuestionDTO(
                questioncontent = questioncontent,
                id = id,
                contentid = contentid
            )
           adminFestaRegisterBtnClick(questionDto)
        }

        binding.adminCancelBtn.setOnClickListener {
            adminCancelBtnClick(contentid)
        }


        binding.adminRegisterBtn.isEnabled = false // 초기에 버튼 비활성화

        binding.adminFestaQuestionRegisterContent.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                activity?.runOnUiThread {
                    var currentByteLength: Int = 0
                    if (s != null) {
                        currentByteLength = calculateByteLength(s.toString())
                    }
                    Log.d("AdminQuestionRegister", "currentByteLength: ${currentByteLength}")

                    if (currentByteLength > 3000 || currentByteLength == 0) {
                        // 3000바이트 이상이거나 글자 수가 없으면 작성완료 버튼 비활성화
                        binding.adminRegisterBtn.isEnabled = false
                        if (currentByteLength == 0) {
                            // 글자 수가 없을 때 "글자를 입력해주세요" 경고창 표시
                            Toast.makeText(requireContext(), "글자를 입력해주세요", Toast.LENGTH_SHORT).show()
                        } else {
                            // 3000바이트 이상이면 "1000글자를 초과하였습니다." 경고창 표시
                            Toast.makeText(requireContext(), "1000글자를 초과하였습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 3000바이트 이하이면서 글자 수가 있는 경우 작성완료 버튼 활성화
                        binding.adminRegisterBtn.isEnabled = true
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

            private fun calculateByteLength(text: String): Int {
                var byteLength = 0
                for (char in text) {
                    byteLength += if ((char.isLetterOrDigit() || char.isWhitespace()) && !char.isHangul()) {
                        Log.d("AdminQuestionRegister", "1바이트 추가")
                        1
                    } else {
                        Log.d("AdminQuestionRegister", "byteLength: ${byteLength}")
                        Log.d("AdminQuestionRegister", "3바이트 추가")
                        3
                    }
                }
                return byteLength
            }

            private fun Char.isHangul(): Boolean {
                val unicodeBlock = Character.UnicodeBlock.of(this)
                return unicodeBlock == Character.UnicodeBlock.HANGUL_SYLLABLES || unicodeBlock == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO || unicodeBlock == Character.UnicodeBlock.HANGUL_JAMO
            }
        })


    }

    // 작성완료 버튼 클릭
    private fun adminFestaRegisterBtnClick(questionDto: QuestionDTO) {


        questionDto.questioncontent = questioncontent
        questionDto.id = id
        questionDto.contentid = contentid
        Log.d("AdminFestaQuestionRegister", "questionDto ${questionDto.toString()}")
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).postQuestionRegister(questionDto)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.code() == 200) {
                        Log.d("AdminFestaQuestionRegister", "200: ${response.body()}")
                        Toast.makeText(requireContext(), "축제 건의글 등록되었습니다.", Toast.LENGTH_SHORT).show()

                        val fragmentManager = requireActivity().supportFragmentManager
                        val adminMemberDetail = FestivalDetail.newInstance(contentid)
                        fragmentManager.beginTransaction()
                            .replace(R.id.container, adminMemberDetail)
                            .addToBackStack(null)
                            .commit()

                    } else {
                        Log.d("AdminFestaQuestionRegister", "Failed to delete question: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.d("AdminFestaQuestionRegister", "Network request failed", t)
                }
            })
    }

    // 작성취소 버튼 클릭
    private fun adminCancelBtnClick(contentid: String) {
        Toast.makeText(requireContext(), "축제 건의글 작성이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        val fragmentManager = requireActivity().supportFragmentManager
        val adminMemberDetail = FestivalDetail.newInstance(contentid)
        fragmentManager.beginTransaction()
            .replace(R.id.container, adminMemberDetail)
            .addToBackStack(null)
            .commit()


    }


}