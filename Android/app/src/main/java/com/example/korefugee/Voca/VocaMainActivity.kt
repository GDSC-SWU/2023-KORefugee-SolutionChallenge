package com.example.korefugee.Voca

import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.ERROR
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.korefugee.APIS
import com.example.korefugee.R
import com.example.korefugee.WordList_R_Model
import com.example.korefugee.databinding.VocaPage2Binding
import com.example.korefugee.databinding.VocaPage3Binding
import com.example.korefugee.databinding.VocaPageBinding
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.android.synthetic.main.activity_voca_main.*
import kotlinx.android.synthetic.main.voca_page.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue


class VocaMainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech ?= null

    private lateinit var binding1 : VocaPageBinding
    private lateinit var binding2 : VocaPage2Binding
    private lateinit var binding3 : VocaPage3Binding

    private var btnSpeak: ImageButton? = null

    lateinit var accesstoken:String
    lateinit var refreshtoken:String
    lateinit var language:String

    lateinit var category:String
    lateinit var level:String
    lateinit var date:String

    // api 이용하기 위한 객체
    val api = APIS.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voca_main)
        binding1 = VocaPageBinding.inflate(layoutInflater)
        binding2 = VocaPage2Binding.inflate(layoutInflater)
        binding3 = VocaPage3Binding.inflate(layoutInflater)

        if(intent.getStringExtra("accesstoken").toString() !=null &&
            intent.getStringExtra("refreshtoken").toString() !=null){
            accesstoken = intent.getStringExtra("accesstoken").toString()
            refreshtoken = intent.getStringExtra("refreshtoken").toString()
            language = intent.getStringExtra("language").toString()
            level =  intent.getStringExtra("level").toString()
            category = intent.getStringExtra("category").toString()
            date=intent.getStringExtra("date").toString()
        }

        if(category != "today"){
            date = "0"
        }

        //어댑터 연결하기
        var adapter= ViewPagerAdapter2(accesstoken,date.toInt(), level, category, language)
        voca_pager.adapter=adapter

        binding1.star.setOnClickListener {
            // api에 이미 있는 단어인지 확인
            // 아니라면 추가ㄴ
        }
        check_radio_group.setOnCheckedChangeListener{ group, checkedId ->
            when(checkedId){
                R.id.All ->
                {
                    var adapter= ViewPagerAdapter2(accesstoken, date.toInt(), level, category, language)
                    voca_pager.adapter=adapter
                }
                R.id.Korea ->
                {
                    var adapter= ViewPagerAdapter3(accesstoken, date.toInt(), level, category)
                    voca_pager.adapter=adapter

                }

                R.id.Native -> {
                    //viewpager 2개 만들고, position 넘기는 방법 생각!ㄴ
                    var adapter= ViewPagerAdapter4(accesstoken, date.toInt(), level, category)
                    voca_pager.adapter=adapter

                }
            }
        }


        prev.setOnClickListener {
            var current = voca_pager.currentItem
            if (current == 0){
                voca_pager.setCurrentItem(0, true)
                Toast.makeText(this, "This is the first page", Toast.LENGTH_SHORT).show()
            }
            else{
                voca_pager.setCurrentItem(current-1, true)

            }
        }

        next.setOnClickListener {
            var current = voca_pager.currentItem
            if (current == 30-1){
                voca_pager.setCurrentItem(30-1, true)
                Toast.makeText(this, "This is the last page", Toast.LENGTH_SHORT).show()

            }
            else{
                voca_pager.setCurrentItem(current+1, true)

            }
        }


        btnSpeak = findViewById(R.id.sound)
        btnSpeak!!.isEnabled = false
        tts = TextToSpeech(this, this)

        btnSpeak!!.setOnClickListener {
            speakOut()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun speakOut() {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.KOREAN)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()


        val koreanTranslator = Translation.getClient(options)

        // 와이파이를 이용하기 위한 상태 체크
        var conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()



        api.get_word("Bearer $accesstoken", "${date}" ,"$level",
            "$category")
            .enqueue(object : Callback<WordList_R_Model> {
                // 응답하면
                override fun onResponse(
                    call: Call<WordList_R_Model>,
                    response: Response<WordList_R_Model>
                ) {
                    Log.d("응답",response.toString())
                    Log.d("응답", response.body().toString())
                    Log.d("응답",response.errorBody()?.string().toString())
                    val responsedata = response.body()

                    if (responsedata != null) {
                        val vocalist: List<WordList_R_Model.Data> = responsedata.data
                        if(responsedata.success == "success"){
                            if(check_radio_group.checkedRadioButtonId ==  R.id.All){
                                binding1.Koreantext.text = vocalist[voca_pager.currentItem].words
                                tts!!.setLanguage(Locale.KOREAN)
                                tts!!.speak(vocalist[voca_pager.currentItem].words, TextToSpeech.QUEUE_FLUSH, null,"")
                            }
                            else if(check_radio_group.checkedRadioButtonId ==  R.id.Korea){
                                binding2.Koreantext.text = vocalist[voca_pager.currentItem].words
                                tts!!.setLanguage(Locale.KOREAN)
                                tts!!.speak(vocalist[voca_pager.currentItem].words, TextToSpeech.QUEUE_FLUSH, null,"")

                            }
                            else if(check_radio_group.checkedRadioButtonId ==  R.id.Native){
                                binding3.Nativetext.text = vocalist[voca_pager.currentItem].words
                                val mtValue = measureTimedValue {
                                    koreanTranslator.downloadModelIfNeeded(conditions)
                                        .addOnSuccessListener {
                                            koreanTranslator.translate(binding3.Nativetext.text.toString())
                                                .addOnSuccessListener { translatedText ->
                                                    // Translation successful.

                                                    tts!!.setLanguage(Locale.ENGLISH)
                                                    tts!!.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null,"")

                                                }
                                                .addOnFailureListener { exception ->
                                                    Log.e("ssssssss1", exception.toString())
                                                }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e("ssssssss2", exception.toString())
                                        }
                                }
                                Log.d("S",binding3.Nativetext.text.toString())
                            }
                        }

                    }
                }

                override fun onFailure(call: Call<WordList_R_Model>, t: Throwable) {
                    // 실패 시
                    Log.d("응답",t.message.toString())
                    Log.d("응답","fail")
                }
            })
    }

    public override fun onDestroy() {
        // Shutdown TTS when
        // activity is destroyed
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    override fun onInit(p0: Int) {

        if (p0 != ERROR) {
            val result1 = tts!!.setLanguage(Locale.KOREAN)

            if (check_radio_group.checkedRadioButtonId ==  R.id.Native){}
            if (result1 == TextToSpeech.LANG_MISSING_DATA ||
                result1 == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            } else {
                btnSpeak!!.isEnabled = true
            }
        }
    }


}