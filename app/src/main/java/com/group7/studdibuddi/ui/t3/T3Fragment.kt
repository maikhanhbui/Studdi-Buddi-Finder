package com.group7.studdibuddi.ui.t3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.group7.studdibuddi.LoginActivity
import com.group7.studdibuddi.databinding.FragmentT3Binding
import com.group7.studdibuddi.ui.t1.T3ViewModel

class T3Fragment : Fragment() {

    private var _binding: FragmentT3Binding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val t3ViewModel =
            ViewModelProvider(this).get(T3ViewModel::class.java)

        _binding = FragmentT3Binding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textT3
        t3ViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // To test the login page
        binding.loginActivityButton.setOnClickListener{
            if (FirebaseAuth.getInstance().currentUser == null) {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
            else{
                Toast.makeText(requireContext(),"Logged in", Toast.LENGTH_SHORT).show()
            }
        }

        binding.logoutActivityButton.setOnClickListener{
            if (FirebaseAuth.getInstance().currentUser != null) {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(requireContext(),"Logged out", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(),"Not logged in", Toast.LENGTH_SHORT).show()
            }
        }



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}