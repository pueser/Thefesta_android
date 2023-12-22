
package com.example.thefesta

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.thefesta.bottomnavi.Board
import com.example.thefesta.bottomnavi.Festival
import com.example.thefesta.bottomnavi.Scheduler
import com.example.thefesta.databinding.ActivityMainBinding
import com.example.thefesta.member.Login
import com.example.thefesta.member.MyPage
import com.example.thefesta.member.Util.PreferenceUtil
import com.example.thefesta.permissioncheck.PermissionUtil
import com.example.thefesta.retrofit.MemberClient
import com.example.thefesta.service.IMemberService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var memberService: IMemberService

    lateinit var binding: ActivityMainBinding

    companion object {
        lateinit var prefs: PreferenceUtil
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        memberService = MemberClient.retrofit.create(IMemberService::class.java)

        PermissionUtil(this).checkPermissions()

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, Festival())
            .commit()

        updateButtonVisibility()

        binding.loginbutton.setOnClickListener {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, Login())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.logoutbutton.setOnClickListener {

            val id = prefs.getString("id", "")

            val call: Call<String> = memberService.logout(id)
            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {

                    if (response.body() == "success") {
                        prefs.setString("id", "")
                        updateButtonVisibility()

                        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.container, Festival())
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                }
            })
        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.festival -> {
                    with(supportFragmentManager.beginTransaction()) {
                        val festival = Festival()
                        replace(R.id.container, festival)
                        commit()
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.scheduler -> {
                    with(supportFragmentManager.beginTransaction()) {
                        val scheduler = Scheduler()
                        replace(R.id.container, scheduler)
                        commit()
                    }
                    return@setOnItemSelectedListener  true
                }
                R.id.board -> {
                    with(supportFragmentManager.beginTransaction()){
                        val board = Board()
                        replace(R.id.container, board)
                        commit()
                    }
                    return@setOnItemSelectedListener  true
                }
                R.id.user -> {
                    val id = MainActivity.prefs.getString("id", "")
                    if (id == "") {
                        val loginFragment = Login()
                        with(supportFragmentManager.beginTransaction()){
                            replace(R.id.container, loginFragment)
                            commit()
                        }
                        return@setOnItemSelectedListener  true
                    } else {
                        val myPageFragment = MyPage()
                        with(supportFragmentManager.beginTransaction()){
                            replace(R.id.container, myPageFragment)
                            commit()
                        }
                        return@setOnItemSelectedListener  true
                    }
                }
            }
            return@setOnItemSelectedListener false
        }

    }

    private fun updateButtonVisibility() {
        val id = MainActivity.prefs.getString("id", "")
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