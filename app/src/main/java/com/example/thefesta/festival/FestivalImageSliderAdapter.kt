package com.example.thefesta.festival

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thefesta.R
import com.example.thefesta.model.festival.FestivalImgItemDTO

class FestivalImageSliderAdapter(private val festivalImgList: List<FestivalImgItemDTO>) : RecyclerView.Adapter<FestivalImageSliderAdapter.ImageSliderViewHolder>() {

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

        if (festivalImg.isNotEmpty()) {
            Glide.with(holder.imageView.context).load(festivalImg).into(holder.imageView)
        } else {
            holder.imageView.setImageResource(R.drawable.noimage)
        }
    }

    override fun getItemCount(): Int = festivalImgList.size

}
