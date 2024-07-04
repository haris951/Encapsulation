package com.example.geozilla.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Looper
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
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
    private lateinit var googleMap: GoogleMap
    private lateinit var locationCallback: LocationCallback

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

            saveUserDataToFirestore(currentUserId) {
                shareDeepLink(currentUserId, sharedLink)
            }
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
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { userId ->
            fetchLinkedUsers(userId)
        }
    }

    private fun shareDeepLink(userId: String, sharedLink: String) {
        val deepLinkUri = generateDeepLink(userId)
        Log.d("PeopleFragment", "Generated Deep Link: $deepLinkUri")
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this link: $deepLinkUri")
        }
        startActivity(Intent.createChooser(shareIntent, "Share link using"))
    }

    private fun generateDeepLink(userId: String): Uri {
        val baseUrl = "https://mygeozilla.page.link/6SuK"
        return Uri.parse(baseUrl).buildUpon()
            .appendQueryParameter("userId", userId)
            .build()
    }

    private fun handleDeepLink(intent: Intent) {
        val data: Uri? = intent.data
        Log.d("PeopleFragment", "handleDeepLink called with intent data: $data")
        var userId: String? = null
        if (data != null) {
            userId = data.getQueryParameter("userId")
        }
        if (!userId.isNullOrEmpty()) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid ?: ""
            if (userId != currentUserId) {
                saveUserDataToFirestore(currentUserId) {
                    addUserToLinkedUsers(currentUserId, userId) {
                        addUserToLinkedUsers(userId, currentUserId) {
                            fetchUserWithId(userId, currentUserId)
                        }
                    }
                }
            }
            isDeepLinkHandled = true
        }
    }

    private fun saveUserDataToFirestore(userId: String, callback: () -> Unit) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val db = FirebaseFirestore.getInstance()
                val user = FirebaseAuth.getInstance().currentUser
                val userName = user?.displayName ?: ""
                val userEmail = user?.email ?: ""
                val userPhotoUrl = user?.photoUrl.toString()
                val latitude = location?.latitude ?: 0.0
                val longitude = location?.longitude ?: 0.0

                val userMap = hashMapOf(
                    "uid" to userId,
                    "name" to userName,
                    "email" to userEmail,
                    "photoUrl" to userPhotoUrl,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "linkedUsers" to emptyList<String>() // Initialize linkedUsers as an empty list
                )

                db.collection("users").document(userId)
                    .set(userMap)
                    .addOnSuccessListener {
                        Log.d("PeopleFragment", "User data successfully saved")
                        callback()
                    }
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    private fun addUserToLinkedUsers(userId: String, linkedUserId: String, onSuccess: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(userId)
        userDocRef.update("linkedUsers", FieldValue.arrayUnion(linkedUserId))
            .addOnSuccessListener {
                Log.d("PeopleFragment", "Successfully added linked user $linkedUserId to $userId's linkedUsers list")
                // Fetch the updated user data to refresh the UI
                fetchUserWithId(linkedUserId, userId) {
                    onSuccess()
                }
            }
    }

    private fun fetchUserWithId(userId: String, currentUserId: String, callback: () -> Unit = {}) {
        if (userId == currentUserId || userExists(userId)) {
            Log.d("PeopleFragment", "User already exists in the list or is the current user")
            logUserList()
            callback()  // Execute the callback even if user already exists
            return
        }
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(userId)
        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    user?.let {
                        userList.add(it)
                        saveUserDataToSharedPreferences(userList)
                        updateRecyclerView(userList)
                        logUserList()
                    }
                }
                callback()
            }
    }

    private fun userExists(userId: String): Boolean {
        return userList.any { it.uid == userId }
    }

    private fun logUserList() {
        Log.d("PeopleFragment", "Current User List: ${userList.map { it.uid }}")
    }

    private fun updateRecyclerView(userList: List<User>) {
        userAdapter.setUserList(userList)
    }

    private fun fetchLinkedUsers(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(userId)
        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    user?.linkedUsers?.let { linkedUsers ->
                        fetchUsers(linkedUsers, userId)
                    }
                }
            }
    }
    @SuppressLint("MissingPermission")
    private fun fetchUsers(userIds: List<String>, currentUserId: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
           userList.clear()
            for (userId in userIds) {
              if (userId != currentUserId) { // Skip the current user's own data
               usersCollection.document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                val newUserLocation = LatLng(it.latitude, it.longitude)
                 googleMap.clear()
                 googleMap.addMarker(MarkerOptions().position(newUserLocation).title(user.name))
                 googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newUserLocation, 15f))
                 getAddressFromLocation(it.latitude, it.longitude) { address ->
                 it.address = address
                 userList.add(it)
                 updateRecyclerView(userList)
               }
              }
             }
             }
            }
           }
    }
    private fun getAddressFromLocation(latitude: Double, longitude: Double, callback: (String) -> Unit) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        if (Geocoder.isPresent()) {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0].getAddressLine(0)
                callback(address)
            } else {
                callback("Address not found")
            }
        } else {
            callback("Geocoding not available")
        }
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

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val wah = LatLng(33.780775611641324, 72.723408419941)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(wah, 15f))
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showLiveLocation()

        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    @SuppressLint("MissingPermission")
    private fun showLiveLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(100)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val userLocation = LatLng(location.latitude, location.longitude)
                    googleMap.clear()
                    googleMap.addMarker(MarkerOptions().position(userLocation).title("Your Location"))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(userLocation.latitude, userLocation.longitude, 1)
                    val tvUserAddress = view?.findViewById<TextView>(R.id.user_address)
                    tvUserAddress?.apply {
                        visibility = View.VISIBLE
                        text = addresses?.get(0)?.getAddressLine(0)
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)

    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
