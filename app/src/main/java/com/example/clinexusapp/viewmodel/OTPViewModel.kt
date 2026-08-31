package com.example.clinexusapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinexusapp.api.AuthRepository
import com.example.clinexusapp.model.GenericResponse
import com.example.clinexusapp.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OTPViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _otpState = MutableStateFlow<Resource<GenericResponse>?>(null)
    val otpState = _otpState.asStateFlow()

    fun verifyOtp(email: String, otp: String, isPasswordReset: Boolean = false) {
        viewModelScope.launch {
            _otpState.value = Resource.Loading()
            if (isPasswordReset) {
                _otpState.value = repository.verifyOTP(email, otp)
            } else {
                _otpState.value = repository.verifyEmail(email, otp)
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _otpState.value = Resource.Loading()
            _otpState.value = repository.forgotPassword(email)
        }
    }

    fun resetPassword(resetToken: String, newPassword: String) {
        viewModelScope.launch {
            _otpState.value = Resource.Loading()
            _otpState.value = repository.resetPassword(resetToken, newPassword)
        }
    }

    fun resendOtp(email: String) {
        viewModelScope.launch {
            // Re-using forgotPassword as a "resend" if backend supports it, 
            // or we could add a specific resendOtp endpoint.
            repository.forgotPassword(email)
        }
    }

    fun resetState() {
        _otpState.value = null
    }
}
