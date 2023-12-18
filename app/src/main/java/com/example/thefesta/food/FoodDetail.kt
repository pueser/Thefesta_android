package com.example.thefesta.food

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.thefesta.R
import com.example.thefesta.databinding.FragmentFoodDetailBinding
import com.example.thefesta.model.food.ItemDTO
import com.example.thefesta.retrofit.FoodClient
import com.example.thefesta.service.IFoodService
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FoodDetail : Fragment(){

    private lateinit var binding: FragmentFoodDetailBinding
    private val foodService: IFoodService = FoodClient.retrofit.create(IFoodService::class.java)
    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFoodDetailBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 인자에서 contentId를 가져옴
        val contentId = arguments?.getString(ARG_CONTENT_ID)

        // 네트워크 요청 및 UI 업데이트
        if (contentId != null) {

            val call: Call<ItemDTO> = foodService.getFoodDetail(contentId = contentId)

            call.enqueue(object : Callback<ItemDTO> {
                override fun onResponse(call: Call<ItemDTO>, response: Response<ItemDTO>) {
                    if (response.isSuccessful) {
                        val itemDto: ItemDTO = response.body()!!
                        // 받은 데이터로 UI 업데이트
                        updateFoodDetail(itemDto)
                        // 데이터 확인
                        Log.d("ItemDto",
                            "contentid: ${itemDto.contentid}, title: ${itemDto.title}, addr1: ${itemDto.addr1}, infocenterfood: ${itemDto.infocenterfood}," +
                                    "firstmenu: ${itemDto.firstmenu}, treatmenu: ${itemDto.treatmenu}, opentimefood: ${itemDto.opentimefood}," +
                                    "restdatefood: ${itemDto.restdatefood}, parkingfood: ${itemDto.parkingfood}, overview: ${itemDto.overview}," +
                                    "firstimage: ${itemDto.firstimage}, firstimage2: ${itemDto.firstimage2}, mapx: ${itemDto.mapx}, mapy: ${itemDto.mapy}," +
                                    "areadoce: ${itemDto.areacode}, sigungucode: ${itemDto.sigungucode}, cat3: ${itemDto.cat3}")


                        // 맵뷰 생성 및 추가
                        mapView = MapView(requireContext())
                        val mapViewContainer = binding.map
                        mapViewContainer.addView(mapView)

                        // 음식점 위치 입력
                        val mapx = itemDto?.mapx?.toDouble() ?: 0.0
                        val mapy = itemDto?.mapy?.toDouble() ?: 0.0
                        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(mapy, mapx), 2, true)
                        mapView.zoomIn(true);
                        mapView.zoomOut(true);

                        // 맵 마커 추가
                        val marker = MapPOIItem()
                        marker.itemName = itemDto?.title
                        marker.tag = 0
                        marker.mapPoint = MapPoint.mapPointWithGeoCoord(mapy, mapx)
                        marker.markerType = MapPOIItem.MarkerType.BluePin
                        marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin

                        mapView.addPOIItem(marker)

                    } else {
                        // 에러 처리
                        Log.e("FoodDetail", "Failed to fetch data")
                    }
                }
                override fun onFailure(call: Call<ItemDTO>, t: Throwable) {
                    // 실패 처리
                    Log.e("FoodDetail", "Network request failed", t)
                    t.printStackTrace()
                }
            })
        }
    }

    private fun updateFoodDetail(itemDto: ItemDTO?) {
        Log.d("FoodDetail", "Binding is null: ${binding == null}")
        // 받은 데이터를 UI에 표시
        val foodImage = itemDto?.firstimage
        if (!foodImage.isNullOrEmpty()) {
            Glide.with(binding.firstimage.context).load(foodImage).into(binding.firstimage)
        } else {
            binding.firstimage.setImageResource(com.example.thefesta.R.drawable.noimage)
        }
        binding.title.text = itemDto?.title ?: ""
        binding.addr.text = itemDto?.addr1 ?: ""
        binding.tel.text = itemDto?.infocenterfood ?: ""
        binding.opentime.text = itemDto?.opentimefood ?: ""
        binding.restdate.text = itemDto?.restdatefood ?: ""
        binding.parking.text = itemDto?.parkingfood ?: ""
        binding.firstmenu.text = itemDto?.firstmenu ?: ""
        binding.treatmenu.text = itemDto?.treatmenu ?: ""
        binding.overview.text = itemDto?.overview ?: "정보 없음"
    }

    companion object {
        private const val ARG_CONTENT_ID = "contentId"
        // newInstance를 사용하여 Fragment를 생성하고 contentId를 전달
        fun newInstance(contentId: String): FoodDetail {
            val fragment = FoodDetail()
            val args = Bundle()
            args.putString(ARG_CONTENT_ID, contentId)
            fragment.arguments = args
            Log.d("food contentid", contentId)
            return fragment
        }
    }

}