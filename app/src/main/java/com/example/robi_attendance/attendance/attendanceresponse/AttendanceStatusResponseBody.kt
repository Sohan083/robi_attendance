package com.example.robi_attendance.attendance.attendanceresponse

import com.example.robi_attendance.attendance.attendanceresponse.Result

data class AttendanceStatusResponseBody(
    val message: String,
    val resultList: List<Result>,
    val success: Boolean
)