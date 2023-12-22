package com.example.thefesta.member

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.thefesta.AdminActivity
import com.example.thefesta.MainActivity
import com.example.thefesta.R
import com.example.thefesta.model.member.MemberDTO
import com.example.thefesta.retrofit.MemberClient
import com.example.thefesta.service.IMemberService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date

class Login : Fragment() {

    companion object {
        const val PREFERENCES_NAME = "loginInfo"
    }

    lateinit var memberService: IMemberService
    var isCeck = "0"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val idEditText: EditText = view.findViewById(R.id.loginId)
        val passwordEditText: EditText = view.findViewById(R.id.loginPassword)
        val loginButton: Button = view.findViewById(R.id.loginButton)
        val saveIdCheckBox: CheckBox = view.findViewById(R.id.saveIdCheckBox)
        val isCheckBoxChecked = MainActivity.prefs.getString("saveIdCheckBox", "")
        val idRemember = MainActivity.prefs.getString("idRemember", "")

        memberService = MemberClient.retrofit.create(IMemberService::class.java)

        if (isCheckBoxChecked == "1") {
            saveIdCheckBox.isChecked = true
            idEditText.setText(idRemember)
        } else {
            saveIdCheckBox.isChecked = false
        }

        val onSignUpClick: TextView = view.findViewById(R.id.gotoSignUp)

        onSignUpClick.setOnClickListener {
            val fragmentJoin = Join()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragmentJoin)
            transaction.commit()
        }

        // 비밀번호 찾기 버튼
        val onForgotPasswordClick: TextView = view.findViewById(R.id.gotoForgotPassword)

        onForgotPasswordClick.setOnClickListener {
            val fragmentPwReset = PwReset()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragmentPwReset)
            transaction.commit()
        }

        saveIdCheckBox.setOnCheckedChangeListener { _, isChecked ->
            Log.d("FragmentLogin", "isChek : $isCeck")
            if (isChecked) {
                isCeck = "1"
                MainActivity.prefs.setString("idRemember", idEditText.text.toString())
            } else {
                isCeck = "0"
                MainActivity.prefs.setString("idRemember", "")
            }

            MainActivity.prefs.setString("saveIdCheckBox", isCeck)

        }

        loginButton.setOnClickListener {
            val id = idEditText.text.toString()
            val password = passwordEditText.text.toString()

            val mDto = MemberDTO(id, "", password, "", "", null, null, null, null, null, null)

            Log.d("FragmentLogin", "$mDto")

            val call: Call<MemberDTO> = memberService.loginPost(mDto)
            call.enqueue(object : Callback<MemberDTO> {
                override fun onResponse(call: Call<MemberDTO>, response: Response<MemberDTO>) {
                    if (response.isSuccessful) {
                        val memInfo: MemberDTO? = response.body()

                        Log.d("FragmentLogin", "memInfo: $memInfo")

                        if (memInfo != null) {
                            MainActivity.prefs.setString("id", idEditText.text.toString())
                            when (memInfo.statecode) {
                                "0" -> {
                                    val intent = Intent(activity, AdminActivity::class.java)
                                    startActivity(intent)
                                    activity?.finish()
                                    Log.d("FragmentLogin", "관리자 페이지 접속")
                                }
                                "1" -> {
                                    val intent = Intent(activity, MainActivity::class.java)
                                    startActivity(intent)
                                    activity?.finish()
                                }
                                "2" -> {
                                    val updateDate: Long? = memInfo.updatedate
                                    if (updateDate != null) {
                                        val currentDate = System.currentTimeMillis()

                                        if (currentDate < updateDate + (7 * 24 * 60 * 60 * 1000)) {
                                            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                                                Date(updateDate + (7 * 24 * 60 * 60 * 1000))
                                            )
                                            val errorMessage = "*탈퇴한 계정입니다. $date 초 이후에 재가입 가능합니다."
                                            val errorTextView: TextView = view.findViewById(R.id.loginIdError)
                                            errorTextView.text = errorMessage
                                        } else {
//                                            수정
                                        }
                                    }
                                }
                                "3" -> {
                                    val errorMessage = "*미가입 된 아이디입니다. 회원가입 후 로그인해주세요."
                                    val errorTextView: TextView = view.findViewById(R.id.loginIdError)
                                    errorTextView.text = errorMessage
                                }
                                "4" -> {
                                    Toast.makeText(requireContext(), "영구 차단된 계정입니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                        }
                    } else {
                        Log.e("FragmentLogin", "Server error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<MemberDTO>, t: Throwable) {
                    val errorMessage = "*미가입 된 아이디입니다. 회원가입 후 로그인해주세요."
                    val errorTextView: TextView = view.findViewById(R.id.loginIdError)
                    errorTextView.text = errorMessage

                    Log.e("FragmentLogin", errorMessage, t)
                }

            })
        }

        return view
    }
}