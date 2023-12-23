package com.example.thefesta.festival

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thefesta.MainActivity
import com.example.thefesta.R
import com.example.thefesta.databinding.FestivalReplyItmeBinding
import com.example.thefesta.model.festival.FestivalReplyDTO
import com.example.thefesta.model.member.MemberDTO

class FestivalReplyAdapter(private val replyList: List<FestivalReplyDTO>, private val userInfo: MemberDTO?) :
    RecyclerView.Adapter<ReplyHolder>() {

    // 삭제 버튼 클릭
    interface OnDeleteButtonClickListener {
        fun onDeleteButtonClick(frno: Int)
    }

    private var deleteButtonClickListener: FestivalReplyAdapter.OnDeleteButtonClickListener? = null

    fun setOnDeleteButtonClickListener(listener: FestivalReplyAdapter.OnDeleteButtonClickListener) {
        this.deleteButtonClickListener = listener
    }

    // 저장 버튼 클릭
    interface OnModifySubmitButtonClickListener {
        fun onModifySubmitButtonClick(frDTO: FestivalReplyDTO)
    }

    private var modifySubmitButtonClickListener: FestivalReplyAdapter.OnModifySubmitButtonClickListener? = null

    fun setOnModifySubmitButtonClickListener(listener: FestivalReplyAdapter.OnModifySubmitButtonClickListener) {
        this.modifySubmitButtonClickListener = listener
    }

    // 신고 버튼 클릭
    interface OnReportButtonClickListener {
        fun onReportButtonClick(frDTO: FestivalReplyDTO)
    }

    private var reportButtonClickListener: FestivalReplyAdapter.OnReportButtonClickListener? = null

    fun setOnReportButtonClickListener(listener: FestivalReplyAdapter.OnReportButtonClickListener) {
        this.reportButtonClickListener = listener
    }

    //
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.festival_reply_itme, parent, false)
        val binding = FestivalReplyItmeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReplyHolder(binding, deleteButtonClickListener!!, modifySubmitButtonClickListener!!, reportButtonClickListener!!, userInfo)
    }

    override fun onBindViewHolder(holder: ReplyHolder, position: Int) {
        val reply = replyList[position]
        holder.bind(reply)
    }

    override fun getItemCount(): Int = replyList.size

}

class ReplyHolder(val binding: FestivalReplyItmeBinding,
                  private val deleteButtonClickListener: FestivalReplyAdapter.OnDeleteButtonClickListener,
                  private val modifySubmitButtonClickListener: FestivalReplyAdapter.OnModifySubmitButtonClickListener,
                  private val reportButtonClickListener: FestivalReplyAdapter.OnReportButtonClickListener,
                  private val userInfo: MemberDTO?) : RecyclerView.ViewHolder(binding.root) {
    fun bind(reply: FestivalReplyDTO) {
        val id = MainActivity.prefs.getString("id", "")

        Glide.with(binding.festivalReplyUserImg.context).load(reply.profileImg).into(binding.festivalReplyUserImg)

        binding.festivalReplyUserNick.text = reply.nickname
        binding.festivalReplyContent.text = reply.frcontent
        binding.festivalReplyDate.text = formatDate(reply.fredit)

        if (id != "" && id.equals(reply.id)) {
            binding.festivalReplyDeleteBtn.visibility = View.VISIBLE
            binding.festivalReplyModifyBtn.visibility = View.VISIBLE
        } else {
            binding.festivalReplyDeleteBtn.visibility = View.GONE
            binding.festivalReplyModifyBtn.visibility = View.GONE
        }

        // 삭제 버튼
        binding.festivalReplyDeleteBtn.setOnClickListener{
            deleteButtonClickListener.onDeleteButtonClick(reply.frno)
        }

        // 수정 버튼
        binding.festivalReplyModifyBtn.setOnClickListener{
            binding.festivalReplyContent.visibility = View.GONE
            binding.festivalReplyModifyBtn.visibility = View.GONE
            binding.festivalReplyDeleteBtn.visibility = View.GONE
            binding.festivalReplyReportBtn.visibility = View.GONE
            binding.festivalReplyModifyInput.visibility = View.VISIBLE
            binding.festivalReplyModifySubmitBtn.visibility = View.VISIBLE
            binding.festivalReplyModifyCancelBtn.visibility = View.VISIBLE
            var frcontent = binding.festivalReplyContent.text.toString()
            binding.festivalReplyModifyInput.setText(frcontent)
        }

        // 저장 버튼
        binding.festivalReplyModifySubmitBtn.setOnClickListener{
            val frDto = FestivalReplyDTO(
                frno = reply.frno,
                contentid = reply.contentid,
                id = reply.id,
                nickname = reply.nickname,
                frcontent = binding.festivalReplyModifyInput.text.toString(),
                profileImg = reply.profileImg,
                frregist = null,
                fredit = null
            )

            binding.festivalReplyContent.visibility = View.VISIBLE
            binding.festivalReplyModifyBtn.visibility = View.VISIBLE
            binding.festivalReplyDeleteBtn.visibility = View.VISIBLE
            binding.festivalReplyReportBtn.visibility = View.VISIBLE
            binding.festivalReplyModifyInput.visibility = View.GONE
            binding.festivalReplyModifySubmitBtn.visibility = View.GONE
            binding.festivalReplyModifyCancelBtn.visibility = View.GONE

            modifySubmitButtonClickListener.onModifySubmitButtonClick(frDto)
        }

        // 취소 버튼
        binding.festivalReplyModifyCancelBtn.setOnClickListener{
            binding.festivalReplyContent.visibility = View.VISIBLE
            binding.festivalReplyModifyBtn.visibility = View.VISIBLE
            binding.festivalReplyDeleteBtn.visibility = View.VISIBLE
            binding.festivalReplyReportBtn.visibility = View.VISIBLE
            binding.festivalReplyModifyInput.visibility = View.GONE
            binding.festivalReplyModifySubmitBtn.visibility = View.GONE
            binding.festivalReplyModifyCancelBtn.visibility = View.GONE
        }

        // 신고 버튼
        binding.festivalReplyReportBtn.setOnClickListener{
            Log.d("FestivalUserId",
                "id: ${id}, replyId: ${reply.id}")
            val frDto = FestivalReplyDTO(
                frno = reply.frno,
                contentid = reply.contentid,
                id = reply.id,
                nickname = reply.nickname,
                frcontent = binding.festivalReplyModifyInput.text.toString(),
                profileImg = reply.profileImg,
                frregist = null,
                fredit = null
            )
            reportButtonClickListener.onReportButtonClick(frDto)
        }
    }

    private fun formatDate(date: String?): String {
        return date ?: ""
    }
}
