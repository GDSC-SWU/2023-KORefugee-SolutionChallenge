package com.example.korefugee.Voca

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.korefugee.*
import com.example.korefugee.Guide.GuideDocumenr01Activity
import com.example.korefugee.databinding.ActivityVocaSavedBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// api 이용하기 위한 객체
val api = APIS.create()
class VocaSavedActivity : AppCompatActivity() {
    private lateinit var binding : ActivityVocaSavedBinding

    lateinit var category : String
    lateinit var language : String
    lateinit var accesstoken : String
    lateinit var refreshtoken : String
    // api 이용하기 위한 객체
    val api = APIS.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityVocaSavedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent로 category 받아오기
        if (intent.hasExtra("category")&&intent.hasExtra("language")
            &&intent.hasExtra("accesstoken")&&intent.hasExtra("refreshtoken")){
            category = intent.getStringExtra("category").toString()
            language = intent.getStringExtra("language").toString()
            accesstoken = intent.getStringExtra("accesstoken").toString()
            refreshtoken = intent.getStringExtra("refreshtoken").toString()

        }

        binding.back.setOnClickListener {
            finish()
        }

        // api 통신으로 데이터 받아와서
        // 리사이클러 뷰 확정
        api.get_myword("Bearer $accesstoken").enqueue(object : Callback<Word_Saved_list_R_Model> {
            // 응답하면
            override fun onResponse(call: Call<Word_Saved_list_R_Model>, response: Response<Word_Saved_list_R_Model>) {

                Log.d("응답",response.toString())
                Log.d("응답", response.body().toString())
                Log.d("응답",response.errorBody()?.string().toString())

                val my = response.body()

                if (my != null ) {
                    val mywordlist: MutableList<Word_Saved_list_R_Model.Data> = my.data as MutableList<Word_Saved_list_R_Model.Data>
                    binding.recyclerviewSaveword.layoutManager = LinearLayoutManager(this@VocaSavedActivity)
                    binding.recyclerviewSaveword.adapter= MyWordRecyclerAdapter(this@VocaSavedActivity,mywordlist,accesstoken)
                }
            }
            override fun onFailure(call: Call<Word_Saved_list_R_Model>, t: Throwable) {
                // 실패 시
                Log.d("응답",t.message.toString())
            }
        })
    }
}