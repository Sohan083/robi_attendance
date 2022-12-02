package com.example.robi_attendance.attendancesummary.attendancesummaryresponse

data class SummeryReport(
    val absent: Int,
    val casual_leave: Int,
    val present: Int,
    val sick_leave: Int,
    val station_leave: Int
)