package com.example.robi_attendance.login.loginResponse

data class SessionData(
    val area_id: String,
    val area_name: Any,
    val employee_id: String,
    val employee_position_id: String,
    val full_name: String,
    val picture_name: Any,
    val region_id: String,
    val region_name: Any,
    val user_id: String,
    val user_name: String,
    val user_type_id: String,
    val user_type_name: String
)