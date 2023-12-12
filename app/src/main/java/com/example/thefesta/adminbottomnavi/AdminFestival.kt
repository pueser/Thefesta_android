package com.example.thefesta.adminbottomnavi


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.thefesta.R
import com.example.thefesta.model.admin.Criteria
import com.example.thefesta.model.admin.QuestionDTO
import com.example.thefesta.model.board.BoardDTO
import com.example.thefesta.retrofit.AdminClient
import com.example.thefesta.service.IAdminService


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * A simple [Fragment] subclass.
 * Use the [AdminFestival.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminFestival : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Activity를 바로 못불러 오기 때문에 bundle 사용
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d("adminFestival","onCreateView 실행")
        getAdminFestivalList()
        val view = inflater.inflate(R.layout.fragment_admin_festival, container, false)
        return view
    }


    //축제리스트 받아오기!!
    fun getAdminFestivalList(){

        val retrofit = AdminClient.retrofit
        val pageNum = 1
        val amount = 10
        retrofit.create(IAdminService::class.java).getfestaList(pageNum, amount)
            .enqueue(object : Callback<QuestionDTO>{
                override fun onResponse(call: Call<QuestionDTO>, response: Response<QuestionDTO>){
                    if (response.code() == 404){
                        Log.d("adminFestival","400에러 : ${response}")
                        //Toast.makeText(this@AdminFestival(), "축제 리스트 받아오기 실패", Toast.LENGTH_SHORT).show()
                    }else if(response.code() == 200){
                        Log.d("adminFestival","200성공 : ${response}")
                        //Toast.makeText(this@AdminFestival(), "축제 리스트 성공", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<QuestionDTO>, t: Throwable) {
                    Log.d("adminFestival","연결실패")
                    //Toast.makeText(this@AdminFestival,"서버와 연결이 실패하였습니다.",Toast.LENGTH_SHORT).show()
                }
            })

    }

    //축제 건의 삭제
    fun postquestionDelete(){

        val retrofit = AdminClient.retrofit
        val questionid = "1"
        retrofit.create(IAdminService::class.java).postquestionDelete(questionid)
            .enqueue(object : Callback<String>{
                override fun onResponse(call: Call<String>, response: Response<String>){
                    if (response.code() == 404){
                        Log.d("adminFestival","400에러 : ${response}")
                        //Toast.makeText(this@AdminFestival(), "축제 리스트 받아오기 실패", Toast.LENGTH_SHORT).show()
                    }else if(response.code() == 200){
                        Log.d("adminFestival","200성공 : ${response}")
                        //Toast.makeText(this@AdminFestival(), "축제 리스트 성공", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("adminFestival","연결실패")
                    //Toast.makeText(this@AdminFestival,"서버와 연결이 실패하였습니다.",Toast.LENGTH_SHORT).show()
                }
            })

    }

}