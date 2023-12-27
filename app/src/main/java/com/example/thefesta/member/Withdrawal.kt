package com.example.thefesta.member

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.thefesta.MainActivity
import com.example.thefesta.R
import com.example.thefesta.model.member.MemberDTO
import com.example.thefesta.retrofit.MemberClient
import com.example.thefesta.service.IMemberService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Withdrawal : Fragment() {

    private lateinit var memberService: IMemberService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_withdrawal, container, false)

        val loginInfo = MainActivity.prefs.getString("id", "")
        val idEditText: EditText = view.findViewById(R.id.withdrawalId)
        val passwordEditText: EditText = view.findViewById(R.id.withdrawalPassword)
        val withdrawalButton: Button = view.findViewById(R.id.withdrawalButton)
        val withdrawalCencelButton: Button = view.findViewById(R.id.withdrawalCencelButton)

        memberService = MemberClient.retrofit.create(IMemberService::class.java)

        idEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if (!charSequence.isNullOrEmpty()) {
                    val errorTextView: TextView? = view?.findViewById(R.id.withdrawalIdError)
                    errorTextView?.text = ""
                }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if (!charSequence.isNullOrEmpty()) {
                    val errorTextView: TextView? = view?.findViewById(R.id.withdrawalPasswordError)
                    errorTextView?.text = ""
                }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        })

        withdrawalButton.setOnClickListener {
            val id = idEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (id.isEmpty()) {
                val errorMessage = "아이디를 입력해주세요."
                val errorTextView: TextView? = view?.findViewById(R.id.withdrawalIdError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                val errorMessage = "비밀번호를 입력해주세요."
                val errorTextView: TextView? = view?.findViewById(R.id.withdrawalPasswordError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

            if (id != null && password.isNotEmpty()) {
                val mDto = MemberDTO(id, null, password)

                val call: Call<MemberDTO> = memberService.selMember(mDto)
                call.enqueue(object : Callback<MemberDTO> {
                    override fun onResponse(call: Call<MemberDTO>, response: Response<MemberDTO>) {
                        if (response.isSuccessful) {
                            val memInfo: MemberDTO? = response.body()

                            if (memInfo != null) {
                                if (memInfo.password != password) {
                                    val errorMessage = "*아이디 또는 비밀번호가 일치하지 않습니다."
                                    val errorTextView: TextView = view.findViewById(R.id.withdrawalPasswordError)
                                    errorTextView.text = errorMessage
                                } else {
                                    val intent = Intent(activity, MainActivity::class.java)
                                    startActivity(intent)
                                    activity?.finish()

                                    stateUpdate(mDto)

                                    Toast.makeText(requireContext(), "회원 탈퇴 되었습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<MemberDTO>, t: Throwable) {
                        val errorMessage = "*아이디 또는 비밀번호가 일치하지 않습니다."
                        val errorTextView: TextView = view.findViewById(R.id.withdrawalPasswordError)
                        errorTextView.text = errorMessage
                    }
                })
            }
        }

        withdrawalCencelButton.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
            Log.d("FragmentLogin", "메인으로 돌아옴!")
        }
//
        return view
    }

    // 상태코드 변경 메서드
    private fun stateUpdate(mDto: MemberDTO) {
        mDto.statecode = "2";
        val joinCall: Call<Void> = memberService.updateState(mDto)
        joinCall.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    MainActivity.prefs.setString("id", "")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("FragmentJoin", "Retrofit onFailure: ${t.message}", t)
            }
        })
    }
}