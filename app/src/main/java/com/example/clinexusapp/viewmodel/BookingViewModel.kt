package com.example.clinexusapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinexusapp.api.AppointmentRepository
import com.example.clinexusapp.model.*
import com.example.clinexusapp.util.Resource
import com.example.clinexusapp.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _bookingState =
        MutableStateFlow<Resource<CreateAppointmentResponse>?>(null)

    val bookingState =
        _bookingState.asStateFlow()


    private val _dentistsState =
        MutableStateFlow<Resource<List<DentistDTO>>>(
            Resource.Loading
        )

    val dentistsState =
        _dentistsState.asStateFlow()


    private val _servicesState =
        MutableStateFlow<Resource<List<BookableServiceDTO>>>(
            Resource.Loading
        )

    val servicesState =
        _servicesState.asStateFlow()


    private val _selectedDentist =
        MutableStateFlow<DentistDTO?>(null)

    val selectedDentist =
        _selectedDentist.asStateFlow()


    private val _selectedServices =
        MutableStateFlow<List<BookableServiceDTO>>(
            emptyList()
        )

    val selectedServices =
        _selectedServices.asStateFlow()


    private val _availableTimeslots =
        MutableStateFlow<Resource<List<AvailableSlotDTO>>>(
            Resource.Success(emptyList())
        )

    val availableTimeslots =
        _availableTimeslots.asStateFlow()


    private val _dentistSchedule =
        MutableStateFlow<Resource<DentistScheduleDTO>?>(null)

    val dentistSchedule =
        _dentistSchedule.asStateFlow()


    private val _selectedDate =
        MutableStateFlow(
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date())
        )

    val selectedDate =
        _selectedDate.asStateFlow()


    init {
        fetchDentists()
        fetchServices()
    }


    // ============================================================
    // DENTISTS
    // ============================================================

    fun fetchDentists() {

        viewModelScope.launch {

            _dentistsState.value =
                Resource.Loading

            try {

                _dentistsState.value =
                    appointmentRepository
                        .getActiveDentists()

            } catch (
                e: Exception
            ) {

                _dentistsState.value =
                    Resource.Error(
                        e.localizedMessage
                            ?: "Failed to fetch dentists."
                    )
            }
        }
    }


    // ============================================================
    // SERVICES
    // ============================================================

    fun fetchServices() {

        viewModelScope.launch {

            _servicesState.value =
                Resource.Loading

            try {

                val result =
                    appointmentRepository
                        .getBookableServices()

                if (
                    result is Resource.Success
                ) {

                    val uniqueServices =
                        result.data
                            .distinctBy {
                                it.serviceId
                            }

                    _servicesState.value =
                        Resource.Success(
                            uniqueServices
                        )

                } else {

                    _servicesState.value =
                        result
                }

            } catch (
                e: Exception
            ) {

                _servicesState.value =
                    Resource.Error(
                        e.localizedMessage
                            ?: "Failed to fetch services."
                    )
            }
        }
    }


    // ============================================================
    // SELECT DENTIST
    // ============================================================

    fun selectDentist(
        dentist: DentistDTO
    ) {

        _selectedDentist.value =
            dentist

        // Clear previous time slots
        _availableTimeslots.value =
            Resource.Success(
                emptyList()
            )

        checkAndFetchTimeslots(
            _selectedDate.value
        )
    }


    // ============================================================
    // DENTIST SCHEDULE
    // ============================================================

    private fun fetchDentistSchedule(
        dentistId: Int
    ) {

        viewModelScope.launch {

            _dentistSchedule.value =
                Resource.Loading

            try {

                _dentistSchedule.value =
                    appointmentRepository
                        .getDentistSchedule(
                            dentistId
                        )

            } catch (
                e: Exception
            ) {

                _dentistSchedule.value =
                    Resource.Error(
                        e.localizedMessage
                            ?: "Failed to fetch dentist schedule."
                    )
            }
        }
    }


    // ============================================================
    // SELECT SERVICES
    // ============================================================

    fun toggleService(
        service: BookableServiceDTO
    ) {

        val current =
            _selectedServices
                .value
                .toMutableList()

        val exists =
            current.any {
                it.serviceId ==
                        service.serviceId
            }

        if (exists) {

            current.removeAll {
                it.serviceId ==
                        service.serviceId
            }

        } else {

            current.add(
                service
            )
        }

        _selectedServices.value =
            current
    }


    // ============================================================
    // DATE / TIMESLOTS
    // ============================================================

    fun checkAndFetchTimeslots(
        date: String
    ) {

        _selectedDate.value =
            date

        val dentist =
            _selectedDentist.value

        if (dentist == null) {

            _availableTimeslots.value =
                Resource.Success(
                    emptyList()
                )

            return
        }

        fetchTimeslots(
            dentistId =
                dentist.dentistId,

            date =
                date
        )
    }


    private fun fetchTimeslots(
        dentistId: Int,
        date: String
    ) {

        viewModelScope.launch {

            _availableTimeslots.value =
                Resource.Loading

            try {

                _availableTimeslots.value =
                    appointmentRepository
                        .getAvailableTimeslots(
                            dentistId =
                                dentistId,

                            date =
                                date
                        )

            } catch (
                e: Exception
            ) {

                _availableTimeslots.value =
                    Resource.Error(
                        e.localizedMessage
                            ?: "Failed to fetch timeslots."
                    )
            }
        }
    }


    // ============================================================
    // CREATE APPOINTMENT
    // ============================================================

    fun createAppointment(
        date: String,
        slot: AvailableSlotDTO
    ) {

        val patientId =
            SessionManager
                .currentUser
                .value
                ?.patientID

        val dentist =
            _selectedDentist.value

        val services =
            _selectedServices.value


        if (
            patientId == null ||
            patientId == 0
        ) {

            _bookingState.value =
                Resource.Error(
                    "Patient ID not found. Please log in again."
                )

            return
        }


        if (
            dentist == null
        ) {

            _bookingState.value =
                Resource.Error(
                    "Please select a dentist."
                )

            return
        }


        if (
            services.isEmpty()
        ) {

            _bookingState.value =
                Resource.Error(
                    "Please select at least one service."
                )

            return
        }


        val startTime =
            slot.startTime

        if (
            startTime.isNullOrBlank()
        ) {

            _bookingState.value =
                Resource.Error(
                    "Appointment start time is missing."
                )

            return
        }


        val endTime =
            slot.endTime

        if (
            endTime.isNullOrBlank()
        ) {

            _bookingState.value =
                Resource.Error(
                    "Appointment end time is missing."
                )

            return
        }


        viewModelScope.launch {

            _bookingState.value =
                Resource.Loading

            try {

                val request =
                    CreateAppointmentRequest(
                        patientId =
                            patientId,

                        dentistId =
                            dentist.dentistId,

                        appointmentDate =
                            date,

                        startTime =
                            startTime,

                        endTime =
                            endTime,

                        notes =
                            "Mobile Booking",

                        selectedServices =
                            services.map {
                                it.serviceId
                            }
                    )

                _bookingState.value =
                    appointmentRepository
                        .createAppointment(
                            request
                        )

            } catch (
                e: Exception
            ) {

                _bookingState.value =
                    Resource.Error(
                        e.localizedMessage
                            ?: "Failed to create appointment."
                    )
            }
        }
    }


    // ============================================================
    // RESET
    // ============================================================

    fun resetState() {

        _bookingState.value =
            null
    }
}