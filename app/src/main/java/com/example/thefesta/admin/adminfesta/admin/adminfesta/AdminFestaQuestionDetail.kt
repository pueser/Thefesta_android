package com.example.thefesta.admin.adminfesta.admin.adminfesta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.thefesta.R
import com.example.thefesta.databinding.FragmentAdminFestaQuestionDetailBinding
import com.example.thefesta.retrofit.AdminClient
import com.example.thefesta.service.IAdminService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminFestaQuestionDetail : Fragment() {

    private lateinit var binding: FragmentAdminFestaQuestionDetailBinding
    private var questioncontent: String = ""
    private var questionid: String = ""
    private var contentId: String = ""



    companion object {
        private const val ARG_questioncontent = "arg_questioncontent"
        private const val ARG_questionid = "arg_questionid"
        private const val ARG_contentId = "arg_contentId"

        fun newInstance(questioncontent: String, questionid: String, contentId: String): AdminFestaQuestionDetail {
            val fragment = AdminFestaQuestionDetail()
            val args = Bundle()
            args.putString(ARG_questioncontent, questioncontent)
            args.putString(ARG_questionid, questionid)
            args.putString(ARG_contentId, contentId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAdminFestaQuestionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questioncontent = arguments?.getString(ARG_questioncontent).orEmpty()
        questionid = arguments?.getString(ARG_questionid).orEmpty()
        contentId = arguments?.getString(ARG_contentId).orEmpty()

        if (questioncontent.isNotBlank() && questionid.isNotBlank()) {
            val truncatedQuestionId = questionid.toDouble().toInt().toString()
            binding.adminFestaDetailReportContent.text = questioncontent
            binding.adminFestaDetailReport.text = "건의번호 : ${truncatedQuestionId}번"
        }
        //확인하기 버튼 클릭했을 때
        binding.adminFestaDetailCheackBtn.setOnClickListener {
            CheackBtnClick(questionid.toDouble().toInt().toString())
        }
        //취소하기 버튼 클릭했을 때
        binding.adminFestaDetailCancleBtn.setOnClickListener {
            Toast.makeText(requireContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show()
            val fragmentManager = requireActivity().supportFragmentManager

            val adminFestaQuestionFragment = AdminFestaQuestion.newInstance(contentId)
            fragmentManager.beginTransaction()
                .replace(R.id.container_admin, adminFestaQuestionFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun CheackBtnClick(questionId: String) {
        val retrofit = AdminClient.retrofit


        retrofit.create(IAdminService::class.java).postQuestionDelete(questionId)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.code() == 200) {
                        val responseText = response.body()
                        if (responseText != null) {
                            val intValue = responseText.toInt()
                            Toast.makeText(requireContext(), "${intValue}번이 확인 되었습니다.", Toast.LENGTH_SHORT).show()
                            val fragmentManager = requireActivity().supportFragmentManager

                            val adminFestaQuestionFragment =
                                AdminFestaQuestion.newInstance(contentId)
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
