package com.example.weeklycode

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val mainEditText:EditText = findViewById(R.id.mainEditText)
        val mainButton:Button = findViewById(R.id.mainButton)
        val mainTextView:TextView = findViewById(R.id.mainTextView)
        mainButton.setOnClickListener{
            val inputString = mainEditText.text.toString()
            val uniqueCharCount = uniqueCharacters(inputString)
            mainTextView.text = uniqueCharCount.toString()
        }
    }
    private fun uniqueCharacters(input:String):Int{
        return input.toSet().size
    }
}