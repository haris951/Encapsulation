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
        val view = inflater.inflate(R.layout.fragment_place, container, false)
        val btnShowBottomSheet = view.findViewById<Button>(R.id.addplace)
        btnShowBottomSheet.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            //inflating a layout file which we have created.
            val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
            dialog.setCancelable(true)
            dialog.setContentView(bottomSheetView)
            dialog.show()
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

   private fun addCafe(view: View) {
        val intent=Intent (requireContext(),AddCafe::class.java)
        requireContext().startActivity(intent)
    }


}

