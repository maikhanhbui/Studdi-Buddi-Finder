package com.group7.studdibuddi.ui.t1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.group7.studdibuddi.databinding.FragmentT1Binding

class T1Fragment : Fragment() {

    private var _binding: FragmentT1Binding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val t1ViewModel =
            ViewModelProvider(this).get(T1ViewModel::class.java)

        _binding = FragmentT1Binding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textT1
        t1ViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}