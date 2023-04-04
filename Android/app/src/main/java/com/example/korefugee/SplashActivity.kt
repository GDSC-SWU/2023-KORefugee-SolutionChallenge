package com.example.korefugee

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.example.korefugee.Login.LoginActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 3초 후 화면 이동
        val handler: Handler = Handler()
        handler.postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        },8000)

        val loading: ImageView = findViewById<View>(R.id.uploadprofileImageView) as ImageView
        val gifImage = GlideDrawableImageViewTarget(loading)
        Glide.with(this).load<Any>(com.example.korefugee.R.drawable.splash).into<GlideDrawableImageViewTarget>(gifImage)




    }



}