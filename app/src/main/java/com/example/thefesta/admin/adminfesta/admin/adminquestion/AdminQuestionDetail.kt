package com.example.thefesta.admin.adminfesta.admin.adminquestion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.thefesta.R
import com.example.thefesta.databinding.FragmentAdminQuestionDetailBinding


class AdminQuestionDetail : Fragment() {
    private lateinit var binding: FragmentAdminQuestionDetailBinding
    private var bid: Int = 0
    private var bcontent: String = ""
    companion object {
        private const val ARG_BID = "arg_bid"
        private const val ARG_BCONTENT = "arg_bcontent"

        fun newInstance(bid: Int, bcontent: String): AdminQuestionDetail {
            val fragment = AdminQuestionDetail()
            val args = Bundle()
            args.putInt(ARG_BID, bid)
            args.putString(ARG_BCONTENT, bcontent)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminQuestionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bid = arguments?.getInt(ARG_BID) ?: 0
        bcontent = arguments?.getString(ARG_BCONTENT).orEmpty()

        if (bid != 0 && bcontent.isNotBlank()) {
            binding.adminAdminQuestionDetail.text = "문의번호 : $bid"
            binding.adminAdminQuestionDetailContent.text = bcontent
        }
        binding.adminQuestionDetailRegister.setOnClickListener {
            val adminQuestionRegister = AdminQuestionRegister.newInstance(bid)
            fragmentManager?.beginTransaction()
                ?.replace(R.id.container_admin, adminQuestionRegister)
                ?.addToBackStack(null)
                ?.commit()
        }

    }




}