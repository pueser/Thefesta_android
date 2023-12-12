
package com.example.thefesta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.thefesta.bottomnavi.Board
import com.example.thefesta.bottomnavi.Festival
import com.example.thefesta.bottomnavi.Scheduler
import com.example.thefesta.bottomnavi.User
import com.example.thefesta.databinding.ActivityMainBinding
import com.example.thefesta.permissioncheck.PermissionUtil

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PermissionUtil(this).checkPermissions()

        //AdminAcitity 넘어가는 버튼(Main activtiy에 admin_btn 추가)
        val intent = Intent(this, AdminActivity::class.java)
        binding.adminbutton.setOnClickListener {
            startActivity(Intent(this@MainActivity, AdminActivity::class.java))
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
                    with(supportFragmentManager.beginTransaction()){
                        val user = User()
                        replace(R.id.container, user)
                        commit()
                    }
                    return@setOnItemSelectedListener  true
                }

            }
            return@setOnItemSelectedListener false
        }


    }
}