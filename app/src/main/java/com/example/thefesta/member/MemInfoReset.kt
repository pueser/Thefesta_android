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
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import com.example.thefesta.MainActivity
import com.example.thefesta.R
import com.example.thefesta.model.member.MemberDTO
import com.example.thefesta.retrofit.MemberClient
import com.example.thefesta.service.IMemberService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MemInfoReset : Fragment() {
    private lateinit var memberService: IMemberService
    private lateinit var idTextView: TextView
    private lateinit var nicknameEditText: EditText
    private lateinit var originalPasswordEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordReEditText: EditText
    private var nicknameChackResult = "fail"

    private var memInfo: MemberDTO? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mem_info_reset, container, false)  // 수정된 부분

        idTextView = view.findViewById(R.id.memInfoResetId)
        nicknameEditText = view.findViewById(R.id.memInfoResetNickname)
        originalPasswordEditText = view.findViewById(R.id.memInfoResetOriginalPassword)
        passwordEditText = view.findViewById(R.id.memInfoResetPassword)
        passwordReEditText = view.findViewById(R.id.memInfoResetRePassword)

        val memInfoResetNicknameCheck: Button = view.findViewById(R.id.memInfoResetNicknameCheck)
        val memInfoResetButton: Button = view.findViewById(R.id.memInfoResetButton)
        val memInfoResetCencelButton: Button = view.findViewById(R.id.memInfoResetCencelButton)

        memberService = MemberClient.retrofit.create(IMemberService::class.java)
        idTextView.setText(MainActivity.prefs.getString("id", ""))

        val id = MainActivity.prefs.getString("id", "")
        var nickname = nicknameEditText.text.toString()

        nicknameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (!charSequence.isNullOrEmpty()) {
                    val errorTextView: TextView? =
                        view?.findViewById(R.id.memInfoResetNicknameError)
                    errorTextView?.text = ""
                    nicknameChackResult = "fail"
                }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (!charSequence.isNullOrEmpty()) {
                    val errorTextView: TextView? =
                        view?.findViewById(R.id.memInfoResetPasswordError)
                    errorTextView?.text = ""
                }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        })

        passwordReEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (!charSequence.isNullOrEmpty()) {
                    val errorTextView: TextView? =
                        view?.findViewById(R.id.memInfoResetRePasswordError)
                    errorTextView?.text = ""
                }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        })

        originalPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if (!charSequence.isNullOrEmpty()) {
                    val errorTextView: TextView? = view?.findViewById(R.id.memInfoResetOriginalPasswordError)
                    errorTextView?.text = ""
                }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        })
//
        if (nickname == null || nickname == "") {
            nickname = memInfo?.nickname.toString()
        }

        var password = passwordEditText.text.toString()

        if (password == null || password == "") {
            password = memInfo?.password.toString()
        }

        val mDto = MemberDTO(id, nickname, password)

        val call: Call<MemberDTO> = memberService.selMember(mDto)
        call.enqueue(object : Callback<MemberDTO> {
            override fun onResponse(call: Call<MemberDTO>, response: Response<MemberDTO>) {
                if (response.isSuccessful) {
                    memInfo = response.body()!!
                    if (memInfo != null) {
                        Log.d("FragmentMemInfoReset", "$memInfo")
                    } else {

                    }
                }
            }

            override fun onFailure(call: Call<MemberDTO>, t: Throwable) {
                Log.e("FragmentMemInfoReset", "Retrofit onFailure: ${t.message}", t)
            }
        })

        memInfoResetNicknameCheck.setOnClickListener {
            val id = ""
            val nickname = nicknameEditText.text.toString()
            Log.d("FragmentMemInfoReset", "$nickname")

            val mDto = MemberDTO(id, nickname)

            if (nickname != "") {

                if (nickname.length > 10) {
                    val errorMessage = "*닉네임은 최대 10글자까지 입력 가능합니다."
                    val errorTextView: TextView? =
                        view?.findViewById(R.id.memInfoResetNicknameError)
                    errorTextView?.text = errorMessage
                    return@setOnClickListener
                }
                val koreanRegex = Regex("^[ㄱ-힣]+$")

                if (!koreanRegex.matches(nickname)) {
                    Log.d("FragmentMemInfoReset", "여기")
                    val errorMessage = "*한글로 된 닉네임만 사용 가능합니다."
                    val errorTextView: TextView? =
                        view?.findViewById(R.id.memInfoResetNicknameError)
                    errorTextView?.text = errorMessage
                    return@setOnClickListener
                }

                val call: Call<String> = memberService.nicknameCheck(mDto)
                call.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val result: String? = response.body()
                            if (result != null) {
                                if (result == "success") {
                                    val errorMessage = "사용 가능한 닉네임입니다."
                                    val errorTextView: TextView? = view?.findViewById(R.id.memInfoResetNicknameError)
                                    errorTextView?.text = errorMessage
                                    nicknameChackResult = "success"

                                } else if (result == "fail") {
                                    val errorMessage = "사용중인 닉네임입니다. 다시 입력해주세요."
                                    val errorTextView: TextView? = view?.findViewById(R.id.memInfoResetNicknameError)
                                    errorTextView?.text = errorMessage
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.e("FragmentJoin", "여기 도착?")
                        Log.e("FragmentJoin", "Retrofit onFailure: ${t.message}", t)
                    }
                })
            }
        }


        memInfoResetButton.setOnClickListener {

            Log.d("FragmentMemInfoReset", "$nickname : $password")

            var nickname = nicknameEditText.text.toString()
            var originalPass = originalPasswordEditText.text.toString()
            var password = passwordEditText.text.toString()
            var passwordRe = passwordReEditText.text.toString()
            var mDto = MemberDTO(id, nickname, password)

            if (nickname != "") {

                if (nicknameChackResult == "fail") {
                    val errorMessage = "*중복체크를 진행해주세요."
                    val errorTextView: TextView? =
                        view?.findViewById(R.id.memInfoResetNicknameError)
                    errorTextView?.text = errorMessage
                    return@setOnClickListener
                }
            }

            if (nickname == "" || nickname == null) {
                memInfo?.let {
                    nickname = it.nickname.toString()
                }
            }

            Log.d("FragmentMemInfoReset", "닉네임 통과 ${nickname}")

            if (originalPass != "") {

                val originalPassword = originalPasswordEditText.text.toString()
                Log.d("FragmentMemInfoReset", "$originalPassword : ${memInfo?.password}")
                if (originalPass != memInfo!!.password) {
                    val errorMessage = "비밀번호가 일치하지 않습니다."
                    val errorTextView: TextView? =
                        view?.findViewById(R.id.memInfoResetOriginalPasswordError)
                    errorTextView?.text = errorMessage
                    return@setOnClickListener
                } else {

                    if (password == "") {
                        if (password.isEmpty()) {
                            val errorMessage = "*변경할 비밀번호를 입력해주세요."
                            val errorTextView: TextView? = view?.findViewById(R.id.memInfoResetPasswordError)
                            errorTextView?.text = errorMessage
                            return@setOnClickListener
                        }
                    }

                if (password != "") {

                    if (password.length < 8 || password.length > 16) {
                        val errorMessage = "*비밀번호는 최소 8글자, 최대 16글자까지 입력할 수 있습니다."
                        val errorTextView: TextView? =
                            view?.findViewById(R.id.memInfoResetPasswordError)
                        errorTextView?.text = errorMessage
                        return@setOnClickListener
                    }

                    val lowercaseRegex = Regex("[a-z]")
                    val numberRegex = Regex("[0-9]")
                    val specialCharacterRegex = Regex("[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\\\/-]")

                    if (!lowercaseRegex.containsMatchIn(password) || !numberRegex.containsMatchIn(
                            password
                        ) || !specialCharacterRegex.containsMatchIn(password)
                    ) {
                        val errorMessage = "*비밀번호에는 소문자, 숫자, 특수문자가 한 자 이상 포함되어야 합니다."
                        val errorTextView: TextView? =
                            view?.findViewById(R.id.memInfoResetPasswordError)
                        errorTextView?.text = errorMessage
                        return@setOnClickListener
                    }

                    if (passwordRe.isEmpty()) {
                        val errorMessage = "*비밀번호를 재입력해주세요."
                        val errorTextView: TextView? =
                            view?.findViewById(R.id.memInfoResetRePasswordError)
                        errorTextView?.text = errorMessage
                        return@setOnClickListener
                    }

                    if (password != passwordRe) {
                        val errorMessage = "*비밀번호와 일치하지 않습니다."
                        val errorTextView: TextView? =
                            view?.findViewById(R.id.memInfoResetRePasswordError)
                        errorTextView?.text = errorMessage
                        return@setOnClickListener
                    }
                    if (password == "" || password == null) {
                        memInfo?.let {
                            password = it.password.toString()
                        }
                    }
                }
            }
            }

            if (password == "") {
                memInfo?.let {
                    password = it.password.toString()
                    val mDto = MemberDTO(id, nickname, password)
                    Log.d("FragmentMemInfoReset", "여기? $mDto")
                    memInfoReset(mDto)
                }
            }

            mDto = MemberDTO(id, nickname, password)
            Log.d("FragmentMemInfoReset", "$nickname :after: $password")
            memInfoReset(mDto)
        }

        memInfoResetCencelButton.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
            Log.d("FragmentMemInfoReset", "메인으로 돌아옴!")
        }

        return view
    }

    private fun memInfoReset(mDto: MemberDTO) {
        Log.d("FragmentMemInfoReset", "${mDto.nickname}, ${mDto.password}")
        val joinCall: Call<String> = memberService.memInfoReset(mDto)
        joinCall.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Log.d("FragmentMemInfoReset", "변경 성공")
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("FragmentMemInfoReset", "Retrofit onFailure: ${t.message}", t)
            }
        })
    }
}