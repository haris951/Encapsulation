package com.example.geozilla.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.geozilla.Login
import com.example.geozilla.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import de.hdodenhof.circleimageview.CircleImageView

class MenuFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_menu, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Find the TextView by its ID
        val settingmenu: TextView = view.findViewById(R.id.settingmenu)
        // Set a click listener for the TextView
        settingmenu.setOnClickListener {
            setting(it) // Pass the clicked view as parameter
        }

        // Load the user's sign-in image,name and email
        val profileImageView: CircleImageView = view.findViewById(R.id.profile_imagemenu)
        val profileNameTextView: TextView = view.findViewById(R.id.profile_name)
        val profileEmailTextView:TextView = view.findViewById(R.id.profile_email)


        // Check if the user is signed in with Google
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
        googleSignInAccount?.let { account ->

            // Retrieve the user's profile image URL name email from GoogleSignInAccount
            val imageUrl = account.photoUrl.toString()
            val userName = account.displayName
            val userEmail=account.email

            // Load the profile image into the CircleImageView using Glide
            Glide.with(this)
                .load(imageUrl)
                .into(profileImageView)

            userName.let {
                profileNameTextView.text=it
            }

            userEmail.let {
                profileEmailTextView.text=it
            }
        }

    }
    private fun setting(view: View) {
        val intent = Intent(requireContext(), Login::class.java)
        requireContext().startActivity(intent)
    }
}