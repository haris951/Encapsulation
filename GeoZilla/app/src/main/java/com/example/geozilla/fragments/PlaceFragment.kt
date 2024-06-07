package com.example.geozilla.fragments
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.geozilla.R
import com.example.geozilla.addcafe.AddCafe
import com.example.geozilla.addgym.AddGym
import com.example.geozilla.addhome.AddHome
import com.example.geozilla.addschool.AddSchool
import com.example.geozilla.addwork.AddWork
import com.google.android.material.bottomsheet.BottomSheetDialog
class PlaceFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_place, container, false)
        // initializing our button with its id.
        val btnShowBottomSheet = view.findViewById<Button>(R.id.addplace)
        btnShowBottomSheet.setOnClickListener {
            //creating a new bottom sheet dialog.
            val dialog = BottomSheetDialog(requireContext())
            //inflating a layout file which we have created.
            val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
            dialog.setCancelable(true)  //set cancelable
            dialog.setContentView(bottomSheetView) //setting content view to our view.
            dialog.show() //calling a show method to display a dialog.
        }
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Find the TextView by its ID
        val edittextTextView: TextView = view.findViewById(R.id.textView2_place)
        val addhomeTextView:TextView=view.findViewById(R.id.textView3)
        val addschoolTextView:TextView=view.findViewById(R.id.textView4)
        val addgymTextView:TextView=view.findViewById(R.id.textView5)
        val addcafeTextView:TextView=view.findViewById(R.id.textView6_addcafe)
        // Set a click listener for the TextView
        edittextTextView.setOnClickListener {
            openMain7(it) // Pass the clicked view as parameter

        }
addhomeTextView.setOnClickListener{
    addHome(it)
}
        addschoolTextView.setOnClickListener{
            addSchool(it)
        }
        addgymTextView.setOnClickListener{
            addGym(it)
        }
        addcafeTextView.setOnClickListener{
            addCafe(it)
        }

    }

    private fun openMain7(view: View) {
         val intent = Intent(requireContext(), AddWork::class.java)
       requireContext().startActivity(intent)
    }

    private fun addHome(view: View) {
        val intent=Intent(requireContext(),AddHome::class.java)
        requireContext().startActivity(intent)
    }

    private fun addSchool(view: View) {
        val intent=Intent (requireContext(),AddSchool::class.java)
        requireContext().startActivity(intent)
    }

    private fun addGym(view: View) {
        val intent=Intent (requireContext(),AddGym::class.java)
        requireContext().startActivity(intent)
    }

    fun addCafe(view: View) {
        val intent=Intent (requireContext(),AddCafe::class.java)
        requireContext().startActivity(intent)
    }


}

