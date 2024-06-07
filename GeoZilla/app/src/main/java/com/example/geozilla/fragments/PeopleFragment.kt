package com.example.geozilla.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geozilla.R
import com.example.geozilla.adapter.UserAdapter
import com.example.geozilla.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import org.checkerframework.checker.units.qual.Length
import java.util.Locale

class PeopleFragment : Fragment(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private var userList: MutableList<User> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_people, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        userAdapter = UserAdapter(userList)
        recyclerView.adapter = userAdapter

        // Set up the map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.id_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up bottom sheet
        val bottomSheet = view.findViewById<View>(R.id.sheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 600
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        val shareButton = bottomSheet.findViewById<Button>(R.id.peoplefragmentbtn)
        shareButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid ?: ""
            shareDeepLink(userId) // Pass uid to shareDeepLink
        }

        // Load the current user's sign-in image
        val profileImageView: CircleImageView = view.findViewById(R.id.profile_image)
        val profileName: TextView = view.findViewById(R.id.profile_peoplefragment)
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
        googleSignInAccount?.let { account ->
            val imageUrl = account.photoUrl.toString()
            val userName = account.displayName
            Glide.with(this)
                .load(imageUrl)
                .into(profileImageView)
            userName?.let {
                profileName.text = it
            }
        }

        // Call handleDeepLink() to handle any deep links
//        handleDeepLink()
        return view
    }

    private fun shareDeepLink(userId: String) {
        val deepLinkUri = generateAndShareDeepLink(userId)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this link: $deepLinkUri")
        }
        startActivity(Intent.createChooser(shareIntent, "Share link using"))

        // Save user data to Firestore
        saveUserDataToFirestore()
    }

    private fun saveUserDataToFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        currentUser?.let { user ->
            val userData = hashMapOf(
                "userId" to user.uid,
                "name" to user.displayName,
                "email" to user.email,
                "photoUrl" to user.photoUrl?.toString()
            )

            val userDocumentId = "sharedUsers"  // Using a single document to store multiple users
            val userField = "users.${user.uid}"

            db.collection("sharedData").document(userDocumentId)
                .update(userField, userData)
                .addOnSuccessListener {
                    Toast.makeText(requireActivity(), "userdata saved", Toast.LENGTH_SHORT).show()
                    Log.d("haris", "User data successfully written to Firestore")
                }
        }
    }

    private fun generateAndShareDeepLink(userId: String): Uri {
        val baseUrl = "https://mygeozilla.page.link/6SuK"
        return Uri.parse(baseUrl).buildUpon()
            .appendQueryParameter("userId", userId)
            .build()
    }

    private fun handleDeepLink() {
        val data: Uri? = activity?.intent?.data
        data?.let {
            val userId = it.getQueryParameter("userId")
            userId?.let {
                fetchUserData(userId)
            }
        }
    }

    private fun fetchUserData(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userDocumentId = "sharedUsers"
        val userField = "users.$userId"

        db.collection("sharedData").document(userDocumentId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userData = document.get(userField) as? Map<String, String>
                    userData?.let {
                        val name = it["name"] ?: ""
                        val photoUrl = it["photoUrl"] ?: ""
                        val user = User(
                            name = name,
                            photoUrl = photoUrl
                        )
                        Toast.makeText(requireActivity(), "userdata fetched as $user ", Toast.LENGTH_SHORT).show()

                        Log.d("haris", "User data fetched: $user")
                        updateRecyclerView(user)
                    } ?: run {
                        Log.d("haris", "No such user data")
                    }
                } else {
                    Log.d("haris", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("haris", "Fetching user data failed", exception)
            }
    }

    private fun updateRecyclerView(user: User) {
        Log.d("haris", "Updating RecyclerView with user: $user")
        userList.add(user)
        userAdapter.notifyDataSetChanged()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val wah = LatLng(33.780775611641324, 72.723408419941)
        googleMap.addMarker(
            MarkerOptions()
                .position(wah)
                .title("Wah Cantt")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(wah, 15f))
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showLiveLocation(googleMap)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun showLiveLocation(googleMap: GoogleMap) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    googleMap.addMarker(MarkerOptions().position(userLocation).title("Your Location"))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(userLocation.latitude, userLocation.longitude, 1)
                    val tvUserAddress = view?.findViewById<TextView>(R.id.user_address)
                    tvUserAddress?.apply {
                        visibility = View.VISIBLE
                        text = addresses?.get(0)?.getAddressLine(0) ?: "Address not found"
                    }
                }
            }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
