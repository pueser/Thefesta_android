package com.example.thefesta.admin.adminfesta.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.thefesta.MainActivity
import com.example.thefesta.databinding.FragmentAdminFestaQuestionregisterBinding
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

        id = MainActivity.prefs.getString("id", "")
        if (contentid.isNotBlank() && title.isNotBlank()) {
            Log.d("AdminFestaQuestionRegister", "contentid: $contentid")
            Log.d("AdminFestaQuestionRegister", "title: $title")
            binding.adminFestaQuestionRegister.text = "축제 : $title"
        }

        binding.adminRegisterBtn.setOnClickListener {
            questioncontent = binding.adminFestaQuestionRegisterContent.text.toString()
            val questionDto = QuestionDTO(
                questionid = null,  // You can provide an actual value if needed
                questioncontent = questioncontent,
                questiondate = null,  // You can provide an actual value if needed
                questioncount = null,  // You can provide an actual value if needed
                id = id,
                contentid = contentid,
                title = title,
                eventstartdate = null,  // You can provide an actual value if needed
                eventenddate = null,  // You can provide an actual value if needed
                addr1 = null  // You can provide an actual value if needed
            )
           adminFestaRegisterBtnClick(questionDto)
        }


    }

    // 작성완료 버튼 클릭
    private fun adminFestaRegisterBtnClick(questionDto: QuestionDTO) {

        Log.d("AdminFestaQuestionRegister", "questioncontent: ${questioncontent}")
        Log.d("AdminFestaQuestionRegister", "id: ${id}")
        Log.d("AdminFestaQuestionRegister", "contentid ${contentid}")
        questionDto.questioncontent = questioncontent
        questionDto.id = id
        questionDto.contentid = contentid

        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).postQuestionRegister(questionDto)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.code() == 200) {
                        Log.d("AdminFestaQuestionRegister", "200: ${response.body()}")
                        Toast.makeText(requireContext(), "답변등록이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        //변경
//                        val adminQuestion = FestivalDetail.newInstance()
//                        fragmentManager?.beginTransaction()
//                            ?.replace(R.id.container_admin, adminQuestion)
//                            ?.addToBackStack(null)
//                            ?.commit()
                    } else {
                        Log.d("AdminFestaQuestionRegister", "Failed to delete question: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.d("AdminFestaQuestionRegister", "Network request failed", t)
                }
            })
    }


}