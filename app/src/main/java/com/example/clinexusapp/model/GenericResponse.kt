
package com.example.clinexusapp.model

import com.google.gson.annotations.SerializedName

data class GenericResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val verified: Boolean? = null,

    @SerializedName("resetToken")
    val resetToken: String? = null,

    @SerializedName("changePasswordToken")
    val changePasswordToken: String? = null,

    val attemptsRemaining: Int? = null,
    val lockedUntil: String? = null,

    @SerializedName("debug_otp")
    val debugOtp: String? = null
)

