package com.example.clinexusapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinexusapp.api.AppointmentRepository
import com.example.clinexusapp.api.AuthRepository
import com.example.clinexusapp.model.AppointmentDTO
import com.example.clinexusapp.model.CancelAppointmentRequest
import com.example.clinexusapp.model.RescheduleRequest
import com.example.clinexusapp.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _historyState = MutableStateFlow<Resource<List<AppointmentDTO>>>(Resource.Loading)
    val historyState = _historyState.asStateFlow()

    init {
        fetchHistory()
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _historyState.value = Resource.Loading
            val result = appointmentRepository.getPatientAppointments()
            
            if (result is Resource.Success && result.data != null) {
                val sorted = result.data.sortedByDescending { 
                    try {
                        val cleanDate = it.appointmentDate.substringBefore("T")
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                        sdf.parse("$cleanDate ${it.startTime}")
                    } catch (_: Exception) {
                        null
                    }
                }
                _historyState.value = Resource.Success(sorted)
            } else {
                _historyState.value = result
            }
        }
    }

    fun rescheduleAppointment(id: Int, newDate: String, newStartTime: String, newEndTime: String, note: String) {
        viewModelScope.launch {
            appointmentRepository.rescheduleAppointment(
                id,
                RescheduleRequest(newDate, newStartTime, newEndTime, note)
            )
            fetchHistory()
        }
    }

    fun cancelAppointment(id: Int, reason: String = "Patient requested cancellation") {
        viewModelScope.launch {
            appointmentRepository.cancelAppointment(
                id,
                CancelAppointmentRequest(reason)
            )
            fetchHistory()
        }
    }
}
