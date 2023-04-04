package com.example.korefugee.Voca

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.korefugee.R
import com.example.korefugee.databinding.ActivityVocalevelBinding

class VocalevelActivity : AppCompatActivity() {
    // 뷰 바인딩을 위한 객체 획득
    lateinit var binding: ActivityVocalevelBinding
    lateinit var category : String
    lateinit var language : String

    lateinit var accesstoken:String
    lateinit var refreshtoken:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 바인딩 기본 작업
        binding= ActivityVocalevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent로 category 받아오기
        if (intent.hasExtra("category")&&intent.hasExtra("language")
            &&intent.hasExtra("accesstoken")&&intent.hasExtra("refreshtoken")){
            category = intent.getStringExtra("category").toString()
            language = intent.getStringExtra("language").toString()
            accesstoken = intent.getStringExtra("accesstoken").toString()
            refreshtoken = intent.getStringExtra("refreshtoken").toString()

        }

        val intent = Intent(this, VocaMainActivity::class.java)

        // 라디오 그룹인 성별 선택 시 저장되는 성별 적용
        binding.levelRadioGroup.setOnCheckedChangeListener{ group, checkedId ->
            when(checkedId){

                R.id.beginner ->
                {
                    intent.putExtra("date", "1")
                    intent.putExtra("category", category)
                    intent.putExtra("level", "Biginner")
                    intent.putExtra("language", language)
                    intent.putExtra("accesstoken", accesstoken)
                    intent.putExtra("refreshtoken", refreshtoken)
                    startActivity(intent)
                    finish()
                }

                R.id.advance ->
                {
                    intent.putExtra("date", "1")
                    intent.putExtra("category", category)
                    intent.putExtra("level", "Advanced")
                    intent.putExtra("language", language)
                    intent.putExtra("accesstoken", accesstoken)
                    intent.putExtra("refreshtoken", refreshtoken)
                    startActivity(intent)
                    finish()
                }

            }
        }

    }
}