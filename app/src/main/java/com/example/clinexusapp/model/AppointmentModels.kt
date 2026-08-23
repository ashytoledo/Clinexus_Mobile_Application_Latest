package com.example.clinexusapp.model

import com.google.gson.annotations.SerializedName


data class DentistDTO(
    @SerializedName("dentist_id") val dentistId: Int,
    @SerializedName("dentist_name") val dentistName: String,
    @SerializedName("specialization_name") val specializationName: String? = null
)

data class BookableServiceDTO(
    @SerializedName("service_id") val serviceId: Int,
    @SerializedName("service_name") val serviceName: String,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("service_category_id") val categoryId: Int? = null,
    @SerializedName("service_category_name") val categoryName: String? = null,
    @SerializedName("pricing_unit_name") val pricingUnit: String? = null,
    @SerializedName("is_bookable_online") val isBookableOnline: Int? = null,
    @SerializedName("booking_workflow") val bookingWorkflow: String? = null
)

data class ServiceCategoryDTO(
    @SerializedName("service_category_id") val id: Int,
    @SerializedName("service_category_name") val name: String
)

data class AvailableSlotDTO(
    @SerializedName("label") val label: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String
)
data class PatientAppointmentsResponse(
    val success: Boolean,
    val appointments: List<AppointmentDTO>
)
data class AvailableTimeslotsResponse(
    @SerializedName("availableTimeslots") val availableTimeslots: List<String>
)

data class DentistScheduleDTO(
    @SerializedName("dentist_id") val dentistId: Int,
    @SerializedName("working_days") val workingDays: List<String>, // e.g., ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"]
    @SerializedName("start_time") val startTime: String, // e.g., "08:00:00"
    @SerializedName("end_time") val endTime: String     // e.g., "17:00:00"
)

data class CreateAppointmentRequest(
    @SerializedName("patientID") val patientId: Int,
    @SerializedName("dentistID") val dentistId: Int,
    @SerializedName("appointmentDate") val appointmentDate: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("notes") val notes: String,
    @SerializedName("selectedServices") val selectedServices: List<Int>
)

data class CreateAppointmentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("appointment_id") val appointmentId: Int? = null
)

data class RescheduleRequest(
    @SerializedName("appointmentDate") val appointmentDate: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("note") val note: String,
    @SerializedName("dentistID") val dentistId: Int? = null
)

data class CancelAppointmentRequest(
    @SerializedName("cancellationNote") val cancellationNote: String
)
