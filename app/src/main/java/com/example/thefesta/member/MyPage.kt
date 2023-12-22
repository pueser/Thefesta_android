package com.example.thefesta.member

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.example.thefesta.MainActivity
import com.example.thefesta.R
import com.example.thefesta.databinding.FragmentMyPageBinding
import com.example.thefesta.model.member.MemberDTO
import com.example.thefesta.retrofit.MemberClient
import com.example.thefesta.service.IMemberService
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class MyPage : Fragment() {

    private lateinit var binding: FragmentMyPageBinding
    private lateinit var memberService: IMemberService
    private lateinit var userImageView: ImageView
    private lateinit var nicknameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Activity를 바로 못불러 오기 때문에 bundle 사용
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        memberService = MemberClient.retrofit.create(IMemberService::class.java)
        binding = FragmentMyPageBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.withdrawal.setOnClickListener {
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.container, Withdrawal())
            transaction.addToBackStack(null)
            transaction.commit()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gallery = view.findViewById<Button>(R.id.gallery)
        val camera = view.findViewById<Button>(R.id.camera)
        nicknameTextView = view.findViewById(R.id.myPageNickname)
        userImageView = view.findViewById(R.id.userImageView)


        gallery.setOnClickListener {
            getFromAlbum()
        }

        camera.setOnClickListener {
            takePhoto()
        }

        getMemberInfo()

    }

    fun getFromAlbum() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 102)
    }

    fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            101 -> {
                if (resultCode == Activity.RESULT_OK) {
                    val bitmap = data?.extras?.get("data") as Bitmap

                    val now = Date()
                    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
                    val fileName = "IMG_${sdf.format(now)}.jpg"

                    val file = File(requireContext().cacheDir, fileName)
                    val path = file.absolutePath

                    if (file.exists()) {
                        Log.d("이미지 주소 카메라 : ", "${path}")
                    }

                    val userImageView = view?.findViewById<ImageView>(R.id.userImageView)
                    userImageView?.setImageBitmap(bitmap)

                    val uri = saveImageToGallery(bitmap, fileName, "camera picture")

                    var selectedImageUri = uri

                    Log.d("이미지 주소 카메라 촬영 : ", "${selectedImageUri}")
                    uploadImage(selectedImageUri,requireContext())
                }
            }
            102 -> {
                if(resultCode == Activity.RESULT_OK){
                    if(Build.VERSION.SDK_INT >= 19){
                        var selectedImageUri = data?.data
                        Log.d("MyPage", "selectedImageUri : ${selectedImageUri}")
                        if (selectedImageUri != null){
                            val bitmap = BitmapFactory.decodeStream(requireActivity().contentResolver.openInputStream(selectedImageUri!!))
                            Log.d("MyPage", "bitmap : ${bitmap}")
                            val userImageView = view?.findViewById<ImageView>(R.id.userImageView)
                            userImageView?.setImageBitmap(bitmap)
                        }
                        uploadImage(selectedImageUri, requireContext())
                    }
                }
            }
        }
    }

    fun uploadImage(uri: Uri?, context: Context) {

        val file  = File(getRealPathFromURI(uri, context))

        // 파일을 multipart/form-data 형식으로 변경
        val requestBody = RequestBody.create(MediaType.parse("image/*"), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)

        val retrofit = MemberClient.retrofit
        retrofit.create(IMemberService::class.java).changeAjaxAction(body)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    Log.d("MyPage", "왔나...")
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("이미지 등록 결과", t.stackTraceToString())
                }
            })
    }


    fun getRealPathFromURI(uri: Uri?, context: Context):String {
        Log.d("MyPage", "도착? ${uri}")

        if (uri == null) {
            return ""
        }
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor?= context.contentResolver.query(uri, projection, null, null, null)

        Log.d("MyPage", "projection: ${projection}")
        Log.d("MyPage", "cursor: ${cursor}")

        try {
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val filePath = it.getString(columnIndex)
                    Log.d("MyPage", "columnIndex: ${columnIndex}")
                    Log.d("MyPage", "File path: ${filePath}")
                    return filePath ?:""
                }
            }
        } finally {
            cursor?.close()
        }
        Log.e("MyPage", "Failed to get file path from URI")
        return ""
    }


    private fun saveImageToGallery(bitmap: Bitmap, title: String, description: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, title)
            put(MediaStore.Images.Media.DESCRIPTION, description)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        val resolver = requireContext().contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            val outputStream = resolver.openOutputStream(it)
            outputStream?.use {
                    stream -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
        }
        return uri
    }

    fun getMemberInfo() {
        val id = MainActivity.prefs.getString("id", "")

        Log.d("MyPage", "getMemberInfo 도착?")
        val retrofit = MemberClient.retrofit
        retrofit.create(IMemberService::class.java).selMember(MemberDTO(id = id))
            .enqueue(object : Callback<MemberDTO> {
                override fun onResponse(call: Call<MemberDTO>, response: Response<MemberDTO>) {
                    if (response.isSuccessful) {
                        val memberInfo = response.body()
                        val nickname = memberInfo?.nickname
                        memberInfo?.let {
                            nicknameTextView.text = nickname
                            val profile = it.profileImg
                            val profileImg = "http://192.168.4.44:9090/resources/fileUpload/" + profile;
//                            val profileImg = "http://192.168.0.10:9090/resources/fileUpload/" + profile;

                            Log.d("MyPage", "profileImg ${profileImg}")

                            if (!profileImg.isNullOrEmpty()) {
                                Log.d("MyPage", "isNullOrEmpty")
                                Glide.with(requireContext())
                                    .load(profileImg)
                                    .into(userImageView)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<MemberDTO>, t: Throwable) {
                    Log.e("MyPage", "Failed to get member info", t)
                }
            })
    }

}