package com.example.korefugee.Login

import android.Manifest
import android.R
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.korefugee.*
import com.example.korefugee.databinding.ActivityLoginBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
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


// email, password 검증 및 오류 메세지 보여주기
// 로그인 유지 시 그냥 들어올 수 있도록 설정
// 오류 메세지 한 번 클릭으로 없어지도록 or 오류 메세지 sign in 눌렀을 때 알아서 3초 후 없어지도록
// 눌렸을 때 디자인 손보기..?
// 키보드 올라오는 것 막기

class LoginActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS = 1
    // 뷰 바인딩을 위한 객체 획득
    lateinit var binding: ActivityLoginBinding

    // api 이용하기 위한 객체
    val api = APIS.create()
    lateinit var accesstoken:String
    lateinit var refreshtoken:String

    lateinit var birth: String
    lateinit var email: String
    lateinit var gender: String
    lateinit var language: String
    lateinit var name: String
    lateinit var nation: String
    lateinit var password: String
    lateinit var status: String

    val MY_PERMISSION_ACCESS_ALL = 100
    @OptIn(ExperimentalTime::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 바인딩 기본 작업
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            var permissions = arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSION_ACCESS_ALL)
        }
        checkPermission()

        // sign in 버튼 클릭 시
        binding.signinButton.setOnClickListener{

            // data 생성 및 정의
            // type -> birth
            val data = LoginModel(binding.emailEditText.text.toString(),
                binding.passwordEditText.text.toString())
            val intent = Intent(this,MainActivity::class.java)


            // 데이터 구조에 맞춰서 회원가입 데이터 받아와서 넣기
            api.post_users(data).enqueue(object : Callback<Login_R_Model> {
                // 응답하면
                override fun onResponse(call: Call<Login_R_Model>, response: Response<Login_R_Model>) {
                        binding.errortextView.visibility =  GONE
                        Log.d("응답",response.toString())
                        Log.d("응답", response.body().toString())
                        Log.d("응답",response.errorBody()?.string().toString())

                    val responseerror = response.body()
                    Log.d("응답a",responseerror.toString())
                    if (responseerror != null ) {
                        if(responseerror.code.toString() == 0.toString()){
                            accesstoken = responseerror.token.accesstoken
                            refreshtoken = responseerror.token.refreshtoken
                            Log.d("응답a",accesstoken)
                            Log.d("응답a",refreshtoken)

                            api.get_my("Bearer $accesstoken").enqueue(object : Callback<MY_R_Model> {
                                // 응답하면
                                override fun onResponse(call: Call<MY_R_Model>, response: Response<MY_R_Model>) {

                                    Log.d("응답",response.toString())
                                    Log.d("응답", response.body().toString())
                                    Log.d("응답",response.errorBody()?.string().toString())

                                    val my = response.body()
                                    Log.d("응답a",my.toString())

                                    if (my != null ) {
                                        if(my.success == "success"){
                                            birth = my.data.birth
                                            email = my.data.email
                                            gender = my.data.gender
                                            language = my.data.language
                                            name = my.data.name
                                            nation = my.data.nation
                                            password = my.data.password
                                            status = my.data.status

                                            intent.putExtra("accesstoken",accesstoken)
                                            intent.putExtra("refreshtoken",refreshtoken)
                                            intent.putExtra("name",name)
                                            intent.putExtra("language",language)
                                            intent.putExtra("email",email)
                                            intent.putExtra("birth",birth)
                                            intent.putExtra("gender",gender)
                                            intent.putExtra("nation",nation)
                                            intent.putExtra("status",status)

                                            startActivity(intent)
                                            finish()

                                        }
                                    }

                                }
                                override fun onFailure(call: Call<MY_R_Model>, t: Throwable) {
                                    // 실패 시
                                    Log.d("응답",t.message.toString())
                                }
                            })

                        }
                        else if(responseerror.code.toString() == 204.toString()){
                            binding.errortextView.visibility =  VISIBLE
                            binding.errortextView.setText(responseerror.success)
                        }
                    }

                }

                override fun onFailure(call: Call<Login_R_Model>, t: Throwable) {
                    // 실패 시
                    Log.d("응답",t.message.toString())
                }
            })
        }

        // sign up 버튼 클릭 시
        binding.signupbutton.setOnClickListener{
            // 회원 가입 화면 전환
            val intent = Intent(this,SignUp01Activity::class.java)
            startActivity(intent)
        }

    }


    private fun checkPermission() {
        var permission = mutableMapOf<String, String>()
        permission["storageRead"] = Manifest.permission.READ_EXTERNAL_STORAGE
        permission["storageWrite"] =  Manifest.permission.WRITE_EXTERNAL_STORAGE

        // 현재 권한 상태 검사
        var denied = permission.count { ContextCompat.checkSelfPermission(this, it.value)  == PackageManager.PERMISSION_DENIED }

        // 마시멜로 버전 이후
        if(denied > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permission.values.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_PERMISSIONS) {

            // 1. 권한 확인이 다 끝난 후 동의하지 않은것이 있다면 종료
            var count = grantResults.count { it == PackageManager.PERMISSION_DENIED } // 동의 안한 권한의 개수
            if(count != 0) {
                Toast.makeText(applicationContext, "서비스의 필요한 권한입니다.\n권한에 동의해주세요.", Toast.LENGTH_SHORT).show()
                finish()
            }

            /* 2. 권한 요청을 거부했다면 안내 메시지 보여주며 앱 종료 */
            grantResults.forEach {
                if(it == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(applicationContext, "서비스의 필요한 권한입니다.\n권한에 동의해주세요.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
