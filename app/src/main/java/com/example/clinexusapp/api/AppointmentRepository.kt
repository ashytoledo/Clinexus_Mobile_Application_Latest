package com.example.clinexusapp.api

import com.example.clinexusapp.model.*
import com.example.clinexusapp.util.Resource
import com.example.clinexusapp.util.SessionManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(
    private val apiService: AppointmentApiService,
) {


    private fun getCleanToken(): String? {
        val raw = SessionManager.token?.trim() ?: return null
        return if (raw.startsWith("Bearer ", ignoreCase = true)) {
            raw.substring(7).trim()
        } else {
            raw
        }
    }

    private fun getAuthorizationHeader(): String? {
        val clean = getCleanToken() ?: return null
        return "Bearer $clean"
    }

    private fun <T> handleError(
        response: Response<*>,
        defaultMessage: String,
    ): Resource<T> {

        val errorBodyString =
            response.errorBody()?.string() ?: ""

        var serverMessage = defaultMessage

        if (errorBodyString.isNotEmpty()) {

            try {

                val type =
                    object : TypeToken<Map<String, Any>>() {}.type

                val errorMap: Map<String, Any> =
                    Gson().fromJson(
                        errorBodyString,
                        type,
                    )

                serverMessage =
                    errorMap["message"]?.toString()
                        ?: errorMap["error"]?.toString()
                                ?: defaultMessage

            } catch (_: Exception) {
                // Keep default message
            }
        }

        val errorMessage = when (response.code()) {

            400 ->
                "Bad Request (400): $serverMessage"

            401 ->
                "Unauthorized (401): $serverMessage. Please login again."

            403 ->
                "Forbidden (403): $serverMessage. Your authentication token is invalid or expired."

            404 ->
                "Route not found (404): $serverMessage"

            500 ->
                "Server Error (500): $serverMessage"

            else ->
                "Error ${response.code()}: $serverMessage"
        }

        return Resource.Error(errorMessage)
    }


    suspend fun getActiveDentists(): Resource<List<DentistDTO>> {

        return try {

            val token = getAuthorizationHeader()

                ?: return Resource.Error(
                    "Not authenticated. Please login again.",
                )

            val rawToken = SessionManager.token ?: ""

            val response =
                apiService.getActiveDentists(rawToken, token)

            if (response.isSuccessful) {

                Resource.Success(
                    response.body() ?: emptyList(),
                )

            } else {

                handleError(
                    response,
                    "Failed to fetch dentists"
                )
            }

        } catch (e: Exception) {

            Resource.Error(
                e.message
                    ?: "An error occurred while loading dentists."
            )
        }
    }


    suspend fun getDentistSchedule(dentistId: Int): Resource<DentistScheduleDTO> {
        return try {
            val token = getAuthorizationHeader()
                ?: return Resource.Error("Not authenticated. Please login again.")

            val response = apiService.getDentistSchedule(token, dentistId)

            if (response.isSuccessful) {
                Resource.Success(response.body()!!)
            } else {
                handleError(response, "Failed to fetch dentist schedule")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred while loading schedule.")
        }
    }



    suspend fun getBookableServices():
            Resource<List<BookableServiceDTO>> {

        return try {

            val token = getAuthorizationHeader()

                ?: return Resource.Error(
                    "Not authenticated. Please login again.",
                )

            val response =
                apiService.getBookableServices(token)

            if (response.isSuccessful) {

                Resource.Success(
                    response.body() ?: emptyList(),
                )

            } else {

                handleError(
                    response,
                    "Failed to fetch services"
                )
            }

        } catch (e: Exception) {

            Resource.Error(
                e.message
                    ?: "An error occurred while loading services."
            )
        }
    }



    suspend fun getPatientAppointments():
            Resource<List<AppointmentDTO>> {

        return try {

            val token = getAuthorizationHeader()

                ?: return Resource.Error(
                    "Not authenticated. Please login again.",
                )

            val response = apiService.getPatientAppointments(token)

            if (response.isSuccessful) {
                // ✅ Extract the wrapper, then get the list from it
                val wrapper = response.body()
                val allAppointments = wrapper?.appointments ?: emptyList()

                Log.d("AppointmentRepo", "Fetched appointments: $allAppointments")
                Resource.Success(allAppointments)   // ← Pass the LIST, not the wrapper

            } else {
                handleError(response, "Failed to fetch appointments")
            }

        } catch (e: Exception) {

            Resource.Error(
                e.message
                    ?: "An error occurred while loading appointments."
            )
        }
    }


    suspend fun getAvailableTimeslots(
        dentistId: Int,
        date: String
    ): Resource<List<AvailableSlotDTO>> {

        return try {

            val token = getAuthorizationHeader()

                ?: return Resource.Error(
                    "Not authenticated. Please login again.",
                )

            val response =
                apiService.getAvailableTimeslots(
                    token,
                    dentistId,
                    date
                )

            if (response.isSuccessful) {

                val timeStrings = response.body()?.availableTimeslots ?: emptyList()
                val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
                val sdf12 = SimpleDateFormat("hh:mm a", Locale.getDefault())

                val slots = timeStrings.map { startTime ->
                    val label = try {
                        val date = sdf24.parse(startTime)
                        if (date != null) sdf12.format(date) else startTime
                    } catch (_: Exception) {
                        startTime
                    }

                    val endTime = try {
                        val date = sdf24.parse(startTime)
                        if (date != null) {
                            val cal = Calendar.getInstance()
                            cal.time = date
                            cal.add(Calendar.HOUR_OF_DAY, 1)
                            sdf24.format(cal.time)
                        } else startTime
                    } catch (_: Exception) {
                        startTime
                    }

                    AvailableSlotDTO(
                        label = label,
                        startTime = startTime,
                        endTime = endTime
                    )
                }

                Resource.Success(slots)

            } else {

                handleError(
                    response,
                    "No timeslots available"
                )
            }

        } catch (e: Exception) {

            Resource.Error(
                e.message
                    ?: "An error occurred while loading timeslots."
            )
        }
    }


    suspend fun createAppointment(
        request: CreateAppointmentRequest
    ): Resource<CreateAppointmentResponse> {

        return try {

            val token = getAuthorizationHeader()

                ?: return Resource.Error(
                    "Not authenticated. Please login again.",
                )

            val response =
                apiService.createAppointment(
                    token,
                    request
                )

            if (response.isSuccessful) {

                val body = response.body()

                if (body != null) {

                    Resource.Success(body)

                } else {

                    Resource.Error(
                        "Server returned an empty response."
                    )
                }

            } else {

                handleError(
                    response,
                    "Failed to create appointment"
                )
            }

        } catch (e: Exception) {

            Resource.Error(
                e.message
                    ?: "An error occurred while creating appointment."
            )
        }
    }


    suspend fun rescheduleAppointment(
        appointmentId: Int,
        request: RescheduleRequest
    ): Resource<GenericResponse> {

        return try {

            val token = getAuthorizationHeader()

                ?: return Resource.Error(
                    "Not authenticated. Please login again.",
                )

            val response =
                apiService.requestReschedule(
                    token,
                    appointmentId,
                    request
                )

            if (response.isSuccessful) {

                val body = response.body()

                if (body != null) {

                    Resource.Success(body)

                } else {

                    Resource.Error(
                        "Server returned an empty response."
                    )
                }

            } else {

                handleError(
                    response,
                    "Reschedule failed"
                )
            }

        } catch (e: Exception) {

            Resource.Error(
                e.message
                    ?: "An error occurred while rescheduling."
            )
        }
    }


    suspend fun cancelAppointment(
        appointmentId: Int,
        request: CancelAppointmentRequest
    ): Resource<GenericResponse> {

        return try {

            val token = getAuthorizationHeader()

                ?: return Resource.Error(
                    "Not authenticated. Please login again.",
                )

            val response =
                apiService.requestCancelAppointment(
                    token,
                    appointmentId,
                    request
                )

            if (response.isSuccessful) {

                val body = response.body()

                if (body != null) {

                    Resource.Success(body)

                } else {

                    Resource.Error(
                        "Server returned an empty response."
                    )
                }

            } else {

                handleError(
                    response,
                    "Cancellation failed"
                )
            }

        } catch (e: Exception) {

            Resource.Error(
                e.message
                    ?: "An error occurred while cancelling appointment."
            )
        }
    }
}
