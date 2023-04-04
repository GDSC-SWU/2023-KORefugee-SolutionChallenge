package com.example.korefugee.Voca

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.korefugee.APIS
import com.example.korefugee.Guide.GuideDocumenr01Activity
import com.example.korefugee.MY_R_Model
import com.example.korefugee.R
import com.example.korefugee.WordDate_R_Model
import com.example.korefugee.databinding.FragmentGuideBinding
import com.example.korefugee.databinding.FragmentVocaBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VocaFragment : Fragment() {
    private lateinit var vocabinding : FragmentVocaBinding

    // api 이용하기 위한 객체
    val api = APIS.create()

    lateinit var accesstoken:String
    lateinit var refreshtoken:String
    lateinit var language:String
    lateinit var date_voca:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vocabinding = FragmentVocaBinding.inflate(inflater, container, false)
        accesstoken = arguments?.getString("accesstoken").toString()
        refreshtoken = arguments?.getString("refreshtoken").toString()
        language = arguments?.getString("language").toString()

        api.get_worddate("Bearer $accesstoken").enqueue(object : Callback<WordDate_R_Model> {
            // 응답하면
            override fun onResponse(call: Call<WordDate_R_Model>, response: Response<WordDate_R_Model>) {

                Log.d("응답",response.toString())
                Log.d("응답", response.body().toString())
                Log.d("응답",response.errorBody()?.string().toString())

                val my = response.body()

                if (my != null ) {
                    val date: List<WordDate_R_Model.Data> = my.data
                    if(my.success == "success"){
                        // 날짜 정보 받아와서 보여주기! - get api 이용!
                        date_voca = date[0].studyDate.toString()
                        vocabinding.day01Text.setText(date[0].studyDate.toString())
                    }
                }
            }
            override fun onFailure(call: Call<WordDate_R_Model>, t: Throwable) {
                // 실패 시
                Log.d("응답",t.message.toString())
            }
        })

        vocabinding.dayBack.setOnClickListener {
            // 날짜 변경 api
            // 날짜 뒤로 가기
        }
        vocabinding.dayGonext.setOnClickListener {
            // 날짜 변경 api
            // 날짜 앞으로 가기
        }
        // 화면 이동
        vocabinding.daystudybutton.setOnClickListener{
            val intent = Intent(activity, VocaMainActivity::class.java)
            intent.putExtra("category", "today")
            intent.putExtra("date", date_voca)
            intent.putExtra("language", language)
            intent.putExtra("accesstoken", accesstoken)
            intent.putExtra("refreshtoken", refreshtoken)
            startActivity(intent)
        }

        vocabinding.button1Medical.setOnClickListener{

            val intent = Intent(activity, VocalevelActivity::class.java)
            intent.putExtra("category", "Medical")
            intent.putExtra("language", language)
            intent.putExtra("accesstoken", accesstoken)
            intent.putExtra("refreshtoken", refreshtoken)
            startActivity(intent)
        }
        vocabinding.button2Law.setOnClickListener{

            val intent = Intent(activity, VocalevelActivity::class.java)
            intent.putExtra("category", "Law")
            intent.putExtra("language", language)
            intent.putExtra("accesstoken", accesstoken)
            intent.putExtra("refreshtoken", refreshtoken)
            startActivity(intent)
        }
        vocabinding.button3Life.setOnClickListener{

            val intent = Intent(activity, VocalevelActivity::class.java)
            intent.putExtra("category", "Daily")
            intent.putExtra("language", language)
            intent.putExtra("accesstoken", accesstoken)
            intent.putExtra("refreshtoken", refreshtoken)
            startActivity(intent)
        }

        vocabinding.more.setOnClickListener{

            val intent = Intent(activity, VocaSavedActivity::class.java)
            intent.putExtra("category", "saved")
            intent.putExtra("language", language)
            intent.putExtra("accesstoken", accesstoken)
            intent.putExtra("refreshtoken", refreshtoken)
            startActivity(intent)
        }
        return vocabinding.root
    }

}