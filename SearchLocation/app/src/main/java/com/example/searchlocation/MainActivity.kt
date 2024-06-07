package com.example.searchlocation

import android.os.Bundle
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// Address data class to hold address information
data class Address(val name: String, val details: String, val location: LatLng)

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var searchView: SearchView
    private lateinit var addressTextView: TextView

    // Sample data (mock addresses)
    private val mockAddresses = listOf(
        Address("123 Main St", "123 Main St, Springfield", LatLng(37.7749, -122.4194)),
        Address("456 Elm St", "456 Elm St, Springfield", LatLng(34.0522, -118.2437)),
        Address("789 Maple St", "789 Maple St, Springfield", LatLng(40.7128, -74.0060)),
        Address("101 Oak St", "101 Oak St, Springfield", LatLng(41.8781, -87.6298))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize SearchView and TextView variables
        searchView = findViewById(R.id.searchView)
        addressTextView = findViewById(R.id.addressTextView)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize the SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    // Find matching address
                    val result = mockAddresses.find { address -> address.name.contains(query, ignoreCase = true) }
                    if (result != null) {
                        // Display the result in the TextView
                        addressTextView.text = result.details
                        addressTextView.visibility = TextView.VISIBLE

                        // Move the map camera to the address location
                        mMap.clear()
                        mMap.addMarker(MarkerOptions().position(result.location).title(result.name))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(result.location, 15f))
                    } else {
                        addressTextView.text = "Address not found"
                        addressTextView.visibility = TextView.VISIBLE
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Default location and marker
        val defaultLocation = LatLng(37.7749, -122.4194) // Example: San Francisco
        mMap.addMarker(MarkerOptions().position(defaultLocation).title("Marker in San Francisco"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
    }
}
