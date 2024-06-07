package com.example.geozilla.addschool

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.geozilla.R
import com.example.geozilla.dataclass.MockData
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

class EditSchool : AppCompatActivity(),OnMapReadyCallback {


    private lateinit var mMap: GoogleMap
    private lateinit var searchviewSchool: SearchView
    private lateinit var addressTextViewschool: TextView
    private lateinit var saveBtn1:Button
    private lateinit var texteditschool:EditText
    private var selectedLatLng: LatLng? = null

    private val db=FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_school)

        // Initialize the map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.id_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize Places
        Places.initialize(applicationContext, getString(R.string.google_map_api_key))

        // Initialize SearchView and TextView variables
        searchviewSchool = findViewById(R.id.searchViewschool)
        addressTextViewschool = findViewById(R.id.addressTextViewschool)
        saveBtn1 = findViewById(R.id.saveBtn1)
        texteditschool=findViewById(R.id.texteditschool)


        retrieveSchoolAddressFromFirestore()

        // Initialize the SearchView
        searchviewSchool.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    // Find matching address
                    val result = MockData.addresses.find { address -> address.name.contains(query, ignoreCase = true) }
                    if (result != null) {
                        // Display the result in the TextView
                        addressTextViewschool.text = result.details
                        addressTextViewschool.visibility = TextView.VISIBLE

                        // Move the map camera to the address location
                        mMap.clear()
                        mMap.addMarker(MarkerOptions().position(result.location).title(result.name))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(result.location, 15f))

                        // Add a circle around the location
                        val circleOptions = CircleOptions()
                            .center(result.location)
                            .radius(650.0)
                            .strokeWidth(12f)
                            .strokeColor(0x5500FF00)
                            .fillColor(0x2200FF00)
                        mMap.addCircle(circleOptions)
                        selectedLatLng = result.location

                    } else {
                        addressTextViewschool.text = getString(R.string.address_not_found)
                        addressTextViewschool.visibility = TextView.VISIBLE
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        saveBtn1.setOnClickListener {
            saveSchoolAddressToFirestore()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Default location and marker
        val defaultLocation = LatLng(33.77151001443163, 72.75154003554175) // Example: San Francisco
        mMap.addMarker(MarkerOptions().position(defaultLocation).title("Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
    }

    private fun saveSchoolAddressToFirestore() {
        val addressText = addressTextViewschool.text.toString().trim()
        val text=texteditschool.text.toString().trim()
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
            db.collection("address").document("school address").set(addressMap)
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
    private fun retrieveSchoolAddressFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("address").document("school address").get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val address = document.getString("address")
                        val place = document.getString("place")
                        val latitude = document.getDouble("latitude")
                        val longitude = document.getDouble("longitude")

                        addressTextViewschool.text = address
                        texteditschool.setText(place)
                        if (latitude != null && longitude != null) {
                            val location = LatLng(latitude, longitude)

                            mMap.clear()
                            mMap.addMarker(MarkerOptions().position(location).title(place ?: ""))

                            val circleOptions = CircleOptions()
                                .center(location)
                                .radius(650.0)
                                .strokeWidth(12f)
                                .strokeColor(0x5500FF00)
                                .fillColor(0x2200FF00)
                            mMap.addCircle(circleOptions)

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                        }

                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to retrieve address: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }

    }
