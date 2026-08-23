package com.example.clinexusapp.model

import com.google.gson.annotations.SerializedName

data class AppointmentDTO(
    @SerializedName("appointment_id") val appointmentId: Int,
    @SerializedName("patient_id") val patientId: Int,
    @SerializedName("dentist_id") val dentistId: Int,
    @SerializedName("appointment_date") val appointmentDate: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("appointment_type") val appointmentType: String,
    @SerializedName("appointment_status") val appointmentStatus: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("dentist_first_name") val dentistFirstName: String?,
    @SerializedName("dentist_last_name") val dentistLastName: String?
) {
    // ✅ Combine first and last names
    val doctor: String get() = listOfNotNull(dentistFirstName, dentistLastName)
        .joinToString(" ")
        .ifEmpty { "Unknown Dentist" }

    // ✅ Use the correct field name
    val treatment: String get() = appointmentType.replaceFirstChar { it.uppercase() }
}
data class ChatMessageDTO(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val isImage: Boolean = false
)

data class ClinicNewsDTO(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String
)

data class HealthInsightDTO(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("iconEmoji") val iconEmoji: String
)
