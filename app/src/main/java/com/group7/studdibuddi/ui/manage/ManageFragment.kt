package com.group7.studdibuddi.ui.manage

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
import com.group7.studdibuddi.databinding.FragmentManageBinding
import com.group7.studdibuddi.ui.settings.ManageViewModel

class ManageFragment : Fragment() {

    private var _binding: FragmentManageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val manageViewModel =
            ViewModelProvider(this).get(ManageViewModel::class.java)

        _binding = FragmentManageBinding.inflate(inflater, container, false)
        val root: View = binding.root





        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}