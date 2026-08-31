package com.example.clinexusapp.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import com.example.clinexusapp.model.*

interface ApiService {

    // ===== AUTH =====
    @Multipart
    @POST("api/patient-register")
    suspend fun registerPatient(
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("firstName") firstName: RequestBody,
        @Part("middleName") middleName: RequestBody?,
        @Part("lastName") lastName: RequestBody,
        @Part("dateOfBirth") dateOfBirth: RequestBody,
        @Part("phoneNumber") phoneNumber: RequestBody,
        @Part("streetAddress") streetAddress: RequestBody,
        @Part("barangay") barangay: RequestBody,
        @Part("city") city: RequestBody,
        @Part("province") province: RequestBody,
        @Part file: MultipartBody.Part? = null
    ): Response<RegisterResponse>

    @POST("api/patient-login")
    suspend fun loginPatient(
        @Body request: LoginRequest,
    ): Response<LoginResponse>

    @POST("api/verify-email")
    suspend fun verifyEmail(
        @Body request: VerifyOtpRequest
    ): Response<GenericResponse>

    @POST("api/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<GenericResponse>

    @POST("api/verify-otp")
    suspend fun verifyOTP(
        @Body request: VerifyOtpRequest
    ): Response<GenericResponse>

    @POST("api/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<GenericResponse>

    // ===== PROFILE (authenticated) =====
    @GET("api/patient-profile")
    suspend fun getPatientProfile(
        @Header("Authorization") token: String
    ): Response<PatientInfo>

    @Multipart
    @PUT("api/patient-profile")
    suspend fun updatePatientAccount(
        @Header("Authorization") token: String,
        @Part("email") email: RequestBody,
        @Part("firstName") firstName: RequestBody,
        @Part("middleName") middleName: RequestBody?,
        @Part("lastName") lastName: RequestBody,
        @Part("phoneNumber") phoneNumber: RequestBody,
        @Part("dateOfBirth") dateOfBirth: RequestBody,
        @Part("streetAddress") streetAddress: RequestBody,
        @Part("province") province: RequestBody,
        @Part("city") city: RequestBody,
        @Part("barangay") barangay: RequestBody,
        @Part file: MultipartBody.Part? = null
    ): Response<GenericResponse>

    // ===== PASSWORD CHANGE (authenticated) =====
    @POST("api/request-password-change")
    suspend fun requestPasswordChange(
        @Header("Authorization") token: String
    ): Response<GenericResponse>

    @POST("api/verify-password-change-otp")
    suspend fun verifyPasswordChangeOTP(
        @Header("Authorization") token: String,
        @Body request: VerifyPasswordChangeOtpRequest
    ): Response<GenericResponse>

    @POST("api/change-password")
    suspend fun changePatientPassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<GenericResponse>

    @GET("api/patient-history")
    suspend fun getAppointmentHistory(
        @Header("Authorization") token: String
    ): Response<List<AppointmentDTO>>

    @GET("api/available-contacts")
    suspend fun getAvailableContacts(
        @Header("Authorization") token: String
    ): Response<List<ContactDTO>>

    @GET("api/conversations")
    suspend fun getConversations(
        @Header("Authorization") token: String
    ): Response<List<ConversationDTO>>

    @GET("api/conversations/{conversationID}/messages")
    suspend fun getConversationMessages(
        @Header("Authorization") token: String,
        @Path("conversationID") conversationID: Int
    ): Response<ConversationMessagesResponse>

    @POST("api/conversations/send-message")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>

    @PATCH("api/conversations/{conversationID}/read")
    suspend fun markConversationAsRead(
        @Header("Authorization") token: String,
        @Path("conversationID") conversationID: Int,
        @Body request: MarkReadRequest
    ): Response<GenericResponse>

    @GET("api/chat-messages")
    suspend fun getChatMessages(
        @Header("Authorization") token: String
    ): Response<List<ChatMessageDTO>>

    @GET("api/clinic-news")
    suspend fun getClinicNews(
        @Header("Authorization") token: String
    ): Response<List<ClinicNewsDTO>>

    @GET("api/health-insights")
    suspend fun getHealthInsights(
        @Header("Authorization") token: String
    ): Response<List<HealthInsightDTO>>
}
