package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import java.util.*

class DetailFragment : Fragment() {

    companion object {
        const val TAG = "DetailFragment"
        const val TURN_DEVICE_LOCATION_ON_REQUEST_CODE = 1
        const val FINE_LOCATION_ACCESS_REQUEST_CODE = 1
        const val ADDRESS_LINE_1 = "AddressLine1"
        const val ADDRESS_LINE_2 = "AddressLine2"
        const val ADDRESS_CITY = "AddressCity"
        const val ADDRESS_ZIP = "AddressZip"
    }

    private lateinit var viewModel: RepresentativeViewModel
    private lateinit var binding: FragmentRepresentativeBinding

    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var address = Address("", "", "", "", "")
    private var state: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRepresentativeBinding.inflate(layoutInflater)

        viewModel = ViewModelProvider(this).get(RepresentativeViewModel::class.java)
        //TODO: Establish bindings
        binding.address = address
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

        binding.buttonSearch.setOnClickListener {
            val address = binding.address
            address?.state = state ?: ""

            address?.let {
                viewModel.getRepresentatives(address)
            }
        }

        binding.buttonLocation.setOnClickListener {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            checkLocationPermissions()
        }

        //TODO: Populate Representative adapter
        //TODO: Establish button listeners for field and location search
        binding.state.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                state = p0?.selectedItem.toString()
            }
        }

        //TODO: Define and assign Representative adapter
        setupRepresentatives()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            with(savedInstanceState) {
                // Restore value of members from saved state
                address.line1 = getString(ADDRESS_LINE_1) ?: ""
                address.line2 = getString(ADDRESS_LINE_2) ?: ""
                address.city = getString(ADDRESS_CITY) ?: ""
                address.zip = getString(ADDRESS_ZIP) ?: ""
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState?.run {
            binding.address?.let {
                address = it
                putString(ADDRESS_LINE_1, address.line1)
                putString(ADDRESS_LINE_2, address.line2)
                putString(ADDRESS_CITY, address.city)
                putString(ADDRESS_ZIP, address.zip)
            }
        }

        super.onSaveInstanceState(outState)
    }

    private fun setupRepresentatives() {
        val adapter = RepresentativeListAdapter()

        binding.representativesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.representativesRecyclerView.adapter = adapter

        viewModel.representatives.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (
            grantResults.isEmpty() ||
            grantResults[0] == PackageManager.PERMISSION_DENIED
        ) {
            Snackbar
                .make(
                    requireView(),
                    "Use my location not available",
                    Snackbar.LENGTH_LONG
                )
                .show()
        } else {
            // TODO: Handle location permission result to get location on permission granted
            asksUserToTurnOnLocation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TURN_DEVICE_LOCATION_ON_REQUEST_CODE) {
            asksUserToTurnOnLocation()
        }
    }

    private fun asksUserToTurnOnLocation() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        TURN_DEVICE_LOCATION_ON_REQUEST_CODE,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                    // exception.startResolutionForResult(this, REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar
                    .make(
                        requireView(),
                        "Use my location not available",
                        Snackbar.LENGTH_LONG
                    )
                    .show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                getLocation()
            }
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            asksUserToTurnOnLocation()
            true
        } else {
            val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            val resultCode = FINE_LOCATION_ACCESS_REQUEST_CODE

            requestPermissions(
                permissionsArray,
                resultCode
            )
            false
        }
    }

    private fun isPermissionGranted(): Boolean {
        return PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                hideKeyboard()

                lastLocation = location

                val address = geoCodeLocation(lastLocation)
                binding.address = address
                val stateText = address.state

                viewModel.getRepresentatives(address)

                if (stateText != "") {
                    val statesForSpinner = requireContext().resources.getStringArray(R.array.states)
                    val statesAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        statesForSpinner
                    )
                    val spinnerPosition: Int = statesAdapter.getPosition(stateText)
                    binding.state.setSelection(spinnerPosition)
                }
            }
        }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
            .map { address ->
                Address(
                    address.thoroughfare,
                    address.subThoroughfare,
                    address.locality,
                    address.adminArea,
                    address.postalCode
                )
            }
            .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

}