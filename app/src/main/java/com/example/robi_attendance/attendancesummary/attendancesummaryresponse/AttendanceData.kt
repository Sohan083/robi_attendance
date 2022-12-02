package com.example.robi_attendance.attendancesummary.attendancesummaryresponse

data class AttendanceData(
    val attendance_date: String,
    val attendance_status_id: String,
    val attendance_status_name: String,
    val employee_id: String,
    val employee_name: String,
    val in_time: String,
    val out_time: String
)