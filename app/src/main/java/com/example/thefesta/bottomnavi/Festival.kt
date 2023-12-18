package com.example.thefesta.bottomnavi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.thefesta.R
import com.example.thefesta.food.FoodList

class Festival : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_festival, container, false)
        val foodBtn: Button = view.findViewById(R.id.foodBtn)
        // foodBtn 버튼 클릭시
        foodBtn.setOnClickListener {
            navigateToFoodListFragment()
        }
        return view
    }
    private fun navigateToFoodListFragment() {

        val foodListFragment = FoodList() // FoodList 프래그먼트의 인스턴스 생성
        val transaction = requireActivity().supportFragmentManager.beginTransaction() // 프래그먼트 트랜잭션 시작
        transaction.replace(R.id.container, foodListFragment) // 현재 프래그먼트를 FoodList 프래그먼트로 교체
        transaction.addToBackStack(null) // 트랜잭션을 백 스택에 추가 (선택 사항)
        transaction.commit()  // 트랜잭션 커밋
    }
}