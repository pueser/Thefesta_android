package com.example.thefesta.member

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import com.example.thefesta.MainActivity
import com.example.thefesta.R
import com.example.thefesta.bottomnavi.Festival
import com.example.thefesta.model.member.MemberDTO
import com.example.thefesta.retrofit.MemberClient
import com.example.thefesta.service.IMemberService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PwReset : Fragment() {

    private lateinit var memberService: IMemberService
    private lateinit var idEditText: EditText
    private lateinit var verificationCodeEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordReEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pw_reset, container, false)  // 수정된 부분

        idEditText = view.findViewById(R.id.pwResetId)
        verificationCodeEditText = view.findViewById(R.id.pwResetVerificationCode)
        passwordEditText = view.findViewById(R.id.pwResetPassword)
        passwordReEditText = view.findViewById(R.id.pwResetRePassword)

        val pwResetVerificationCodeSend: Button = view.findViewById(R.id.pwResetVerificationCodeSend)
        val pwResetVerificationCodeCheck: Button = view.findViewById(R.id.pwResetVerificationCodeCheck)
        val pwResetButton: Button = view.findViewById(R.id.pwResetButton)
        val pwResetCencelButton: Button = view.findViewById(R.id.pwResetCencelButton)

        var randomCode: String? = null

        memberService = MemberClient.retrofit.create(IMemberService::class.java)


        // 인증번호 전송 버튼
        pwResetVerificationCodeSend.setOnClickListener {
            val id = idEditText.text.toString()
            val nickname = ""
            val password = passwordEditText.text.toString()

            val mDto = MemberDTO(id, nickname, password)

            val call: Call<String> = memberService.mailSend(mDto)
            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        randomCode = response.body()
                        Log.d("FragmentPwReset", "인증번호 전송 완료 : $randomCode")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {

                }
            })
        }

        // 인증번호 확인 버튼
        pwResetVerificationCodeCheck.setOnClickListener {
            val verificationCode = verificationCodeEditText.text.toString()

            if (randomCode == verificationCode) {
                val errorMessage = "인증이 완료되었습니다."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetVerificationCodeError)
                errorTextView?.text = errorMessage
            } else {
                val errorMessage = "인증번호가 일치하지 않습니다."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetVerificationCodeError)
                errorTextView?.text = errorMessage
            }
        }

        // 비밀번호 재설정 버튼
        pwResetButton.setOnClickListener {

            val id = idEditText.text.toString()
            val nickname = ""
            val password = passwordEditText.text.toString()

            val mDto = MemberDTO(id, nickname, password)

            Log.d("FragmentPwReset", "${mDto}")
            val call: Call<Void> = memberService.pwReset(mDto)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {

                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                }
            })
        }

        // 취소 버튼
        pwResetCencelButton.setOnClickListener {

            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return view  // 수정된 부분
    }
}