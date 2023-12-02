package com.group7.studdibuddi.ui.t2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.group7.studdibuddi.databinding.FragmentT2Binding
import com.group7.studdibuddi.ui.settings.T2ViewModel

class T2Fragment : Fragment() {

    private var _binding: FragmentT2Binding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val t2ViewModel =
            ViewModelProvider(this).get(T2ViewModel::class.java)

        _binding = FragmentT2Binding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textT2
        t2ViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}