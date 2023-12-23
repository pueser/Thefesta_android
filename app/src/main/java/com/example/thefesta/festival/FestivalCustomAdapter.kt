package com.example.thefesta.festival

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thefesta.MainActivity
import com.example.thefesta.R
import com.example.thefesta.databinding.FestivalItemBinding
import com.example.thefesta.model.festival.AreacodeDTO
import com.example.thefesta.model.festival.FestivalItemDTO
import com.example.thefesta.retrofit.FestivalClient
import com.example.thefesta.service.IFestivalService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date

class FestivalCustomAdapter: RecyclerView.Adapter<FestivalHolder>() {
    private val festivalService: IFestivalService =
        FestivalClient.retrofit.create(IFestivalService::class.java)
    private var lastKeyword: String? = null
    var festivalList: MutableList<FestivalItemDTO>? = mutableListOf()
    var areaCodeList: List<AreacodeDTO>? = null
    private val id = MainActivity.prefs.getString("id", "")

    // 축제 클릭
    interface OnItemClickListener {
        fun onItemClick(festival: FestivalItemDTO)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }

    // 좋아요 버튼 클릭
    interface OnLikeButtonClickListener {
        fun onLikeButtonClick(festival: FestivalItemDTO, position: Int)
    }

    private var likeButtonClickListener: OnLikeButtonClickListener? = null

    fun setOnLikeButtonClickListener(listener: OnLikeButtonClickListener) {
        this.likeButtonClickListener = listener
    }

    //
    fun setAreaCodeListCustom(newList: List<AreacodeDTO>) {
        this.areaCodeList = newList
    }

    //
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FestivalHolder {
        val binding = FestivalItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FestivalHolder(binding, likeButtonClickListener!!)
    }

    override fun getItemCount(): Int {
        return festivalList?.size ?:0
    }

    override fun onBindViewHolder(holder: FestivalHolder, position: Int) {
        val festival = festivalList?.get(position)
        holder.setFestival(festival, position, areaCodeList)

        checkLikeStatus(festival?.contentid ?: "", holder)

        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(festival!!)
        }
    }

    private fun checkLikeStatus(contentid: String, holder: FestivalHolder) {
        festivalService.likeSearch(contentid = contentid, id = id)
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        val result: Int? = response.body()
                        val likeImageResource = if (result == 1) {
                            R.drawable.selstar
                        } else {
                            R.drawable.star
                        }
                        if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                            holder.binding.likeBtn.setImageResource(likeImageResource)
                            festivalList?.get(holder.adapterPosition)?.likeStatus = (result == 1)
                        }
                    } else {
                        holder.binding.likeBtn.setImageResource(R.drawable.star)
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    holder.binding.likeBtn.setImageResource(R.drawable.star)
                }
            })
    }

    fun addFestivalList(newList: List<FestivalItemDTO>, keyword: String? = null) {
        if (!keyword.isNullOrEmpty() && keyword != lastKeyword) {
            festivalList?.clear()
            lastKeyword = keyword
        }
        festivalList?.addAll(newList)
        notifyDataSetChanged()
    }

    fun toggleLikeState(position: Int) {
        festivalList?.get(position)?.likeStatus = !(festivalList?.get(position)?.likeStatus ?: false)
        notifyItemChanged(position)
    }
}

class FestivalHolder(val binding: FestivalItemBinding, private val likeButtonClickListener: FestivalCustomAdapter.OnLikeButtonClickListener): RecyclerView.ViewHolder(binding.root) {
    fun setFestival(festival: FestivalItemDTO?, position: Int, areaCodeList: List<AreacodeDTO>?) {
        festival?.let {
            val festivalImg = it.firstimage
            if (!festivalImg.isNullOrEmpty()) {
                Glide.with(binding.firstimage.context).load(festivalImg).into(binding.firstimage)
            } else {
                binding.firstimage.setImageResource(R.drawable.noimage)
            }

            val festivalTitle = it.title
            if (festivalTitle.length > 0) {
                binding.title.text = festivalTitle
            } else {
                binding.title.text = "제목 없음"
            }

            val festivalAcode = it.acode
            val festivalScode = it.scode

            if (festivalScode != 0 && festivalAcode != 0) {
                areaCodeList?.forEach {areaCode ->
                    if (areaCode.acode == festivalAcode && areaCode.scode == festivalScode) {
                        binding.acode.text = areaCode.aname
                        binding.scode.text = if (areaCode.acode == 8 && areaCode.scode == 1) "" else areaCode.sname
                    }
                }
            } else {
                binding.scode.text = "정보 없음"
            }

            val startDate = festival.eventstartdate.toInt()
            val endDate = festival.eventenddate.toInt()

            val dateFormat = SimpleDateFormat("yyyyMMdd")
            val todayString = dateFormat.format(Date())
            val today = todayString.toInt()
            if (startDate > today) {
                binding.festivalState.text = "축제 예정"
                binding.festivalState.setTextColor(Color.parseColor("#808080"))
            } else if (startDate < today && endDate > today) {
                binding.festivalState.text = "축제 진행 중"
                binding.festivalState.setTextColor(Color.parseColor("#0066ff"))
            } else if (startDate < today && endDate < today) {
                binding.festivalState.text = "축제 종료"
                binding.festivalState.setTextColor(Color.parseColor("#c0392b"))
            }


            binding.likeBtn.setOnClickListener {
                likeButtonClickListener.onLikeButtonClick(festival, position)
            }

            val likeImageResource = if (festival.likeStatus) {
                R.drawable.selstar
            } else {
                R.drawable.star
            }
            binding.likeBtn.setImageResource(likeImageResource)
        }
    }


}