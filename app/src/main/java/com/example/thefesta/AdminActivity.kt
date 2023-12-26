package com.example.thefesta

import AdminFestival
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.thefesta.admin.adminfesta.admin.AdminFestaQuestionRegister
import com.example.thefesta.adminbottomnavi.AdminBoard
import com.example.thefesta.adminbottomnavi.AdminMember
import com.example.thefesta.adminbottomnavi.AdminQuestion
import com.example.thefesta.adminbottomnavi.AdminReport
import com.example.thefesta.bottomnavi.Festival
import com.example.thefesta.databinding.ActivityAdminBinding
import com.example.thefesta.member.Login
import com.example.thefesta.member.Util.PreferenceUtil
import com.example.thefesta.permissioncheck.PermissionUtil
import com.example.thefesta.retrofit.MemberClient
import com.example.thefesta.service.IMemberService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminActivity : AppCompatActivity() {
    lateinit var binding: ActivityAdminBinding
    private lateinit var memberService: IMemberService

    companion object {
        lateinit var prefs: PreferenceUtil
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AdminActivity.prefs = PreferenceUtil(applicationContext)
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        memberService = MemberClient.retrofit.create(IMemberService::class.java)

        PermissionUtil(this).checkPermissions()

        supportFragmentManager.beginTransaction()
            .replace(R.id.container_admin, AdminFestival())
            .commit()

        updateButtonVisibility()

        binding.loginbutton.setOnClickListener {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container_admin, Login()) // Changed from R.id.container to R.id.container_admin
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.logoutbutton.setOnClickListener {
            val id = AdminActivity.prefs.getString("id", "")

            val call: Call<String> = memberService.logout(id)
            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.body() == "success") {
                        AdminActivity.prefs.setString("id", "")
                        updateButtonVisibility()

                        // Navigate back to MainActivity
                        val intent = Intent(this@AdminActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Close the current activity
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    // Handle failure
                }
            })
        }

        binding.bottomAdminNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                //첫번째 버튼 클릭
                R.id.adminfestival -> {
                    with(supportFragmentManager.beginTransaction()) {
                        val adminfestival = AdminFestival()
                        replace(R.id.container_admin, adminfestival) // Changed from R.id.container to R.id.container_admin
                        commit()
                    }
                    return@setOnItemSelectedListener true
                }
                //두번째 버튼 클릭
                R.id.adminboard -> {
                    with(supportFragmentManager.beginTransaction()) {
                        val adminboard = AdminBoard()
                        replace(R.id.container_admin, adminboard) // Changed from R.id.container to R.id.container_admin
                        commit()
                    }
                    return@setOnItemSelectedListener true
                }
                //세번째 버튼 클릭
                R.id.adminuser -> {
                    with(supportFragmentManager.beginTransaction()) {
                        val adminmember = AdminFestaQuestionRegister()
                        replace(R.id.container_admin, adminmember) // Changed from R.id.container to R.id.container_admin
                        commit()
                    }
                    return@setOnItemSelectedListener true
                }
                //네번째 버튼 클릭
                R.id.adminreport -> {
                    with(supportFragmentManager.beginTransaction()) {
                        val adminreport = AdminReport()
                        replace(R.id.container_admin, adminreport) // Changed from R.id.container to R.id.container_admin
                        commit()
                    }
                    return@setOnItemSelectedListener true
                }
                //다번째 버튼 클릭
                R.id.adminquestion -> {
                    with(supportFragmentManager.beginTransaction()) {
                        val adminquestion = AdminQuestion()
                        replace(R.id.container_admin, adminquestion) // Changed from R.id.container to R.id.container_admin
                        commit()
                    }
                    return@setOnItemSelectedListener true
                }
            }
            return@setOnItemSelectedListener false
        }
    }

    private fun updateButtonVisibility() {
        val id = AdminActivity.prefs.getString("id", "")
        if (id.isNullOrEmpty()) {
            // id가 null이거나 빈 문자열인 경우
            binding.loginbutton.visibility = View.VISIBLE
            binding.logoutbutton.visibility = View.GONE
        } else {
            // id가 null이 아닌 경우
            binding.loginbutton.visibility = View.GONE
            binding.logoutbutton.visibility = View.VISIBLE
        }
    }
}
