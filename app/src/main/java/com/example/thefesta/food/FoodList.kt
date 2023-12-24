package com.example.thefesta.food

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thefesta.MainActivity
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
    private var contentId: String = ""
    private val id = MainActivity.prefs.getString("id", "")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFoodListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 설정
        val spanCount = 5   //표시할 아이템 개수
        val gridLayoutManager = GridLayoutManager(requireContext(), spanCount, GridLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = gridLayoutManager
        binding.recyclerView.adapter = customAdapter

        // festival contentid 가져오기
        val bundle = arguments
        if (bundle != null) {
            contentId = bundle.getString("contentid", "") ?: ""
        }

        // 음식점 목록 요청 및 UI 업데이트
        if (!contentId.isNullOrEmpty()) {
            val call: Call<FoodResponse> = foodService.getFoodList(contentId = contentId, id = id)

            call.enqueue(object : Callback<FoodResponse> {
                override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                    if (response.isSuccessful) {
                        val foodResponse: FoodResponse? = response.body()
                        //데이터 확인
                        foodResponse?.recommendDTOList?.forEach { recommendDTO ->
                            Log.d("FoodList_RecommendDTO", recommendDTO.toString())
                        }
                        foodResponse?.areacodeDTO?.let { areacodeDTO ->
                            Log.d("FoodList_AreacodeDTO", "sname: ${areacodeDTO.sname}")
                            binding.foodListIntro.text = "축제와 함께 하는 ${areacodeDTO.sname} 맛집"   // TextView 설정
                        }
                        updateFoodList(foodResponse)   // UI 업데이트
                    } else {
                        Log.e("FoodList", "Failed to fetch data")
                    }
                }
                override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                    Log.e("FoodList", "Network request failed", t)
                    t.printStackTrace()
                }
            })
        } else {
            Log.e("FoodList", "contentId is null or empty")
        }

        // 음식점 클릭 시
        customAdapter.setOnItemClickListener(object : CustomAdapter.OnItemClickListener {
            override fun onItemClick(food: RecommendDTO) {
                // FoodDetail 프래그먼트로 food contentid 전달
                val foodDetailFragment = FoodDetail.newInstance(food.contentid)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.container, foodDetailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        })
    }
    private fun updateFoodList(foodResponse: FoodResponse?) {
        customAdapter.recFoodList = foodResponse?.recommendDTOList
        customAdapter.notifyDataSetChanged()
    }
}