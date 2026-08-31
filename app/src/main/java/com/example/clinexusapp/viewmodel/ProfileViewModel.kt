package com.example.clinexusapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinexusapp.api.AuthRepository
import com.example.clinexusapp.api.AddressRepository
import com.example.clinexusapp.model.*
import com.example.clinexusapp.util.Resource
import com.example.clinexusapp.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ProfileViewModel(
    private val repository: AuthRepository,
    private val addressRepository: AddressRepository? = null
) : ViewModel() {

    // Update state
    private val _updateState = MutableStateFlow<Resource<GenericResponse>?>(null)
    val updateState = _updateState.asStateFlow()

    // Address dropdown data
    private val _regions = MutableStateFlow<List<Region>>(emptyList())
    val regions = _regions.asStateFlow()

    private val _provinces = MutableStateFlow<List<Province>>(emptyList())
    val provinces = _provinces.asStateFlow()

    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities = _cities.asStateFlow()

    private val _barangays = MutableStateFlow<List<Barangay>>(emptyList())
    val barangays = _barangays.asStateFlow()

    init {
        loadRegions()
    }

    // ---------- Address Helpers ----------
    private fun loadRegions() {
        viewModelScope.launch {
            addressRepository?.getRegions()?.let { result ->
                if (result is Resource.Success) {
                    _regions.value = result.data ?: emptyList()
                }
            }
        }
    }

    fun onRegionSelected(regionCode: String) {
        viewModelScope.launch {
            _provinces.value = emptyList()
            _cities.value = emptyList()
            _barangays.value = emptyList()
            addressRepository?.getProvinces(regionCode)?.let { result ->
                if (result is Resource.Success) {
                    _provinces.value = result.data ?: emptyList()
                }
            }
        }
    }

    fun onProvinceSelected(provinceCode: String) {
        viewModelScope.launch {
            _cities.value = emptyList()
            _barangays.value = emptyList()
            addressRepository?.getCities(provinceCode)?.let { result ->
                if (result is Resource.Success) {
                    _cities.value = result.data ?: emptyList()
                }
            }
        }
    }

    fun onCitySelected(cityCode: String) {
        viewModelScope.launch {
            _barangays.value = emptyList()
            addressRepository?.getBarangays(cityCode)?.let { result ->
                if (result is Resource.Success) {
                    _barangays.value = result.data ?: emptyList()
                }
            }
        }
    }

    // ---------- Update Profile ----------

    fun updateProfile(
        firstName: String,
        lastName: String,
        email: String
    ) {
        viewModelScope.launch {
            val currentUser = SessionManager.currentUser.value

            if (currentUser == null) {
                _updateState.value = Resource.Error("User profile not found")
                return@launch
            }

            val request = UpdateProfileRequest(
                email = email.trim(),
                firstName = firstName.trim(),
                middleName = currentUser.middleName,
                lastName = lastName.trim(),
                phoneNumber = currentUser.phoneNumber ?: "",
                dateOfBirth = currentUser.dateOfBirth ?: "",
                streetAddress = currentUser.streetAddress ?: "",
                province = currentUser.province ?: "",
                city = currentUser.city ?: "",
                barangay = currentUser.barangay ?: ""
            )

            _updateState.value = Resource.Loading()

            val result = repository.updatePatientProfile(request)

            if (result is Resource.Success) {
                refreshProfile()
            }

            _updateState.value = result
        }
    }

    fun updateFullProfile(
        request: UpdateProfileRequest,
        profileImage: MultipartBody.Part? = null
    ) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading()
            val result = repository.updatePatientProfile(request, profileImage)

            if (result is Resource.Success) {
                refreshProfile()
            }

            _updateState.value = result
        }
    }

// ---------- Fetch Profile ----------

    fun fetchProfile() {
        viewModelScope.launch {
            refreshProfile()
        }
    }
    private suspend fun refreshProfile() {
        repository.getPatientProfile().let { result ->
            if (result is Resource.Success) {
                SessionManager.updateProfile(result.data!!)
            }
        }
    }

    fun resetState() {
        _updateState.value = null
    }
}