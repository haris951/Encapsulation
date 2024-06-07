package com.gtappdevelopers.kotlingfgproject

import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.deeplinking.R

class MainActivity : AppCompatActivity() {

    // on below line we are creating
    // variables for our text view
    lateinit var msgTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // on below line we are initializing
        // our variable with their ids.
        msgTV = findViewById(R.id.idTVMsg)

        // getting the data from our
        // intent in our uri.
        val uri: Uri? = intent.data

        // checking if the uri is null or not.
        if (uri != null) {
            // if the uri is not null then we are getting the
            // path segments and storing it in list.
            val parameters: List<String> = uri.getPathSegments()

            // after that we are extracting string from that parameters.
            val param = parameters[parameters.size - 1]

            // on below line we are setting
            // that string to our text view
            // which we got as params.
            msgTV.setText(param)
        }
    }
}
