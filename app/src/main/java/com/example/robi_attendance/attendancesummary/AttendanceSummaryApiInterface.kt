package com.example.robi_attendance.attendancesummary

import com.example.robi_attendance.attendancesummary.attendancesummaryresponse.AttendanceSummaryResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AttendanceSummaryApiInterface {
    @FormUrlEncoded
    @POST("attendance/get_summery_data.php")
    fun getAttendanceSummary(
        @Field("UserId") UserId: String,
        @Field("EmployeeId") EmployeeId: String,
        //@Field("AttendanceType") AttendanceType: String
    ): retrofit2.Call<AttendanceSummaryResponse>
}