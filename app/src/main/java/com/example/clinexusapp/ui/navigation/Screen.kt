package com.example.clinexusapp.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object OTP : Screen("otp/{email}") {
        fun createRoute(email: String) = "otp/$email"
    }
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password/{email}") {
        fun createRoute(email: String) = "reset_password/$email"
    }

    // Main
    object Home : Screen("home")
    object Dashboard : Screen("dashboard")
    object AppointmentBooking : Screen("appointment_booking?doctorName={doctorName}") {
        fun createRoute(doctorName: String) = "appointment_booking?doctorName=$doctorName"
    }
    object AppointmentHistory : Screen("appointment_history")
    object Chat : Screen("chat")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object PersonalInformation : Screen("personal_information")
}
