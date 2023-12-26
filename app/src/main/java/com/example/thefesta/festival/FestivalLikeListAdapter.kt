package com.example.thefesta.festival

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thefesta.R
import com.example.thefesta.databinding.FestivalLikelistItemBinding
import com.example.thefesta.databinding.FestivalReplyItmeBinding
import com.example.thefesta.model.festival.LikeDTO

class FestivalLikeListAdapter(private val likeList: List<LikeDTO>): RecyclerView.Adapter<LikeListHolder>() {

    // 축제 클릭
    interface OnFestivalInfoClickListener {
        fun onFestivalInfoClick(contentid: String)
    }

    private var festivalInfoClickListener: FestivalLikeListAdapter.OnFestivalInfoClickListener? = null

    fun setOnFestivalInfoClickListener(listener: FestivalLikeListAdapter.OnFestivalInfoClickListener) {
        this.festivalInfoClickListener = listener
    }

    // 체크박스 클릭
    interface OnLikeCheckBoxClickListener {
        fun onLikeCheckBoxClick()
    }

    private var likeCheckBoxClickListener: FestivalLikeListAdapter.OnLikeCheckBoxClickListener? = null

    fun setOnLikeCheckBoxClickListener(listener: FestivalLikeListAdapter.OnLikeCheckBoxClickListener) {
        this.likeCheckBoxClickListener = listener
    }

    // 각 아이템의 체크 상태를 저장하는 리스트
    private val checkedItems = mutableListOf<Boolean>()

    init {
        // 초기에 모든 아이템을 선택되지 않은 상태로 초기화
        likeList.forEach { checkedItems.add(false) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeListHolder {
        val binding = FestivalLikelistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LikeListHolder(binding, festivalInfoClickListener!!, likeCheckBoxClickListener!!)
    }

    override fun getItemCount(): Int = likeList.size

    override fun onBindViewHolder(holder: LikeListHolder, position: Int) {
        val like = likeList[position]
        holder.bind(like, checkedItems[position])

        // 체크박스 상태가 변경될 때마다 리스트 업데이트
        holder.binding.likeListCheckBox.setOnCheckedChangeListener { _, isChecked ->
            checkedItems[position] = isChecked
        }
    }

    // 외부에서 모든 아이템을 선택 또는 선택 해제하는 함수
    fun selectAll(select: Boolean) {
        for (i in checkedItems.indices) {
            checkedItems[i] = select
        }
        notifyDataSetChanged()
    }

    // 외부에서 선택된 아이템의 contentid를 가져오는 함수
    fun getSelectedContentIds(): List<String> {
        val selectedContentIds = mutableListOf<String>()
        for (i in checkedItems.indices) {
            if (checkedItems[i]) {
                // 해당 아이템이 선택된 경우에만 contentid를 추가
                selectedContentIds.add(likeList[i].contentid)
            }
        }
        return selectedContentIds
    }

}

class LikeListHolder(val binding: FestivalLikelistItemBinding,
                     private val festivalInfoClickListener: FestivalLikeListAdapter.OnFestivalInfoClickListener,
                     private val likeCheckBoxClickListener: FestivalLikeListAdapter.OnLikeCheckBoxClickListener,): RecyclerView.ViewHolder(binding.root) {
    fun bind(like: LikeDTO, checked: Boolean) {
        if (!like.firstimage.isNullOrEmpty()) {
            Glide.with(binding.likeListFestivalImg.context).load(like.firstimage).into(binding.likeListFestivalImg)
        } else {
            binding.likeListFestivalImg.setImageResource(R.drawable.noimage)
        }

        binding.likeListFestivalTitle.text = like.title

        binding.likeListFestivalInfo.setOnClickListener{
            festivalInfoClickListener.onFestivalInfoClick(like.contentid)
        }

        binding.likeListCheckBox.setOnClickListener{
            likeCheckBoxClickListener.onLikeCheckBoxClick()
        }

        // 체크박스 상태 설정
        binding.likeListCheckBox.isChecked = checked

        // 체크박스 클릭 리스너
        binding.likeListCheckBox.setOnCheckedChangeListener { _, isChecked ->
            festivalInfoClickListener.onFestivalInfoClick(like.contentid)
        }

    }
}