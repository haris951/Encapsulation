package com.example.geozilla.addwork

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.geozilla.geofencer.GeofenceBroadcastReceiver
import com.example.geozilla.R
import com.example.geozilla.dataclass.MockData
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditWork : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var searchView: SearchView
    private lateinit var addressTextView: TextView
    private lateinit var saveBtn: Button
    private lateinit var text: EditText
    private var selectedLatLng: LatLng? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var geofencingClient: GeofencingClient
    private val geofenceRadius = 650.0 // Radius in meters
    private val geofenceId = "WORK_GEOFENCE_ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_work)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.id_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize Places
        Places.initialize(applicationContext, getString(R.string.google_map_api_key))

        geofencingClient = LocationServices.getGeofencingClient(this)

        searchView = findViewById(R.id.searchView)
        addressTextView = findViewById(R.id.addressTextView)
        saveBtn = findViewById(R.id.saveBtn)
        text = findViewById(R.id.text)

        retrieveAddressFromFirestore()

        // Initialize the SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    // Find matching address
                    val result = MockData.addresses.find { address -> address.name.contains(query, ignoreCase = true) }
                    if (result != null) {
                        addressTextView.text = result.details
                        addressTextView.visibility = TextView.VISIBLE

                        mMap.clear()
                        mMap.addMarker(MarkerOptions().position(result.location).title(result.name))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(result.location, 15f))

                        val circleOptions = CircleOptions()
                            .center(result.location)
                            .radius(geofenceRadius)
                            .strokeWidth(12f)
                            .strokeColor(0x5500FF00)
                            .fillColor(0x2200FF00)
                        mMap.addCircle(circleOptions)

                        selectedLatLng = result.location

                        // Add geofence
                        addGeofence(result.location)
                    } else {
                        addressTextView.text = getString(R.string.address_not_found)
                        addressTextView.visibility = TextView.VISIBLE
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        saveBtn.setOnClickListener {
            saveAddressToFirestore()
        }

        // Request location permissions
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
           != PackageManager.PERMISSION_GRANTED) {
       ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
          REQUEST_LOCATION_PERMISSION_CODE)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val defaultLocation = LatLng(33.77151001443163, 72.75154003554175) // Example: San Francisco
        mMap.addMarker(MarkerOptions().position(defaultLocation).title("Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
    }

    private fun saveAddressToFirestore() {
        val addressText = addressTextView.text.toString().trim()
        val text = text.text.toString().trim()
        if (addressText.isEmpty() || selectedLatLng == null) {
            Toast.makeText(this, "No address selected", Toast.LENGTH_SHORT).show()
            return
        }
        val addressMap = hashMapOf(
            "address" to addressText,
            "place" to text,
            "latitude" to selectedLatLng?.latitude,
            "longitude" to selectedLatLng?.longitude
        )

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("address").document("work address").set(addressMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Changes Accepted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Changes Not Accepted", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun retrieveAddressFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("address").document("work address").get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val address = document.getString("address")
                        val place = document.getString("place")
                        val latitude = document.getDouble("latitude")
                        val longitude = document.getDouble("longitude")

                        addressTextView.text = address
                        text.setText(place)
                        if (latitude != null && longitude != null) {
                            val location = LatLng(latitude, longitude)

                            mMap.clear()
                            mMap.addMarker(MarkerOptions().position(location).title(place ?: ""))

                            val circleOptions = CircleOptions()
                                .center(location)
                                .radius(geofenceRadius)
                                .strokeWidth(12f)
                                .strokeColor(0x5500FF00)
                                .fillColor(0x2200FF00)
                            mMap.addCircle(circleOptions)

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

                            // Add geofence
                            addGeofence(location)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to retrieve address: $exception",
                        Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun addGeofence(location: LatLng) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }
        val geofence = Geofence.Builder()
            .setRequestId(geofenceId)
            .setCircularRegion(location.latitude, location.longitude, geofenceRadius.toFloat())
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        geofencingClient.addGeofences(geofencingRequest, pendingIntent).run {
            addOnSuccessListener {
                Log.i("EditWork", "Geofence added successfully")
            }
            addOnFailureListener {
                Log.e("EditWork", "Failed to add geofence: ${it.message}")
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION_CODE = 1
    }
}
