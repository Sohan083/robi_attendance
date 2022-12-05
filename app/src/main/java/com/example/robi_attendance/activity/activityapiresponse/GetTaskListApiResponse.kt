package com.example.robi_attendance.activity.activityapiresponse

data class GetTaskListApiResponse(
    val message: String,
    val resultList: List<Result>,
    val success: Boolean
)