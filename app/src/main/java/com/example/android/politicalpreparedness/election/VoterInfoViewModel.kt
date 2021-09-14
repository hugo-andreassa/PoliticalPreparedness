package com.example.android.politicalpreparedness.election

import android.util.Log
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.launch

class VoterInfoViewModel(private val election: Election?, private val dataSource: ElectionDao) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    private val _buttonText = MutableLiveData<String>()
    private val _selectedElection = MutableLiveData<Election?>()
    private val _voterInfoForSelectedElection = MutableLiveData<VoterInfoResponse?>()

    val loading: LiveData<Boolean>
        get() = _loading

    val buttonText: LiveData<String>
        get() = _buttonText

    val selectedElection: LiveData<Election?>
        get() = _selectedElection

    val votingLocation: LiveData<String> = Transformations.map(_voterInfoForSelectedElection) {
        it?.state?.get(0)?.electionAdministrationBody?.votingLocationFinderUrl ?: "No voting location finder url provided"
    }
    val ballotInfoUrl: LiveData<String> = Transformations.map(_voterInfoForSelectedElection) {
        it?.state?.get(0)?.electionAdministrationBody?.ballotInfoUrl ?: "No ballot info provided"
    }

    val mailingAddress: LiveData<String> = Transformations.map(_voterInfoForSelectedElection) {
        it?.state?.get(0)?.electionAdministrationBody?.correspondenceAddress?.toFormattedString()
            ?: "No address provided"
    }

    init {
        _loading.value = true

        _selectedElection.value = election
        viewModelScope.launch {
            election?.let {
                updateButtonText(election.id)

                try {
                    val response = CivicsApi.retrofitService.getVoterInfo(
                        it.id,
                        it.division.country + "/" + it.division.state
                    )
                    if (response.isSuccessful) {
                        _voterInfoForSelectedElection.value = response.body()
                    } else {
                        _voterInfoForSelectedElection.value = null
                        Log.e("ViewModelInfo", response.code().toString())
                    }
                } catch (e: Exception) {
                    Log.e("ViewModelInfo", e.message.toString())
                } finally {
                    _loading.value = false
                }
            }
        }
    }

    private suspend fun updateButtonText(id: Int) {
        try {
            val election = dataSource.get(id)
            if (election != null) {
                _buttonText.value = "Unfollow"
            } else {
                _buttonText.value = "Follow"
            }
        } catch (e: Exception) {
            Log.e("ViewModelElection", e.message.toString())
        }
    }

    fun followUnfollowElection() {
        _loading.value = true
        viewModelScope.launch {
            try {
                election?.let {
                    val electionDB = dataSource.get(it.id)
                    if (electionDB != null) {
                        dataSource.delete(electionDB)
                        _buttonText.value = "Follow"
                    } else {
                        dataSource.insert(election)
                        _buttonText.value = "Unfollow"
                    }
                }
            } catch (e: Exception) {
                Log.e("ViewModelElection", e.message.toString())
            } finally {
                _loading.value = false
            }
        }
    }
}