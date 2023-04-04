package com.example.korefugee.Community

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Patterns
import android.view.View
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.example.korefugee.*
import com.google.gson.JsonPrimitive
import kotlinx.android.synthetic.main.activity_translateloading.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class Translateloading : AppCompatActivity() {

    // api 이용하기 위한 객체
    val api = APIS.create()

    lateinit var language_short: String
    lateinit var path: String
    lateinit var testpath: String
    lateinit var resultpath: String
    lateinit var way: String

    lateinit var accesstoken: String
    lateinit var refreshtoken: String

    lateinit var requestFile : RequestBody

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_translateloading)

        // 이메일 및 비밀번호 받아오기
        if (intent.hasExtra("language_short") && intent.hasExtra("way")) {
            way = intent.getStringExtra("way").toString()
            language_short = intent.getStringExtra("language_short").toString()
            path = intent.getStringExtra("file").toString()
            accesstoken = intent.getStringExtra("accesstoken").toString()
            refreshtoken = intent.getStringExtra("refreshtoken").toString()
        }
        Log.e("aaaaa",language_short)
        Log.e("aaaaa",path)


        if(way == "camera"){
            requestFile = RequestBody.create(MediaType.parse("image/*"), path)
        }
        else if(way =="pdf"){
            requestFile = RequestBody.create(MediaType.parse("applcation/pdf"), path)
        }

        val body = MultipartBody.Part.createFormData("file", "ㅁㅁ", requestFile)


        val loading: ImageView =
            findViewById<View>(com.example.korefugee.R.id.gif_loading) as ImageView
        val gifImage = GlideDrawableImageViewTarget(loading)
        Glide.with(this).load<Any>(com.example.korefugee.R.drawable.loading)
            .into<GlideDrawableImageViewTarget>(gifImage)

        api.post_translate("Bearer $accesstoken", language_short,body).enqueue(
            object : Callback<TRANS_R_Model> {
                // 응답하면
                override fun onResponse(
                    call: Call<TRANS_R_Model>,
                    response: Response<TRANS_R_Model>
                ) {
                    Log.d("응답", response.toString())
                    Log.d("응답", response.body().toString())
                    Log.d("응답", response.errorBody()?.string().toString())

                    val responsedata = response.body()

                    if (responsedata != null) {
                        testpath = responsedata.imgPath
                        Log.d("aa",testpath)
                        // api 연결
                        api.get_translate("http://34.64.122.97:8000/translate/${testpath}/${language_short}")
                            .enqueue(object : Callback<JsonPrimitive> {
                                // 응답하면
                                override fun onResponse(
                                    call: Call<JsonPrimitive>, response: Response<JsonPrimitive>
                                ) {

                                    Log.d("응답a", response.toString())
                                    Log.d("응답aaaaaa!!", response.body().toString())
                                    resultpath = Uri.parse(response.body().toString()).toString()
                                    resultpath = resultpath.replace(("\""), "")
                                    check_img_result.visibility = View.VISIBLE
                                    check_text_result.visibility = View.VISIBLE
                                    downloadButton.visibility = View.VISIBLE
                                    gif_loading.visibility = View.GONE
                                    gif_loading_text.visibility = View.GONE
                                    downloadButton.setOnClickListener {
                                        try {
                                            if (!URLUtil.isValidUrl(resultpath)) {
                                                Toast.makeText(
                                                    this@Translateloading,
                                                    " This is not a valid link", Toast.LENGTH_LONG)
                                                    .show()
                                            } else {
                                                val intent = Intent(Intent.ACTION_VIEW)
                                                intent.data = Uri.parse(resultpath)
                                                startActivity(intent)
                                            }
                                        } catch (e: ActivityNotFoundException) {
                                            Toast.makeText(
                                                this@Translateloading,
                                                " You don't have any browser to open web page",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<JsonPrimitive>, t: Throwable) {
                                    // 실패 시
                                    Log.d("응답", t.message.toString())
                                }
                            })
                    }

                }

                override fun onFailure(call: Call<TRANS_R_Model>, t: Throwable) {
                    // 실패 시
                    Log.d("응답", t.message.toString())
                }
            })





    }

}