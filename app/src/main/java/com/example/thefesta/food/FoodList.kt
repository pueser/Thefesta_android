package com.example.thefesta.food

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thefesta.R
import com.example.thefesta.databinding.FragmentFoodListBinding
import com.example.thefesta.model.food.FoodResponse
import com.example.thefesta.model.food.RecommendDTO
import com.example.thefesta.retrofit.FoodClient
import com.example.thefesta.service.IFoodService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FoodList : Fragment() {

    private lateinit var binding: FragmentFoodListBinding
    private val foodService: IFoodService = FoodClient.retrofit.create(IFoodService::class.java)

    private val customAdapter = CustomAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFoodListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 설정
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = customAdapter

        // Retrofit을 사용하여 네트워크 요청 수행
        val call: Call<FoodResponse> = foodService.getFoodList(contentId = "2943607")

        call.enqueue(object : Callback<FoodResponse> {
            override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                if (response.isSuccessful) {
                    val foodResponse: FoodResponse? = response.body()
                    // 받은 데이터로 UI 업데이트
                    updateFoodList(foodResponse)
                    // 데이터 확인
                    foodResponse?.recommendDTOList?.forEach { recommendDTO ->
                        Log.d(
                            "RecommendDTO",
                            "contentid: ${recommendDTO.contentid}, title: ${recommendDTO.title}, addr1: ${recommendDTO.addr1}, firstimage2: ${recommendDTO.firstimage2}, likeCnt: ${recommendDTO.likeCnt} "
                        )
                    }
                    foodResponse?.areacodeDTO?.let { areacodeDTO ->
                        Log.d("AreacodeDTO", "sname: ${areacodeDTO.sname}")
                    }
                    // areacodeDTO의 sname 값으로 TextView 설정
                    foodResponse?.areacodeDTO?.let { areacodeDTO ->
                        binding.foodListIntro.text = "축제와 함께 하는 ${areacodeDTO.sname} 맛집"
                    }


                } else {
                    // 에러 처리
                    Log.e("FoodList", "Failed to fetch data")
                }
            }

            override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                // 실패 처리
                Log.e("FoodList", "Network request failed", t)
                t.printStackTrace()
            }
        })

        // 음식점 클릭 시
        customAdapter.setOnItemClickListener(object : CustomAdapter.OnItemClickListener {
            override fun onItemClick(restaurant: RecommendDTO) {
                // FoodDetail 프래그먼트로 contentId를 전달
                val foodDetailFragment = FoodDetail.newInstance(restaurant.contentid)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.container, foodDetailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        })


    }

    private fun updateFoodList(foodResponse: FoodResponse?) {
        // 받은 데이터로 RecyclerView 업데이트
        customAdapter.recFoodList = foodResponse?.recommendDTOList
        customAdapter.notifyDataSetChanged()
    }
}