package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment : Fragment() {

    private lateinit var binding: FragmentElectionBinding
    private lateinit var viewModel: ElectionsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentElectionBinding.inflate(inflater)

        val dataSource = ElectionDatabase.getInstance(requireContext()).electionDao
        viewModel = ViewModelProvider(
            this,
            ElectionsViewModelFactory(dataSource)
        ).get(ElectionsViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }
        })

        // TODO: Link elections to voter info
        setupNavigation()
        setupUpcomingElections()
        setupSavedElections()

        return binding.root
    }

    private fun setupNavigation() {
        viewModel.navigateToVoterInfo.observe(viewLifecycleOwner, Observer {
            it?.let {
                findNavController().navigate(
                    ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(
                        it
                    )
                )
                viewModel.navigateToVoterInfoDone()
            }
        })
    }

    private fun setupUpcomingElections() {
        val adapter = ElectionListAdapter(ElectionListener {
            viewModel.navigateToVoterInfo(it)
        })

        binding.recyclerViewUpcomingElections.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewUpcomingElections.adapter = adapter

        viewModel.upcomingElections.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun setupSavedElections() {
        val adapter = ElectionListAdapter(ElectionListener {
            viewModel.navigateToVoterInfo(it)
        })

        binding.recyclerViewSavedElections.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSavedElections.adapter = adapter

        viewModel.savedElections.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }
}