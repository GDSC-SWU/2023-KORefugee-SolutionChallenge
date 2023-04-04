package com.example.korefugee.Voca

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.example.korefugee.*
import kotlinx.android.synthetic.main.voca_page.view.*
import kotlinx.android.synthetic.main.voca_page.view.Korean_read
import kotlinx.android.synthetic.main.voca_page.view.Koreantext
import kotlinx.android.synthetic.main.voca_page2.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewPagerAdapter3(accesstoken:String, date:Int, level:String,
                        category:String) : PagerAdapter(){
    // api 이용하기 위한 객체
    val api = APIS.create()

    private var mContext: Context?=null

    var t_accesstoken:String = accesstoken
    var t_date:Int = date
    var t_level:String = level
    var t_category:String = category
    var id:Int = 1

    fun ViewPagerAdapter(context: Context){
        mContext=context;
    }

    //position에 해당하는 페이지 생성
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // page.xml 과 연결

        var view= LayoutInflater.from(container.context).inflate(R.layout.voca_page2,container,false)


        api.get_word("Bearer $t_accesstoken", "${t_date}" ,"$t_level", "$t_category")
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
                            view.Koreantext.setText(vocalist[position].words)
                            view.Korean_read.setText(vocalist[position].wordP)

                            view.star2.setOnClickListener {
                                id = vocalist[position].wordId
                                view.star2?.isSelected = (view.star2?.isSelected != true)
                                val data = MyWord_Save_Model(id)
                                if(view.star2?.isSelected != true){
                                    // 추가
                                    if(vocalist[position].check == true){
                                        api.post_myword("Bearer $t_accesstoken",data).enqueue(object : Callback<Word_Saved_check_R_Model> {
                                            // 응답하면
                                            override fun onResponse(call: Call<Word_Saved_check_R_Model>, response: Response<Word_Saved_check_R_Model>) {

                                                vocalist[position].check = true
                                                Log.e("응답",vocalist[position].check.toString())
                                            }
                                            override fun onFailure(call: Call<Word_Saved_check_R_Model>, t: Throwable) {
                                                // 실패 시
                                                Log.d("응답",t.message.toString())
                                            }
                                        })
                                    }
                                }
                                else{
                                    // 삭제
                                    if(vocalist[position].check == true){
                                        com.example.korefugee.Voca.api.delete_myword("Bearer $t_accesstoken",data.wordId).enqueue(object :
                                            Callback<Word_Deleted_R_Model> {
                                            // 응답하면
                                            override fun onResponse(call: Call<Word_Deleted_R_Model>, response: Response<Word_Deleted_R_Model>) {
                                                Log.e("응답",response.toString())
                                                Log.e("응답", response.body().toString())
                                                Log.e("응답",response.errorBody()?.string().toString())
                                                vocalist[position].check = false
                                                Log.e("응답",vocalist[position].check.toString())

                                            }
                                            override fun onFailure(call: Call<Word_Deleted_R_Model>, t: Throwable) {
                                                // 실패 시
                                                Log.e("응답",t.message.toString())
                                            }
                                        })
                                    }
                                }

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
        container.addView(view)
        return view
    }

    //position에 위치한 페이지 제거
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    //사용가능한 뷰 개수 리턴
    override fun getCount(): Int {
        // 날짜 별 개수로 화면 구성

        return 10
    }

    //페이지뷰가 특정 키 객체(key object)와 연관 되는지 여부
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (view==`object`)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
    }

    override fun getItemPosition(`object`: Any): Int {
        return super.getItemPosition(`object`)
    }

}