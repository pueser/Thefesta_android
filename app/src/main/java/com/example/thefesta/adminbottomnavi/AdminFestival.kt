import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thefesta.R
import com.example.thefesta.admin.adminfesta.admin.adminfesta.AdminFestaQuestion
import com.example.thefesta.databinding.FragmentAdminFestivalBinding
import com.example.thefesta.databinding.ItemAdminfestivalDataListBinding
import com.example.thefesta.retrofit.AdminClient
import com.example.thefesta.service.IAdminService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class FestivalData(
    val title: String,
    val contentid: String,
    val addr1: String,
    val eventstartdate: String,
    val eventenddate: String,
    val questioncount: Double?,
) {
    val truncatedAddr1: String
        get() = truncateStringAfterSpace(addr1)

    private fun truncateStringAfterSpace(input: String): String {
        val indexOfSpace = input.indexOf(' ')
        return if (indexOfSpace != -1) {
            input.substring(0, indexOfSpace)
        } else {
            input
        }
    }

    val formattedQuestionCount: String
        get() = when {
            questioncount is Double && questioncount == 0.0 -> "0건"
            else -> "${questioncount?.toInt()}건"
        }
}

class AdminFestival : Fragment() {
    private lateinit var binding: FragmentAdminFestivalBinding
    private var amount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminFestivalBinding.inflate(inflater)
        getAdminFestivalAmount(binding)

        Log.d("adminFestival", "onCreateView 실행")
        return binding.root
    }

    private fun getAdminFestivalAmount(binding: FragmentAdminFestivalBinding) {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).getfestaListAmount()
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        amount = response.body() ?: 0
                        getAdminFestivalList(binding)
                        Log.d("adminFestivalCnt", "amount: ${response.body()}")
                    } else {
                        Log.d("adminFestivalCnt", "Failed to get amount: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("adminFestivalCnt", "Network request failed", t)
                }
            })
    }

    private fun getAdminFestivalList(binding: FragmentAdminFestivalBinding) {
        val retrofit = AdminClient.retrofit
        val pageNum = 1

        var dataList: List<FestivalData>

        retrofit.create(IAdminService::class.java).getfestaList(pageNum, amount)
            .enqueue(object : Callback<Map<String, Object>> {
                override fun onResponse(
                    call: Call<Map<String, Object>>,
                    response: Response<Map<String, Object>>
                ) {
                    if (response.code() == 404) {
                        Log.d("adminFestival", "400에러 : ${response}")
                    } else if (response.code() == 200) {
                        Log.d("adminFestival", "200성공 : ${response.body()}")

                        val questionDtoList =
                            response.body()?.get("questionDto") as? List<Map<String, Any>>
                        dataList = questionDtoList?.mapNotNull {
                            val title = it["title"] as? String
                            val contentid = it["contentid"] as? String
                            val addr1 = it["addr1"] as? String
                            val eventstartdate = it["eventstartdate"] as? String
                            val eventenddate = it["eventenddate"] as? String
                            val questioncount = it["questioncount"] as? Double
                            if (title != null && contentid != null && addr1 != null && eventstartdate != null && eventenddate != null) {
                                FestivalData(
                                    title,
                                    contentid,
                                    addr1,
                                    eventstartdate,
                                    eventenddate,
                                    questioncount
                                )
                            } else {
                                null
                            }
                        } ?: emptyList()

                        binding.recyclerView.layoutManager =
                            LinearLayoutManager(requireContext())
                        binding.recyclerView.adapter =
                            AdminFestaAdapter(dataList, this@AdminFestival::navigateToDetailFragment, this@AdminFestival::deleteClick)
                        binding.recyclerView.addItemDecoration(
                            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
                        )
                    }
                }

                override fun onFailure(call: Call<Map<String, Object>>, t: Throwable) {
                    Log.d("adminFestival", "연결실패")
                }
            })
    }

    private fun navigateToDetailFragment(contentId: String) {
        val detailFragment = AdminFestaQuestion.newInstance(contentId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container_admin, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    // 삭제 버튼 클릭시
    fun deleteClick(contentId: String) {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).postFestaDelete(contentId)
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "${contentId}번 축제가 삭제 되었습니다.", Toast.LENGTH_SHORT).show()
                        // Refresh the festival list after deletion
                        getAdminFestivalList(binding)
                    } else {
                        Log.d("adminFestival", "Failed to delete festival: ${response.code()}")
                        Toast.makeText(
                            requireContext(),
                            "Failed to delete festival",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.d("adminFestival", "Network request failed", t)
                }
            })
    }
}


class AdminFestaAdapter(
    private val dataList: List<FestivalData>,
    private val onItemClicked: (String) -> Unit,
    private val onDeleteClicked: (String) -> Unit
) : RecyclerView.Adapter<AdminFestaAdapter.AdminFestaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminFestaViewHolder {
        val binding = ItemAdminfestivalDataListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminFestaViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: AdminFestaViewHolder, position: Int) {
        val data = dataList[position]
        val binding = holder.binding

        holder.setData(data)
        binding.adminTbody.setOnClickListener {
            onItemClicked(data.contentid)
        }
        binding.deleteClickBtn.setOnClickListener {
            onDeleteClicked(data.contentid)
        }
    }

    inner class AdminFestaViewHolder(val binding: ItemAdminfestivalDataListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(data: FestivalData) {
            binding.apply {
                title.text = data.title
                contentid.text = data.contentid
                addr1.text = data.truncatedAddr1
                eventstartdate.text = data.eventstartdate
                eventenddate.text = data.eventenddate
                questioncount.text = data.formattedQuestionCount
            }
        }
    }
}
