package com.example.robi_attendance.attendance

import com.example.robi_attendance.attendance.attendanceresponse.AttendanceStatusResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AttendanceApiInterface {
    @FormUrlEncoded
    @POST("attendance/get_attendance.php")
    fun getAttendanceStatus(
        @Field("UserId") UserId: String,
        @Field("EmployeeId") EmployeeId: String,
        @Field("AttendanceType") AttendanceType: String
    ): retrofit2.Call<AttendanceStatusResponseBody>
}