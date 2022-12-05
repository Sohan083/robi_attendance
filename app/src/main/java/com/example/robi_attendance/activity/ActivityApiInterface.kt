package com.example.robi_attendance.activity

import com.example.robi_attendance.activity.activityapiresponse.GetTaskListApiResponse
import com.example.robi_attendance.attendance.attendanceresponse.AttendanceStatusResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface ActivityApiInterface {
    @FormUrlEncoded
    @POST("task_list/get_task_list.php")
    fun getTaskList(
        @Field("UserId") UserId: String,
    ): retrofit2.Call<GetTaskListApiResponse>
}