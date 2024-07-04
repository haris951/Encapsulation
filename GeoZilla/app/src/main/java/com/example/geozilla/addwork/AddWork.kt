package com.example.geozilla.addwork

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geozilla.NotificationActivity
import com.example.geozilla.R
import com.example.geozilla.adapter.UserNotification
import com.example.geozilla.geofencer.GeofenceBroadcastReceiver
import com.example.geozilla.model.User
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddWork : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var textview2work: TextView
    private lateinit var mMap: GoogleMap
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserNotification
    private val db = FirebaseFirestore.getInstance()
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofencePendingIntent: PendingIntent
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var currentLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_work)

        recyclerView = findViewById(R.id.recyclerview_addwork)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserNotification(emptyList()) { user ->
            val intent = Intent(this, NotificationActivity::class.java)
            intent.putExtra("USER_NAME", user.name)
            startActivity(intent)
        }
        recyclerView.adapter = userAdapter

        retrieveLinkedUsersFromFirestore()

        // Initialize the map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.id_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        textview2work = findViewById(R.id.textview2work)

        retrieveAddressFromFirestore()

        // Initialize GeofencingClient
        geofencingClient = LocationServices.getGeofencingClient(this)
        geofencePendingIntent = PendingIntent.getBroadcast(
            this, 0,
            Intent(this, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            currentLocation?.let {
                addGeofence(it)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                currentLocation?.let {
                    addGeofence(it)
                }
            } else {
                Toast.makeText(this, "Location permissions are required for geofencing", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                        currentLocation = location
                        updateMapLocation(location, address)

                        // Set up geofencing
                        checkAndRequestPermissions()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to retrieve address: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Default location
        val defaultLocation = LatLng(33.77151001443163, 72.75154003554175)
        updateMapLocation(defaultLocation, "Default Location")

        mMap.setOnMapClickListener { latLng ->
            currentLocation = latLng
            updateMapLocation(latLng, "Selected Location")
            checkAndRequestPermissions()
        }
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

    @SuppressLint("MissingPermission")
    private fun addGeofence(location: LatLng) {
        val geofence = Geofence.Builder()
            .setRequestId("workGeofence")
            .setCircularRegion(location.latitude, location.longitude, 50f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(60000) // 1 minute user remains inside the circle
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            .addOnSuccessListener {
                Toast.makeText(this, "Geofence added", Toast.LENGTH_SHORT).show()
            }
    }
}
