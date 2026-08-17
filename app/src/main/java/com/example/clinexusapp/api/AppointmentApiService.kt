package com.example.clinexusapp.api

import com.example.clinexusapp.model.*
import retrofit2.Response
import retrofit2.http.*

interface AppointmentApiService {

    // Active dentists
    @GET("api/appointments/{token}/active-dentists")
    suspend fun getActiveDentists(
        @Path("token") tokenPath: String,
        @Header("Authorization") tokenHeader: String,
    ): Response<List<DentistDTO>>

    // Patient appointments
    @GET("api/appointments")
    suspend fun getPatientAppointments(
        @Header("Authorization") token: String,
    ): Response<List<AppointmentDTO>>

    // Bookable services (FIXED ROUTE)
    @GET("api/appointments/requires-appointment-services")
    suspend fun getBookableServices(
        @Header("Authorization") token: String,
    ): Response<List<BookableServiceDTO>>


    // Dentist available slots
    @GET("api/appointments/available-timeslots")
    suspend fun getAvailableTimeslots(
        @Header("Authorization") token: String,
        @Query("dentistID") dentistId: Int,
        @Query("appointmentDate") appointmentDate: String,
    ): Response<AvailableTimeslotsResponse>

    // Create appointment
    @POST("api/appointments")
    suspend fun createAppointment(
        @Header("Authorization") token: String,
        @Body request: CreateAppointmentRequest
    ): Response<CreateAppointmentResponse>

    // Request reschedule
    @PATCH("api/appointments/{appointmentID}/request-reschedule")
    suspend fun requestReschedule(
        @Header("Authorization") token: String,
        @Path("appointmentID") appointmentId: Int,
        @Body request: RescheduleRequest
    ): Response<GenericResponse>

    // Request cancellation
    @PATCH("api/appointments/{appointmentID}/request-cancellation")
    suspend fun requestCancelAppointment(
        @Header("Authorization") token: String,
        @Path("appointmentID") appointmentId: Int,
        @Body request: CancelAppointmentRequest
    ): Response<GenericResponse>
}
