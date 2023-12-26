package com.example.thefesta.festival

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thefesta.R
import com.example.thefesta.model.festival.FestivalImgItemDTO
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator

class FestivalImageSliderAdapter(
    private val festivalImgList: List<FestivalImgItemDTO>) : RecyclerView.Adapter<FestivalImageSliderAdapter.ImageSliderViewHolder>() {

    inner class ImageSliderViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSliderViewHolder {
        val imageView = ImageView(parent.context)
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return ImageSliderViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageSliderViewHolder, position: Int) {
        val festivalImg = festivalImgList[position].originimgurl

        // Glide를 사용하여 이미지 로딩
        if (festivalImg.isNotEmpty()) {
            // 이미지가 있을 경우 Glide를 사용하여 이미지 로딩
            Glide.with(holder.imageView.context).load(festivalImg).into(holder.imageView)
        } else {
            // 이미지가 없을 경우 기본 이미지 설정
            holder.imageView.setImageResource(R.drawable.noimage)
        }
    }

    override fun getItemCount(): Int = festivalImgList.size

}
