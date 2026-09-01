package com.example.clinexusapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinexusapp.api.AuthRepository
import com.example.clinexusapp.model.LoginRequest
import com.example.clinexusapp.model.LoginResponse
import com.example.clinexusapp.util.Resource
import com.example.clinexusapp.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<LoginResponse>?>(null)
    val loginState = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            val request = LoginRequest(email, password)
            val result = repository.login(request)
            
            if (result is Resource.Success && (result.data?.token != null) && (result.data.patient != null)) {
                SessionManager.saveSession(result.data.token, result.data.patient)
                
                // Fetch full profile immediately to get first/last name
                val profileResult = repository.getPatientProfile()
                if (profileResult is Resource.Success && profileResult.data != null) {
                    SessionManager.updateProfile(profileResult.data)
                }
            }
            
            _loginState.value = result
        }
    }
    
    fun resetState() {
        _loginState.value = null
    }
}
