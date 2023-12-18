package com.example.thefesta.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thefesta.R
import com.example.thefesta.databinding.FooditemBinding
import com.example.thefesta.model.food.RecommendDTO

class CustomAdapter: RecyclerView.Adapter<Holder>() {
    var recFoodList : List<RecommendDTO>? = null

    // 아이템 클릭을 위한 인터페이스
    interface OnItemClickListener {
        fun onItemClick(restaurant: RecommendDTO)
    }

    private var itemClickListener: OnItemClickListener? = null

    // 아이템 클릭 리스너 설정 메서드
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = FooditemBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val food = recFoodList?.get(position)
        holder.setFood(food)

        // 아이템 뷰를 클릭할 때의 리스너 설정
        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(food!!)
        }
    }

    override fun getItemCount(): Int {
        return recFoodList?.size?:0
    }

}

class Holder(val binding: FooditemBinding): RecyclerView.ViewHolder(binding.root) {
    fun setFood(food: RecommendDTO?) {
        food?.let {
            val foodImage = it.firstimage2
            if (!foodImage.isNullOrEmpty()) {
                Glide.with(binding.firstimage2.context).load(foodImage).into(binding.firstimage2)
            } else {
                //이미지가 없는 경우
                binding.firstimage2.setImageResource(R.drawable.noimage)
            }

            val foodTitle = it.title
            if (foodTitle.length <= 16) {
                binding.title.text = foodTitle
            } else {
                binding.title.text = foodTitle.substring(0,16)+".."
            }

            val foodAddr = it.addr1
            if (foodAddr.isNotEmpty()) {
                binding.addr.text = foodAddr
            } else {
                binding.addr.text = "정보 없음"
            }
        }
    }
}