package com.example.clinexusapp.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val message: String? = null,
    val token: String? = null,
    val patient: PatientInfo? = null,
    val success: Boolean? = null
)

data class PatientInfo(
    @SerializedName("account_id") val accountID: Int,
    @SerializedName("patient_id") val patientID: Int,
    val role: String,
    val email: String? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("middle_name") val middleName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    @SerializedName("street_address") val streetAddress: String? = null,
    @SerializedName("province") val province: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("barangay") val barangay: String? = null
)
