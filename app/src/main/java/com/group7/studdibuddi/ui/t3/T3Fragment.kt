package com.group7.studdibuddi.ui.t3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}