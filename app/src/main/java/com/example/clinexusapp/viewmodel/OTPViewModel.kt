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

    // General OTP operation state
    private val _otpState = MutableStateFlow<Resource<GenericResponse>?>(null)
    val otpState = _otpState.asStateFlow()

    // Stores the token returned after OTP verification (for reset or change password)
    private val _resetToken = MutableStateFlow<String?>(null)
    val resetToken = _resetToken.asStateFlow()

    // ---------- Forgot Password (logged out) ----------
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _otpState.value = Resource.Loading()
            _otpState.value = repository.forgotPassword(email)
        }
    }

    fun verifyOTP(email: String, otp: String) {
        viewModelScope.launch {
            _otpState.value = Resource.Loading()
            val result = repository.verifyOTP(email, otp)
            if (result is Resource.Success) {
                _resetToken.value = result.data?.resetToken
            }
            _otpState.value = result
        }
    }

    fun resetPassword(resetToken: String, newPassword: String) {
        viewModelScope.launch {
            _otpState.value = Resource.Loading()
            _otpState.value = repository.resetPassword(resetToken, newPassword)
        }
    }

    // ---------- Change Password (logged in) ----------
    fun requestPasswordChange() {
        viewModelScope.launch {
            _otpState.value = Resource.Loading()
            _otpState.value = repository.requestPasswordChange()
        }
    }

    fun verifyPasswordChangeOTP(otp: String) {
        viewModelScope.launch {
            _otpState.value = Resource.Loading()
            val result = repository.verifyPasswordChangeOTP(otp)
            if (result is Resource.Success) {
                _resetToken.value = result.data?.changePasswordToken
            }
            _otpState.value = result
        }
    }

    fun changePassword(changePasswordToken: String, newPassword: String) {
        viewModelScope.launch {
            _otpState.value = Resource.Loading()
            _otpState.value = repository.changePassword(changePasswordToken, newPassword)
        }
    }

    // Helper: resend OTP (re‑uses forgotPassword – you can add a dedicated endpoint later)
    fun resendOtp(email: String) {
        viewModelScope.launch {
            _otpState.value = Resource.Loading()
            _otpState.value = repository.forgotPassword(email)
        }
    }

    fun resetState() {
        _otpState.value = null
        _resetToken.value = null
    }
}