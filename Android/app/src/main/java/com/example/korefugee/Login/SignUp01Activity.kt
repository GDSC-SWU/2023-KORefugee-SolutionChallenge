package com.example.korefugee.Login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.korefugee.*
import com.example.korefugee.databinding.ActivityLoginBinding
import com.example.korefugee.databinding.ActivitySignUp01Binding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUp01Activity : AppCompatActivity() {
    // 뷰 바인딩을 위한 객체 획득
    lateinit var binding: ActivitySignUp01Binding

    // api 이용하기 위한 객체
    val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 바인딩 기본 작업
        binding= ActivitySignUp01Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // sign up 버튼 클릭 시
        binding.signupButton.setOnClickListener{

            if(binding.emailEditText.text.toString() != "" && binding.passwordEditText.text.toString() != "" && binding.pluspasswordEditText.text.toString() != ""){
                // passwordplus 오류
                val data = sign_up_email(binding.emailEditText.text.toString())
                val intent = Intent(this,SignUp02Activity::class.java)

                api.post_register_email(data).enqueue(object :
                    Callback<Word_Deleted_R_Model> {
                    // 응답하면
                    override fun onResponse(call: Call<Word_Deleted_R_Model>, response: Response<Word_Deleted_R_Model>) {
                        Log.e("응답",response.toString())
                        Log.e("응답", response.body().toString())
                        Log.e("응답",response.errorBody()?.string().toString())
                        val responsedata = response.body()
                        if (responsedata != null) {
                            if(responsedata.error == "Already used Email."){
                                binding.errortextView.setText("Already used Email.")
                                binding.errortextView.visibility = View.VISIBLE
                            }
                            else if(responsedata.success != ""){
                                if( binding.pluspasswordEditText.text.toString() != binding.passwordEditText.text.toString()){
                                    // 중복 비밀번호 확인이 잘못되었을 때
                                    binding.errortextView.setText("Please enter the same password")
                                    binding.errortextView.visibility = View.VISIBLE
                                }
                                else if(false){

                                }
                                else {
                                    // sign up 화면 전환 + intent로 email과 password 정보 전달
                                    intent.putExtra("email", binding.emailEditText.text.toString())
                                    intent.putExtra("password", binding.passwordEditText.text.toString())
                                    startActivity(intent)
                                    finish()
                                }

                            }


                        }

                    }
                    override fun onFailure(call: Call<Word_Deleted_R_Model>, t: Throwable) {
                        // 실패 시
                        Log.e("응답",t.message.toString())
                    }
                })


                //

            }
        }



    }
}