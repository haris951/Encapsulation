package com.example.geozilla.addwork

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geozilla.R
import com.example.geozilla.adapter.UserAdapter
import com.example.geozilla.model.User
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale


class AddWork : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var textview2work: TextView
    private lateinit var mMap: GoogleMap
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_work)

        recyclerView = findViewById(R.id.recyclerview_addwork)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(emptyList())
        recyclerView.adapter = userAdapter

        retrieveLinkedUsersFromFirestore()
        retrieveAddressFromFirestore()

        // Initialize the map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.id_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        textview2work=findViewById(R.id.textview2work)

        retrieveAddressFromFirestore()
    }

    private fun fetchAddress(latitude: Double, longitude: Double, callback: (String) -> Unit) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            callback(addresses[0].getAddressLine(0))
        } else {
            callback("Address not found")
        }    }

    fun openMain8(view: View) {
        val intent = Intent(this, EditWork::class.java)
        startActivity(intent)
    }
    private fun retrieveAddressFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("address").document("work address").get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val address = document.getString("address")
                        val latitude = document.getDouble("latitude") ?: 33.77151001443163
                        val longitude = document.getDouble("longitude") ?: 72.75154003554175
                        textview2work.text = address

                        // Update map with the retrieved location
                        val location = LatLng(latitude, longitude)
                        updateMapLocation(location, address)

                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to retrieve address: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }
     override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
         //default location
        val defaultLocation = LatLng(33.77151001443163, 72.75154003554175)
        updateMapLocation(defaultLocation, "Default Location")
    }

    private fun updateMapLocation(location: LatLng, title: String?) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(location).title(title))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))

        // Add a circle around the location
        val circleOptions = CircleOptions()
            .center(location)
            .radius(50.0)
            .strokeWidth(12f)
            .strokeColor(0x5500FF00)

        mMap.addCircle(circleOptions)
    }
    private fun retrieveLinkedUsersFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)
                        user?.linkedUsers?.let { linkedUsers ->
                            fetchUsersFromIds(linkedUsers)
                        }
                    }
                }
        }
    }

    private fun fetchUsersFromIds(userIds: List<String>) {
        db.collection("users")
            .whereIn("uid", userIds)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val userList = mutableListOf<User>()
                for (document in querySnapshot.documents) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        userList.add(it)
                    }
                }
                userAdapter.setUserList(userList)
            }

    }
}