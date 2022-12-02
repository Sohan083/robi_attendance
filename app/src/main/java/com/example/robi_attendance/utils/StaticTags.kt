package com.example.robi_attendance.utils

class StaticTags {

    companion object{
        const val SELFIE_IMAGE_CAPTURE_CODE = 5
        const val OUTLET_FRONT_IMAGE_CAPTURE_CODE = 6
        const val INTERIOR1_IMAGE_CAPTURE_CODE = 7
        const val INTERIOR2_IMAGE_CAPTURE_CODE = 8
        val GPS_REQUEST: Int = 101
        val LOCATION_REQUEST: Int = 100
        const val IMAGE_CAPTURE_CODE: Int = 1
        //var BASE_URL = "https://parkrd.atmdbd.com/api/"
        //var BASE_URL = "https://demo.parkrd.atmdbd.com/api/"
        var BASE_URL = "https://robi.isgalleon.com/api/"
        var isBeta = true
        const val timeFormat = "HH:mm"
        val dateFormat = "yyyy-MM-dd"
        val dateFormatShow = "dd-MM-yyyy"
    }
}