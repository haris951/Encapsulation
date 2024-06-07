package com.example.geozilla.addcafe

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.geozilla.R
import com.example.geozilla.addgym.EditGym
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddCafe : AppCompatActivity(),OnMapReadyCallback {
    private lateinit var textview2cafe: TextView
    private lateinit var mMap:GoogleMap
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_cafe)
        textview2cafe=findViewById(R.id.textview2cafe)

        val mapFragment=supportFragmentManager.findFragmentById(R.id.id_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        retrieveCafeAddressFromFirestore()
    }
    fun editCafe(view: View) {
        val intent= Intent(this, EditCafe::class.java)
        startActivity(intent)
    }
    private fun retrieveCafeAddressFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("address").document("cafe address").get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val address = document.getString("address")
                        val latitude = document.getDouble("latitude") ?: 33.77151001443163
                        val longitude = document.getDouble("longitude") ?: 72.75154003554175
                        textview2cafe.text = address

                        // Update map with the retrieved location
                        val location = LatLng(latitude,longitude)
                        updateMapLocation(location,address)

                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to retrieve address: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val defaultLocation = LatLng(33.77151001443163, 72.75154003554175) // Example: default location
        updateMapLocation(defaultLocation, "Default Location")
    }
    private fun updateMapLocation(location: LatLng, title: String?) {

        mMap.clear()
        mMap.addMarker(MarkerOptions().position(location).title(title))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))

        val circleOptions = CircleOptions()
            .center(location)
            .radius(50.0)
            .strokeWidth(12f)
            .strokeColor(0x5500FF00)
        mMap.addCircle(circleOptions)
    }
}