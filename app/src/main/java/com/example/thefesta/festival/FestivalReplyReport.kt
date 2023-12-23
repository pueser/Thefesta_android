package com.example.thefesta.festival

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.thefesta.databinding.FragmentFestivalReplyReportBinding
import com.example.thefesta.model.festival.FestivalReplyReportDTO
import com.example.thefesta.retrofit.FestivalClient
import com.example.thefesta.service.IFestivalService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FestivalReplyReport : Fragment() {
    private lateinit var binding: FragmentFestivalReplyReportBinding
    private val festivalService: IFestivalService =
        FestivalClient.retrofit.create(IFestivalService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFestivalReplyReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 신고
        binding.festivalReplyReportSubmitBtn.setOnClickListener{
            val alertDialog = AlertDialog.Builder(requireContext())
                .setTitle("알림")
                .setMessage("신고하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    val reportcontent = binding.festivalReplyReportInput.text.toString()
                    val reporter = arguments?.getString(ARG_REPORTER).toString()
                    val reported = arguments?.getString(ARG_REPORTED).toString()
                    val rfrno = arguments?.getInt(ARG_RFRNO) ?: 0

                    Log.d("FestivalReplyReport", "reportcontent : ${reportcontent}, reporter : ${reporter}, reported : ${reported}, rfrno : ${rfrno}")

                    val frrDto = FestivalReplyReportDTO(
                        reportcontent = reportcontent,
                        reporter = reporter,
                        reported = reported,
                        rfrno = rfrno
                    )
                    val call: Call<Unit> = festivalService.festaReplyReport(frrDto)
                    call.enqueue(object : Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            Log.d("FestivalReplyReport", "댓글 신고 성공")
                            activity?.supportFragmentManager?.popBackStack()
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Log.e("FestivalReplyReport", "댓글 신고 실패", t)
                            t.printStackTrace()
                        }

                    })
                }
                .setNegativeButton("취소", { _, _ ->
                    Toast.makeText(requireContext(), "댓글 신고가 취소되었습니다.", Toast.LENGTH_SHORT).show()
                    activity?.supportFragmentManager?.popBackStack()
                })
                .create()

            alertDialog.show()
        }

        // 신고 취소
        binding.festivalReplyReportCancelBtn.setOnClickListener{
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    companion object {
        private const val ARG_REPORTER = "reporter"
        private const val ARG_REPORTED = "reported"
        private const val ARG_RFRNO = "rfrno"

        fun newInstance(reporter: String, reported: String, rfrno: Int): FestivalReplyReport {
            val fragment = FestivalReplyReport()
            val args = Bundle()
            args.putString(ARG_REPORTER, reporter)
            args.putString(ARG_REPORTED, reported)
            args.putInt(ARG_RFRNO, rfrno)
            fragment.arguments = args
            return fragment
        }
    }
}