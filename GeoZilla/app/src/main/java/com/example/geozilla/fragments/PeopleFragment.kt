package com.example.geozilla.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Locale

class PeopleFragment : Fragment(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private var userList: MutableList<User> = mutableListOf()
    private var isDeepLinkHandled = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_people, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        userAdapter = UserAdapter(userList)
        recyclerView.adapter = userAdapter

        // Load data from SharedPreferences
        val savedUserList = loadUserDataFromSharedPreferences()
        if (savedUserList.isNotEmpty()) {
            userList.addAll(savedUserList)
            updateRecyclerView(userList)
        }

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
            val currentUserId = user?.uid ?: ""
            val sharedLink = "https://mygeozilla.page.link/6SuK?userId=$currentUserId"

            saveUserDataToFirestore(currentUserId, sharedLink)
        }

        // Load the current user's sign-in image and name
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

        return view
    }

    override fun onResume() {
        super.onResume()
        handleDeepLink(requireActivity().intent)
    }
    private fun handleDeepLink(intent: Intent) {
        val data: Uri? = intent.data
        Log.d("PeopleFragment", "handleDeepLink called with intent data: $data")
        var userId: String? = null
        if (data != null) {
            userId = data.getQueryParameter("userId")
        }
        if (!userId.isNullOrEmpty()) {
            fetchUserWithId(userId)
            isDeepLinkHandled = true
        }
    }

    private fun saveUserDataToFirestore(userId: String, sharedLink: String) {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val userName = user?.displayName ?: ""
        val userEmail = user?.email ?: ""
        val userPhotoUrl = user?.photoUrl.toString()
        val userMap = hashMapOf(
            "uid" to userId,
            "name" to userName,
            "email" to userEmail,
            "photoUrl" to userPhotoUrl,
            "sharedLink" to sharedLink,
        )
        db.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Log.d("PeopleFragment", "User data successfully saved")
                shareDeepLink(userId)
            }
    }

    private fun fetchUserWithId(userId: String) {
        if (userExists(userId)) {
            Log.d("PeopleFragment", "User already exists in the list")
            logUserList()
            return
        }
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(userId)
        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        userList.add(user)
                        updateRecyclerView(userList)
                        saveUserDataToSharedPreferences(userList)
                        logUserList()  // Log the userList to check its contents after adding
                    }
                }
            }
    }
    private fun updateRecyclerView(userList: List<User>) {
        userAdapter.setUserList(userList)
    }
    private fun userExists(userId: String): Boolean {
        return userList.any { it.uid == userId }
    }

    private fun logUserList() {
        Log.d("PeopleFragment", "Current User List:")
        for (user in userList) {
            Log.d("PeopleFragment", "User ID: ${user.uid}, Name: ${user.name}, Email: ${user.email}")
        }
    }

    private fun generateAndShareDeepLink(userId: String): Uri {
        val baseUrl = "https://mygeozilla.page.link/6SuK"
        return Uri.parse(baseUrl).buildUpon()
            .appendQueryParameter("userId", userId)
            .build()
    }

    private fun shareDeepLink(userId: String) {
        val deepLinkUri = generateAndShareDeepLink(userId)
        Log.d("PeopleFragment", "Generated Deep Link: $deepLinkUri")
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this link: $deepLinkUri")
        }
        startActivity(Intent.createChooser(shareIntent, "Share link using"))
    }
    
    private fun saveUserDataToSharedPreferences(userList: List<User>) {
        val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(userList)
        editor.putString("user_list", json)
        editor.apply()
    }
    private fun loadUserDataFromSharedPreferences(): List<User> {
        val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("user_list", null)
        val type = object : TypeToken<List<User>>() {}.type
        return if (json != null) {
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
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
                    val userAddress = view?.findViewById<TextView>(R.id.userAddress)
                    tvUserAddress?.apply {
                        visibility = View.VISIBLE
                        text = addresses?.get(0)?.getAddressLine(0)
                    }
                    userAddress?.apply {
                        visibility=View.VISIBLE
                        text=addresses?.get(0)?.getAddressLine(0)
                    }
                }
            }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
