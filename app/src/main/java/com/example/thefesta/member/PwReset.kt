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
    private var memberState = "fail"
    private var verificationCodeSendResult = "fail"
    private var verificationCodeCheckResult = "fail"

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

        idEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if (!charSequence.isNullOrEmpty()) {
                    val errorTextView: TextView? = view?.findViewById(R.id.pwResetIdError)
                    errorTextView?.text = ""
                    memberState = "fail"
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
                    val errorTextView: TextView? = view?.findViewById(R.id.pwResetPasswordError)
                    errorTextView?.text = ""
                }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        })

        passwordReEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if (!charSequence.isNullOrEmpty()) {
                    val errorTextView: TextView? = view?.findViewById(R.id.pwResetRePasswordError)
                    errorTextView?.text = ""
                }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        })

        verificationCodeEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if (!charSequence.isNullOrEmpty()) {
                    val errorTextView: TextView? = view?.findViewById(R.id.pwResetVerificationCodeError)
                    errorTextView?.text = ""
                }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        })

        // 인증번호 전송 버튼
        pwResetVerificationCodeSend.setOnClickListener {
            val id = idEditText.text.toString()
            val nickname = ""
            val password = passwordEditText.text.toString()

            val mDto = MemberDTO(id, nickname, password)

            if (id.isEmpty()) {
                val errorMessage = "*사용할 아이디를 입력해주세요."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetIdError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

            val call: Call<String> = memberService.mailSend(mDto)
            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        randomCode = response.body()
                        Log.d("FragmentPwReset", "인증번호 전송 완료 : $randomCode")
                        verificationCodeSendResult = "success"
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    val errorMessage = ""
                    val errorTextView: TextView? = view?.findViewById(R.id.pwResetIdError)
                    errorTextView?.text = errorMessage
                }
            })
        }

        // 인증번호 확인 버튼
        pwResetVerificationCodeCheck.setOnClickListener {
            val id = idEditText.text.toString()
            val verificationCode = verificationCodeEditText.text.toString()

            if (id.isEmpty()) {
                val errorMessage = "*사용할 아이디를 입력해주세요."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetIdError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

            if (verificationCodeSendResult == "fail") {
                val errorMessage = "*인증번호를 발급받아주세요."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetVerificationCodeError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

            if (verificationCode.isEmpty()) {
                val errorMessage = "*인증번호를 입력해주세요."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetVerificationCodeError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

            if (randomCode == verificationCode) {
                val errorMessage = "인증이 완료되었습니다."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetVerificationCodeError)
                errorTextView?.text = errorMessage
                verificationCodeCheckResult = "success"
                Log.d("FragmentPwReset", "verificationCodeCheckResult : $verificationCodeCheckResult")
            } else {
                val errorMessage = "인증번호가 일치하지 않습니다."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetVerificationCodeError)
                errorTextView?.text = errorMessage
            }
        }

        // 비밀번호 재설정 버튼
        pwResetButton.setOnClickListener {

            Log.d("FragmentPwReset", "verificationCodeCheckResult : $verificationCodeCheckResult")
            val id = idEditText.text.toString()
            val nickname = ""
            val password = passwordEditText.text.toString()
            val passwordRe = passwordReEditText.text.toString()

            val verificationCode = verificationCodeEditText.text.toString()

            val mDto = MemberDTO(id, nickname, password)

            if (id.isEmpty()) {
                val errorMessage = "*사용할 아이디를 입력해주세요."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetIdError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

            selMember(mDto)

            if (memberState == "fail") {
                return@setOnClickListener
            }

            if (verificationCodeSendResult == "fail") {
                val errorMessage = "*인증번호를 발급받아주세요."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetVerificationCodeError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

            if (verificationCode.isEmpty()) {
                val errorMessage = "*인증번호를 입력해주세요."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetVerificationCodeError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

            if (verificationCodeCheckResult == "fail") {
                val errorMessage = "*인증번호를 확인해주세요."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetVerificationCodeError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                val errorMessage = "*사용할 비밀번호를 입력해주세요."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetPasswordError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

            if (password.length < 8 || password.length > 16) {
                val errorMessage = "*비밀번호는 최소 8글자, 최대 16글자까지 입력할 수 있습니다."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetPasswordError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

            val lowercaseRegex = Regex("[a-z]")
            val numberRegex = Regex("[0-9]")
            val specialCharacterRegex = Regex("[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\\\/-]")

            if (!lowercaseRegex.containsMatchIn(password) || !numberRegex.containsMatchIn(password) || !specialCharacterRegex.containsMatchIn(password)) {
                val errorMessage = "*비밀번호에는 소문자, 숫자, 특수문자가 한 자 이상 포함되어야 합니다."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetRePasswordError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

            if (passwordRe.isEmpty()) {
                val errorMessage = "*비밀번호를 재입력해주세요."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetRePasswordError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

            if (password != passwordRe) {
                val errorMessage = "*비밀번호와 일치하지 않습니다."
                val errorTextView: TextView? = view?.findViewById(R.id.pwResetRePasswordError)
                errorTextView?.text = errorMessage
                return@setOnClickListener
            }

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

    private fun selMember(mDto: MemberDTO) {

        val errorMessage = ""
        val errorTextView: TextView? = view?.findViewById(R.id.pwResetIdError)

        val call: Call<MemberDTO> = memberService.selMember(mDto)
        call.enqueue(object : Callback<MemberDTO> {
            override fun onResponse(call: Call<MemberDTO>, response: Response<MemberDTO>) {
                if (response.isSuccessful) {
                    val memInfo: MemberDTO? = response.body()
                    Log.d("FragmentPwReset", "memInfo : $memInfo")
                    val stateCode = memInfo?.statecode;
                    if (stateCode == "0" || stateCode == "1") {
                        memberState = "sussess"
                    } else if (stateCode == "2" || stateCode == "3") {
                        Toast.makeText(requireContext(), "탈퇴한 계정입니다.", Toast.LENGTH_SHORT).show()
                        errorTextView?.text = errorMessage
                    } else if (stateCode == "4") {
                        Toast.makeText(requireContext(), "이용이 제한된 아이디입니다.", Toast.LENGTH_SHORT).show()
                        errorTextView?.text = errorMessage
                    } else if (stateCode == "") {
                        Toast.makeText(requireContext(), "미가입 된 아이디입니다.", Toast.LENGTH_SHORT).show()
                        errorTextView?.text = errorMessage

                    }
                }
            }

            override fun onFailure(call: Call<MemberDTO>, t: Throwable) {
                Toast.makeText(requireContext(), "미가입 된 아이디입니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}