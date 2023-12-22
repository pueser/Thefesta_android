package com.example.thefesta.member

import android.annotation.SuppressLint
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
import java.text.SimpleDateFormat
import java.util.Date

class Join : Fragment() {
    private lateinit var memberService: IMemberService
    private lateinit var idEditText: EditText
    private lateinit var nicknameEditText: EditText
    private lateinit var verificationCodeEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordReEditText: EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_join, container, false)

        idEditText = view.findViewById(R.id.joinId)
        nicknameEditText = view.findViewById(R.id.joinNickname)
        verificationCodeEditText = view.findViewById(R.id.joinVerificationCode)
        passwordEditText = view.findViewById(R.id.joinPassword)
        passwordReEditText = view.findViewById(R.id.joinRePassword)
        val joinIdCheck: Button = view.findViewById(R.id.joinIdCheck)
        val joinNicknameCheck: Button = view.findViewById(R.id.joinNicknameCheck)
        val joinVerificationCodeSend: Button = view.findViewById(R.id.joinVerificationCodeSend)
        val joinVerificationCodeCheck: Button = view.findViewById(R.id.joinVerificationCodeCheck)
        val joinButton: Button = view.findViewById(R.id.joinButton)
        val joinCencelButton: Button = view.findViewById(R.id.joinCencelButton)

        var randomCode: String? = null

        memberService = MemberClient.retrofit.create(IMemberService::class.java)

        // 아이디 중복체크 버튼
        joinIdCheck.setOnClickListener {

            val id = idEditText.text.toString()
            val nickname = nicknameEditText.text.toString()
            val password = passwordEditText.text.toString()

            val mDto = MemberDTO(id, nickname, password)

            Log.d("FragmentJoin", "Id: $id, Password: $password, Nickname: $nickname")

            val call: Call<MemberDTO> = memberService.selMember(mDto)
            call.enqueue(object : Callback<MemberDTO> {
                override fun onResponse(call: Call<MemberDTO>, response: Response<MemberDTO>) {
                    if (response.isSuccessful) {
                        val memInfo: MemberDTO? = response.body()
                        Log.d("FragmentJoin", "response: $memInfo")

                        if (memInfo != null) {
                            handleExistingMember(memInfo)
                        }
                    }
                }

                override fun onFailure(call: Call<MemberDTO>, t: Throwable) {
                    Log.e("FragmentJoin", "Retrofit onFailure: ${t.message}", t)
                    val errorMessage = "사용 가능한 아이디입니다."
                    val errorTextView: TextView? = view?.findViewById(R.id.joinIdError)
                    errorTextView?.text = errorMessage
                }
            })

        }

        // 닉네임 중복체크 버튼
        joinNicknameCheck.setOnClickListener {

            val id = idEditText.text.toString()
            val nickname = nicknameEditText.text.toString()
            val password = passwordEditText.text.toString()

            val mDto = MemberDTO(id, nickname, password)

            Log.d("FragmentJoin", "Id: $id, Password: $password, Nickname: $nickname")

            val call: Call<MemberDTO> = memberService.selMember(mDto)
            call.enqueue(object : Callback<MemberDTO> {
                override fun onResponse(call: Call<MemberDTO>, response: Response<MemberDTO>) {
                    if (response.isSuccessful) {
                        val memInfo: MemberDTO? = response.body()
                        Log.d("FragmentJoin", "memInfo : $memInfo")
                        val stateCode = memInfo?.statecode;
                        if (stateCode == "3") {
                            if (mDto.nickname == memInfo.nickname) {
                                val errorMessage = "사용 가능한 닉네임입니다."
                                val errorTextView: TextView? = view?.findViewById(R.id.joinNicknameError)
                                errorTextView?.text = errorMessage
                            } else {
                                nicknameCheck(mDto)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<MemberDTO>, t: Throwable) {
                    nicknameCheck(mDto)
                }
            })
        }

        // 인증번호 전송 버튼
        joinVerificationCodeSend.setOnClickListener {
            val id = idEditText.text.toString()
            val nickname = nicknameEditText.text.toString()
            val password = passwordEditText.text.toString()

            val mDto = MemberDTO(id, nickname, password)

            val call: Call<String> = memberService.mailSend(mDto)
            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        randomCode = response.body()
                        Log.d("FragmentJoin", "인증번호 전송 완료 : $randomCode")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    nicknameCheck(mDto)
                }
            })
        }

        // 인증번호 확인 버튼
        joinVerificationCodeCheck.setOnClickListener {
            val verificationCode = verificationCodeEditText.text.toString()

            if (randomCode == verificationCode) {
                val errorMessage = "인증이 완료되었습니다."
                val errorTextView: TextView? = view?.findViewById(R.id.joinVerificationCodeError)
                errorTextView?.text = errorMessage
            } else {
                val errorMessage = "인증번호가 일치하지 않습니다."
                val errorTextView: TextView? = view?.findViewById(R.id.joinVerificationCodeError)
                errorTextView?.text = errorMessage
            }
        }

        // 회원가입 버튼
        joinButton.setOnClickListener {
            val id = idEditText.text.toString()
            val nickname = nicknameEditText.text.toString()
            val password = passwordEditText.text.toString()

            val mDto = MemberDTO(id, nickname, password)

            val call: Call<MemberDTO> = memberService.selMember(mDto)
            call.enqueue(object : Callback<MemberDTO> {
                override fun onResponse(call: Call<MemberDTO>, response: Response<MemberDTO>) {
                    if (response.isSuccessful) {
                        val memInfo: MemberDTO? = response.body()
                        Log.d("FragmentJoin", "memInfo : $memInfo")
                        val stateCode = memInfo?.statecode;
                        if (stateCode == "3") {
                            reJoin(mDto)
                        } else {
                            join(mDto)

                            val intent = Intent(activity, MainActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }
                    }
                }

                override fun onFailure(call: Call<MemberDTO>, t: Throwable) {
                    join(mDto)

                }
            })

        }

        // 취소 버튼
        joinCencelButton.setOnClickListener {

            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return view
    }

    private fun handleExistingMember(memInfo: MemberDTO) {
        val stateCode = memInfo.statecode

        Log.d("FragmentJoin", "statecode: $stateCode")
        when (stateCode) {
            "0" -> {
                val errorMessage = "유효한 아이디가 아닙니다."
                val errorTextView: TextView? = view?.findViewById(R.id.joinIdError)
                errorTextView?.text = errorMessage
            }
            "1" -> {
                val errorMessage = "사용중인 아이디입니다. 다시 입력해주세요."
                val errorTextView: TextView? = view?.findViewById(R.id.joinIdError)
                errorTextView?.text = errorMessage
            }
            "2" -> {
                val updateDate: Long? = memInfo.updatedate
                Log.d("FragmentJoin", "response: $memInfo")
                if (updateDate != null) {
                    Log.d("FragmentJoin", "response: $updateDate")
                    val currentDate = System.currentTimeMillis()

                    if (currentDate < updateDate + (7 * 24 * 60 * 60 * 1000)) {
                        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(updateDate + (7 * 24 * 60 * 60 * 1000)))
                        val errorMessage = "*탈퇴한 계정입니다. $date 초 이후에 재가입 가능합니다."
                        val errorTextView: TextView? = view?.findViewById(R.id.joinIdError)
                        errorTextView?.text = errorMessage
                    } else {
                        stateUpdate(memInfo)
                    }
                }
            }
            "3" -> {
                val errorMessage = "사용 가능한 아이디입니다."
                val errorTextView: TextView? = view?.findViewById(R.id.joinIdError)
                errorTextView?.text = errorMessage
            }
            "4" -> {
                val errorMessage = "이용이 제한된 아이디입니다. 관리자에게 문의해주세요."
                val errorTextView: TextView? = view?.findViewById(R.id.joinIdError)
                errorTextView?.text = errorMessage
            }
        }
    }

    // 상태코드 변경 메서드
    private fun stateUpdate(mDto: MemberDTO) {
        mDto.statecode = "3";
        val joinCall: Call<Void> = memberService.updateState(mDto)
        joinCall.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val errorMessage = "사용 가능한 아이디입니다."
                    val errorTextView: TextView? = view?.findViewById(R.id.joinIdError)
                    errorTextView?.text = errorMessage
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("FragmentJoin", "Retrofit onFailure: ${t.message}", t)
            }
        })
    }

    // 닉네임 중복체크
    private fun nicknameCheck(mDto: MemberDTO) {

        val id = idEditText.text.toString()
        val nickname = nicknameEditText.text.toString()
        val password = passwordEditText.text.toString()

        val mDto = MemberDTO(id, nickname, password)

        Log.d("FragmentJoin", "mDto: $mDto")
        val call: Call<String> = memberService.nicknameCheck(mDto)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val result: String? = response.body()
                    if (result != null) {
                        if (result == "success") {
                            val errorMessage = "사용 가능한 닉네임입니다."
                            val errorTextView: TextView? = view?.findViewById(R.id.joinNicknameError)
                            errorTextView?.text = errorMessage
                        } else if (result == "fail") {
                            val errorMessage = "사용중인 닉네임입니다. 다시 입력해주세요."
                            val errorTextView: TextView? = view?.findViewById(R.id.joinNicknameError)
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

    private fun join(mDto: MemberDTO) {

        val id = idEditText.text.toString()
        val nickname = nicknameEditText.text.toString()
        val password = passwordEditText.text.toString()

        val mDto = MemberDTO(id, nickname, password)

        val call: Call<Void> = memberService.joinPost(mDto)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
            }
        })
    }

    private fun reJoin(mDto: MemberDTO) {

        val id = idEditText.text.toString()
        val nickname = nicknameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val statecode = "1"

        val mDto = MemberDTO(id, nickname, password, statecode)

        val call: Call<String> = memberService.memInfoReset(mDto)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
            }
        })
    }
}