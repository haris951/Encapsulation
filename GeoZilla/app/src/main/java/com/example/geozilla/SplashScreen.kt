package com.example.geozilla

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.geozilla.activities.Geozillawelcome
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        auth = FirebaseAuth.getInstance()
        val currentUser=auth.currentUser
        if(currentUser!=null){
            val intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
        }else{
            val intent=Intent(this,Geozillawelcome::class.java)
            startActivity(intent)
        }
        finish()
    }
}