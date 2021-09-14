package com.example.android.politicalpreparedness.election

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch

//TODO: Construct ViewModel and provide election datasource
class ElectionsViewModel(private val dataSource: ElectionDao): ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    private val _navigateToVoterInfo = MutableLiveData<Election?>()
    private val _upcomingElections = MutableLiveData<List<Election>>()
    private val _savedElections = dataSource.getAll()

    val loading: LiveData<Boolean>
        get() = _loading
    val navigateToVoterInfo: LiveData<Election?>
        get() = _navigateToVoterInfo
    val upcomingElections: LiveData<List<Election>>
        get() = _upcomingElections
    val savedElections: LiveData<List<Election>>
        get() = _savedElections

    init {
        loadElections()
    }

    private fun loadElections() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = CivicsApi.retrofitService.getElections()
                if(response.isSuccessful) {
                    _upcomingElections.value = response.body()?.elections
                } else {
                    Log.e("ViewModelElection", response.code().toString())
                }
            } catch (e: Exception) {
                Log.e("ViewModelElection", e.message.toString())
            } finally {
                _loading.value = false
            }
        }
    }

    fun navigateToVoterInfo(election: Election) {
        _navigateToVoterInfo.value = election
    }

    fun navigateToVoterInfoDone() {
        _navigateToVoterInfo.value = null
    }

    fun removeElection(election: Election) {
        _loading.value = true
        viewModelScope.launch {
            try {
                dataSource.delete(election)
            } catch (e: Exception) {
                Log.e("ViewModelElection", e.message.toString())
            } finally {
                _loading.value = false
            }
        }
    }

    //TODO: Create live data val for saved elections

    //TODO: Create val and functions to populate live data for upcoming elections from the API and saved elections from local database

    //TODO: Create functions to navigate to saved or upcoming election voter info

}