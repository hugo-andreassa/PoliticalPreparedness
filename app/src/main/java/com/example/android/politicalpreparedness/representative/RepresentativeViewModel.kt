package com.example.android.politicalpreparedness.representative

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel: ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String>()
    private val _representatives = MutableLiveData<List<Representative>>()
    private val _address = MutableLiveData<Address>()

    val loading: LiveData<Boolean>
        get() = _loading
    val errorMessage: LiveData<String>
        get() = _errorMessage
    val address: LiveData<Address>
        get() = _address
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    fun getRepresentatives(address: Address) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = CivicsApi.retrofitService.getRepresentativeInfoByAddress(address.toFormattedString())
                if(response.isSuccessful) {
                    val representativesNetwork = response.body()
                    representativesNetwork?.let {
                        val representatives = representativesNetwork.offices.flatMap { it.getRepresentatives(representativesNetwork.officials) }
                        _representatives.value = representatives
                    }
                } else {
                    _errorMessage.value = "Can't find representatives"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    //TODO: Create function get address from geo location

    //TODO: Create function to get address from individual fields

    /**
     *  The following code will prove helpful in constructing a representative from the API. This code combines the two nodes of the RepresentativeResponse into a single official :

    val (offices, officials) = getRepresentativesDeferred.await()
    _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }

    Note: getRepresentatives in the above code represents the method used to fetch data from the API
    Note: _representatives in the above code represents the established mutable live data housing representatives

     */
}
