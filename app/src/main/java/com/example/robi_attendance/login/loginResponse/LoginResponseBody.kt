package com.example.robi_attendance.login.loginResponse

data class LoginResponseBody(
    val message: String,
    val sessionData: SessionData,
    val success: Boolean
)