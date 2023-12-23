package com.example.thefesta.festival

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thefesta.R
import com.example.thefesta.databinding.FestivalLikelistItemBinding
import com.example.thefesta.model.festival.LikeDTO

class FestivalLikeListAdapter(private val likeList: List<LikeDTO>): RecyclerView.Adapter<LikeListHolder>() {

    interface OnFestivalInfoClickListener {
        fun onFestivalInfoClick(contentid: String)
    }

    private var festivalInfoClickListener: FestivalLikeListAdapter.OnFestivalInfoClickListener? = null

    fun setOnFestivalInfoClickListener(listener: FestivalLikeListAdapter.OnFestivalInfoClickListener) {
        this.festivalInfoClickListener = listener
    }

    interface OnLikeCheckBoxClickListener {
        fun onLikeCheckBoxClick()
    }

    private var likeCheckBoxClickListener: FestivalLikeListAdapter.OnLikeCheckBoxClickListener? = null

    fun setOnLikeCheckBoxClickListener(listener: FestivalLikeListAdapter.OnLikeCheckBoxClickListener) {
        this.likeCheckBoxClickListener = listener
    }

    private val checkedItems = mutableListOf<Boolean>()

    init {
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

        holder.binding.likeListCheckBox.setOnCheckedChangeListener { _, isChecked ->
            checkedItems[position] = isChecked
        }
    }

    fun selectAll(select: Boolean) {
        for (i in checkedItems.indices) {
            checkedItems[i] = select
        }
        notifyDataSetChanged()
    }

    fun getSelectedContentIds(): List<String> {
        val selectedContentIds = mutableListOf<String>()
        for (i in checkedItems.indices) {
            if (checkedItems[i]) {
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

        binding.likeListCheckBox.isChecked = checked

        binding.likeListCheckBox.setOnCheckedChangeListener { _, isChecked ->
            festivalInfoClickListener.onFestivalInfoClick(like.contentid)
        }

    }
}