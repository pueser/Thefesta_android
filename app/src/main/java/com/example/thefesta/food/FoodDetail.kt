package com.example.thefesta.food

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.thefesta.MainActivity
import com.example.thefesta.R
import com.example.thefesta.databinding.FragmentFoodDetailBinding
import com.example.thefesta.model.food.ItemDTO
import com.example.thefesta.model.food.LikeDTO
import com.example.thefesta.model.food.LikeResponse
import com.example.thefesta.retrofit.FoodClient
import com.example.thefesta.service.IFoodService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FoodDetail : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentFoodDetailBinding
    private val foodService: IFoodService = FoodClient.retrofit.create(IFoodService::class.java)
    private lateinit var mapView: MapView
    private val id = MainActivity.prefs.getString("id", "")
    private var mMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFoodDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 음식점 위치 맵 설정
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val contentId = arguments?.getString(ARG_CONTENT_ID)   // food contentId 가져오기
        var isLiked = false   // 좋아요를 누르지 않은 상태로 초기 설정

        // 음식점 상세 요청 및 UI 업데이트
        if (contentId != null) {
            val call: Call<ItemDTO> = foodService.getFoodDetail(contentId = contentId)
            call.enqueue(object : Callback<ItemDTO> {
                override fun onResponse(call: Call<ItemDTO>, response: Response<ItemDTO>) {
                    if (response.isSuccessful) {
                        val itemDto: ItemDTO = response.body()!!
                        Log.d("FoodDetail_ItemDto", itemDto.toString())  //데이터 확인
                        updateFoodDetail(itemDto)   // UI 업데이트

                        // 좋아요 버튼 클릭 시
                        binding.likeBtn.setOnClickListener() {

                            if (!id.isNullOrEmpty()) {
                                Log.d("FoodDetail_id", id)

                                val likeDto = LikeDTO(itemDto.contentid, itemDto.title, itemDto.cat3, id)
                                Log.d("FoodDetail_likeDto", likeDto.toString())

                                if (isLiked) {
                                    binding.likeBtn.setImageResource(R.drawable.emptyheart) // 하트 아이콘 변경

                                    // 좋아요 취소 요청 전송
                                    val unlikeCall: Call<Void> = foodService.postUnlikeFood(likeDto)

                                    unlikeCall.enqueue(object : Callback<Void> {
                                        override fun onResponse(
                                            call: Call<Void>,
                                            response: Response<Void>
                                        ) {
                                            if (response.isSuccessful) {
                                                Log.d("FoodDetail", "좋아요 취소 요청 성공")
                                            } else {
                                                Log.e("FoodDetail", "좋아요 취소 요청 실패")
                                            }
                                        }
                                        override fun onFailure(call: Call<Void>, t: Throwable) {
                                            Log.e("FoodDetail", "네트워크 요청 실패", t)
                                            t.printStackTrace()
                                        }
                                    })
                                    isLiked = false   // 좋아요 상태 업데이트

                                } else {
                                    binding.likeBtn.setImageResource(R.drawable.fullheart)

                                    // 좋아요 요청 전송
                                    val likeCall: Call<Void> = foodService.postLikeFood(likeDto)

                                    likeCall.enqueue(object : Callback<Void> {
                                        override fun onResponse(
                                            call: Call<Void>,
                                            response: Response<Void>
                                        ) {
                                            if (response.isSuccessful) {
                                                Log.d("FoodDetail", "좋아요 요청 성공")
                                            } else {
                                                Log.e("FoodDetail", "좋아요 요청 실패")
                                            }
                                        }
                                        override fun onFailure(call: Call<Void>, t: Throwable) {
                                            Log.e("FoodDetail", "네트워크 요청 실패", t)
                                            t.printStackTrace()
                                        }
                                    })
                                    isLiked = true
                                }
                            } else {
                                isLiked = false
                                Toast.makeText(requireContext(), "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        // 사용자의 좋아요 목록 조회하여 likeBtn 업데이트
                        checkUserLiked(id, itemDto.contentid)

                    } else {
                        Log.e("FoodDetail", "Failed to fetch data")
                    }
                }
                override fun onFailure(call: Call<ItemDTO>, t: Throwable) {
                    Log.e("FoodDetail", "Network request failed", t)
                    t.printStackTrace()
                }
            })
        } else {
            Log.e("FoodDetail", "contentId is null or empty")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val contentId = arguments?.getString(ARG_CONTENT_ID)

        if (contentId != null) {
            val call: Call<ItemDTO> = foodService.getFoodDetail(contentId = contentId)
            call.enqueue(object : Callback<ItemDTO> {
                override fun onResponse(call: Call<ItemDTO>, response: Response<ItemDTO>) {
                    if (response.isSuccessful) {
                        val itemDto: ItemDTO = response.body()!!

                        val mapx = itemDto.mapx?.toDouble() ?: 0.0
                        val mapy = itemDto.mapy?.toDouble() ?: 0.0

                        val foodLocation = LatLng(mapy, mapx)

                        val markerOptions = MarkerOptions()
                        markerOptions.position(foodLocation)
                        markerOptions.title(itemDto.title)

                        mMap?.addMarker(markerOptions)
                        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(foodLocation, 17f))

                        val mMapUiSettings: UiSettings = mMap!!.uiSettings
                        mMapUiSettings.isZoomControlsEnabled = true
                    } else {
                        Log.e("FoodDetail", "Failed to fetch data")
                    }
                }
                override fun onFailure(call: Call<ItemDTO>, t: Throwable) {
                    Log.e("FoodDetail", "Network request failed", t)
                    t.printStackTrace()
                }
            })
        } else {
            Log.e("FoodDetail", "contentId is null or empty")
        }
    }

    private fun updateLikeButton(isUserLiked: Boolean) {
        if (isUserLiked) {
            binding.likeBtn.setImageResource(R.drawable.fullheart)
        } else {
            binding.likeBtn.setImageResource(R.drawable.emptyheart)
        }
    }

    private fun checkUserLiked(id: String, contentid: String) {

        // 사용자의 좋아요 목록을 요청
        foodService.getUserLikeList(id).enqueue(object : Callback<LikeResponse> {
            override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                if (response.isSuccessful) {
                    val likeResponse: LikeResponse? = response.body()

                    // 좋아요 목록 데이터 확인
                    likeResponse?.likeDTOList?.forEach { likeDTO ->
                        Log.d("FoodDetail_LikeDTO", likeDTO.toString())
                    }

                    // 현재 음식점이 사용자의 좋아요 목록에 있는지 확인
                    val isUserLiked =
                        likeResponse?.likeDTOList?.any { it.contentid == contentid } ?: false

                    // 사용자가 현재 음식점을 좋아했는지 여부에 따라 likeBtn 업데이트
                    updateLikeButton(isUserLiked)
                } else {
                    Log.e("FoodDetail", "Failed to get user's liked list")
                }
            }
            override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                Log.e("FoodDetail", "Network request failed", t)
                t.printStackTrace()
            }
        })
    }

    private fun updateFoodDetail(itemDto: ItemDTO?) {
        Log.d("FoodDetail", "Binding is null: ${binding == null}")

        if (itemDto != null) {
            val foodImage = itemDto.firstimage
            Glide.with(binding.firstimage.context)
                .load(foodImage)
                .placeholder(R.drawable.noimage)
                .into(binding.firstimage)

            val title = itemDto.title ?: ""
            val cleanedTitle = cleanTitle(title)
            binding?.title?.text = cleanedTitle
            binding?.addr?.text = itemDto.addr1 ?: ""
            binding?.tel?.text = itemDto.infocenterfood ?: ""
            binding?.opentime?.text = itemDto.opentimefood ?: ""
            binding?.restdate?.text = itemDto.restdatefood ?: ""
            binding?.parking?.text = itemDto.parkingfood ?: ""
            binding?.firstmenu?.text = itemDto.firstmenu ?: ""
            binding?.treatmenu?.text = itemDto.treatmenu ?: ""
            binding?.overview?.text = itemDto.overview ?: "정보 없음"
        } else {
            Log.e("FoodDetail", "ItemDTO is null")
        }
    }

    private fun cleanTitle(title: String): String {
        return title.replace("\\([^)]*\\)".toRegex(), "").trim()
    }

    companion object {
        private const val ARG_CONTENT_ID = "contentId"
        private const val DEFAULT_STRING = ""

        // newInstance를 사용하여 Fragment를 생성하고 contentId를 전달
        fun newInstance(contentId: String): FoodDetail {
            val fragment = FoodDetail()
            val args = Bundle()
            args.putString(ARG_CONTENT_ID, contentId ?: DEFAULT_STRING)
            fragment.arguments = args
            Log.d("FoodDetail_food_contentid", contentId)
            return fragment
        }
    }
}
