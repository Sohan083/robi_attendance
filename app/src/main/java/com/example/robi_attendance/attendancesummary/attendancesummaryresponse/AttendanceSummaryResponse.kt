package com.example.robi_attendance.attendancesummary.attendancesummaryresponse

data class AttendanceSummaryResponse(
    val attendanceData: List<AttendanceData>,
    val message: String,
    val stationLeaveData: List<StationLeaveData>,
    val success: Boolean,
    val summeryReport: SummeryReport
)