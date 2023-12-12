package com.example.thefesta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.thefesta.adminbottomnavi.AdminBoard
import com.example.thefesta.adminbottomnavi.AdminFestival
import com.example.thefesta.adminbottomnavi.AdminLogout
import com.example.thefesta.adminbottomnavi.AdminMember
import com.example.thefesta.adminbottomnavi.AdminReport
import com.example.thefesta.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {
    lateinit var binding: ActivityAdminBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomAdminNavigation.setOnItemSelectedListener {
            when(it.itemId){
                //첫번째 버튼 클릭
                R.id.adminfestival ->{
                    with(supportFragmentManager.beginTransaction()){
                        val adminfestival = AdminFestival()
                        replace(R.id.container, adminfestival)

                        commit()
                    }
                    return@setOnItemSelectedListener true
                }
                //두번째 버튼 클릭
                R.id.adminboard -> {
                    with(supportFragmentManager.beginTransaction()){
                        val adminboard = AdminBoard()
                        replace(R.id.container, adminboard)

                        commit()
                    }
                    return@setOnItemSelectedListener true
                }
                //세번째 버튼 클릭
                R.id.adminuser-> {
                    with(supportFragmentManager.beginTransaction()){
                        val adminmember = AdminMember()
                        replace(R.id.container, adminmember)
                        commit()
                    }
                    return@setOnItemSelectedListener  true
                }
                //네번째 버튼 클릭
                R.id.adminreport-> {
                    with(supportFragmentManager.beginTransaction()){
                        val adminreport = AdminReport()
                        replace(R.id.container, adminreport)
                        commit()
                    }
                    return@setOnItemSelectedListener  true
                }
                //다번째 버튼 클릭
                R.id.adminlogout-> {
                    with(supportFragmentManager.beginTransaction()){
                        val adminlogout = AdminLogout()
                        replace(R.id.container, adminlogout)
                        commit()
                    }
                    return@setOnItemSelectedListener  true
                }
            }
            return@setOnItemSelectedListener false
        }

    }
}