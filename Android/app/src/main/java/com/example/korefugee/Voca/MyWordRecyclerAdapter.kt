package com.example.korefugee.Voca

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.korefugee.Guide.GuideDocument02Activity
import com.example.korefugee.Word_Deleted_R_Model
import com.example.korefugee.Word_Saved_list_R_Model
import com.example.korefugee.databinding.ListItemBinding
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.android.synthetic.main.voca_page.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class RecyclerItemViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root)

class MyWordRecyclerAdapter (val context: Context, val itemList: MutableList<Word_Saved_list_R_Model.Data>, var accessToken:String)
    : RecyclerView.Adapter<RecyclerItemViewHolder>() {
    var accesstoken: String = accessToken
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
    : RecyclerItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return RecyclerItemViewHolder(ListItemBinding.inflate(layoutInflater))
    }

    @OptIn(ExperimentalTime::class)
    override fun onBindViewHolder(holder: RecyclerItemViewHolder, position: Int) {
        val data = itemList.get(position)
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.KOREAN)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()

        val koreanTranslator = Translation.getClient(options)
        // 와이파이를 이용하기 위한 상태 체크
        var conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        // view랑 data 연결
        holder.binding.run {
            korean.text=data.words
            val mtValue = measureTimedValue {
                koreanTranslator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener {
                        koreanTranslator.translate(data.words)
                            .addOnSuccessListener { translatedText ->
                                // Translation successful.
                                mothertoungue.text = translatedText.toString()

                            }
                            .addOnFailureListener { exception ->
                                Log.e("ssssssss1", exception.toString())
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ssssssss2", exception.toString())
                    }
            }
            // 번역
            trash.setOnClickListener {
                // api 통신으로 wordID 넘기기
                api.delete_myword("Bearer $accesstoken",data.wordId).enqueue(object :
                    Callback<Word_Deleted_R_Model> {
                    // 응답하면
                    override fun onResponse(call: Call<Word_Deleted_R_Model>, response: Response<Word_Deleted_R_Model>) {
                        Log.d("응답",response.toString())
                        Log.d("응답", response.body().toString())
                        Log.d("응답",response.errorBody()?.string().toString())
                        Toast.makeText(context, "${data.words} has been deleted.", Toast.LENGTH_SHORT).show()

                    }
                    override fun onFailure(call: Call<Word_Deleted_R_Model>, t: Throwable) {
                        // 실패 시
                        Log.d("응답",t.message.toString())
                    }
                })
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}