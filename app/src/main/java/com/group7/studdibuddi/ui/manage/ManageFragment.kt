package com.group7.studdibuddi.ui.manage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.group7.studdibuddi.MySessionActivity
import com.group7.studdibuddi.session.SessionUtil
import com.group7.studdibuddi.databinding.FragmentManageBinding
import com.group7.studdibuddi.session.Session
import com.group7.studdibuddi.session.SessionListAdapter
import com.group7.studdibuddi.session.SessionViewModel
import com.group7.studdibuddi.session.SessionViewModelFactory
import com.group7.studdibuddi.ui.settings.ManageViewModel


private lateinit var sessionViewModel: SessionViewModel
private lateinit var sessionListAdapter: SessionListAdapter
private lateinit var viewModelFactory: SessionViewModelFactory

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

        viewModelFactory = SessionViewModelFactory()
        sessionViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(
            SessionViewModel::class.java)


        sessionListAdapter = SessionListAdapter(requireActivity(), emptyList())

        binding.manageSessionList.adapter = sessionListAdapter

        sessionViewModel.joinedSessionLiveData.observe(viewLifecycleOwner) { sessions ->
            // Update when observe changes
            sessionListAdapter.replace(sessions)
            sessionListAdapter.notifyDataSetChanged()
        }

        binding.ownerCheckBox.setOnCheckedChangeListener { _, isChecked ->
            SessionUtil.showOwnedOnly = isChecked
            sessionViewModel.updateJoined()
        }

        binding.manageSessionList.setOnItemClickListener { _, _, position, _ ->
            SessionUtil.selectedSession = sessionListAdapter.getItem(position)

            val intent = Intent(requireContext(), MySessionActivity::class.java)
            startActivity(intent)
        }



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}