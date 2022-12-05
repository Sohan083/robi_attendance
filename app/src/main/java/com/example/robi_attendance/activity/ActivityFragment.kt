package com.example.robi_attendance.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.robi_attendance.MainActivity
import com.example.robi_attendance.R
import com.example.robi_attendance.activity.activityapiresponse.GetTaskListApiResponse
import com.example.robi_attendance.activity.activityapiresponse.Result
import com.example.robi_attendance.attendancesummary.AttendanceSummaryApiInterface
import com.example.robi_attendance.attendancesummary.attendancesummaryresponse.AttendanceData
import com.example.robi_attendance.attendancesummary.attendancesummaryresponse.AttendanceSummaryResponse
import com.example.robi_attendance.databinding.FragmentActivitiesBinding
import com.example.robi_attendance.model.User
import com.example.robi_attendance.utils.CustomUtility
import com.example.robi_attendance.utils.GPSLocation
import com.example.robi_attendance.utils.MySingleton
import com.example.robi_attendance.utils.StaticTags
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ActivityFragment : Fragment() {
    lateinit var binding: FragmentActivitiesBinding
    var taskList = ArrayList<String>()
    var taskIdMap = mutableMapOf<Int, Result>()
    var pDialog: SweetAlertDialog? = null
    var selectedTask: Result? = null
    private var gpsLocation: GPSLocation? = null

    var imageString = ""
    var photoFlag = false
    var network = false
    var currentPath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActivitiesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        gpsLocation = GPSLocation(requireContext())
        gpsLocation!!.GPS_Start()
        gpsLocation!!.setLocationChangedListener(object : GPSLocation.LocationChangedListener {
            override fun locationChangeCallback(lat: String?, lon: String?, acc: String?) {
                MainActivity.presentLat = lat
                MainActivity.presentLon = lon
                MainActivity.presentAcc = acc
                //binding.gpsText.text = MainActivity.presentAcc
            }

        })

        getTaskList()


        binding.spinnerActivityType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedTask = taskIdMap[position]

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }

        binding.camera.setOnClickListener(View.OnClickListener {
            dispatchTakePictureIntent(StaticTags.IMAGE_CAPTURE_CODE)
            //Log.d("called","okk")
        })
        binding.submitbtn.setOnClickListener {
            network = CustomUtility.haveNetworkConnection(requireContext())
            val flag = chekFeilds()
            if (flag) {
                val confirm = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                confirm.setTitle("Are you sure?")
                confirm.setConfirmButton("Yes") {
                    confirm.dismissWithAnimation()
                    upload()
                }
                confirm.setCancelButton("No") { confirm.dismissWithAnimation() }
                confirm.show()
            }
        }

    }

    //after finishing camera intent whether the picture was save or not
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == StaticTags.IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            val file = File(currentPath!!)
            if (file.exists()) {
                val bitmap = MediaStore.Images.Media.getBitmap(
                    requireActivity().contentResolver,
                    Uri.fromFile(file)
                )
                imageString = CustomUtility.imageToString(bitmap)
                binding.takeSelfieText.text = getString(R.string.take_image_done)
                photoFlag = true
            }

        }
    }


    private fun chekFeilds(): Boolean {
        if (!network) {
            CustomUtility.showWarning(
                requireContext(),
                "Please turn on internet connection",
                "No internet connection!"
            )
            return false
        }
        if (MainActivity.presentAcc == "") {
            CustomUtility.showWarning(
                requireContext(),
                "Please wait for the gps",
                "Required fields"
            )
            return false
        } else if ((selectedTask!!.required_picture == "1") and !photoFlag) {
            CustomUtility.showWarning(requireContext(), "Take a selfie", "Required field!")
            return false
        }
//        else if ((selectedTask!!.required_remarks == "1") and (binding.remark.text.toString() != "")) {
//            CustomUtility.showWarning(requireContext(), "Take a selfie", "Required field!")
//            return false
//        }
        return true
    }


    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent(code: Int) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.robi_attendance.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, code)
                }
            }
        }
    }


    private fun upload() {
        pDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.progressHelper.barColor = Color.parseColor("#08839b")
        pDialog!!.titleText = "Loading"
        pDialog!!.setCancelable(false)
        pDialog!!.show()
        val upLoadServerUri: String = StaticTags.BASE_URL + "task/submit_daily_task.php"
        val stringRequest: StringRequest = object : StringRequest(Method.POST, upLoadServerUri,
            Response.Listener { response ->
                pDialog!!.dismiss()
                Log.e("response", response!!)
                try {
                    val jsonObject = JSONObject(response)
                    var code = jsonObject.getString("success")
                    val message = jsonObject.getString("message")
                    if (code == "true") {
                        if (photoFlag) {
                            CustomUtility.deleteFile(File(currentPath!!))
                        }
                        code = "Successful"
                        SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Successful")
                            .setContentText("")
                            .setConfirmText("Ok")
                            .setConfirmClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                                startActivity(requireActivity().intent)
                                requireActivity().finish()
                            }
                            .show()
                    } else {
                        code = "Failed"
                        CustomUtility.showError(requireContext(), message, code)
                        //CustomUtility.showError(AttendanceActivity.this,"You allready submitted in",code);
                    }
                } catch (e: JSONException) {
                    CustomUtility.showError(requireContext(), e.message, "Failed")
                }
            }, Response.ErrorListener { error ->
                pDialog!!.dismiss()
                Log.e("response", error.toString())
                CustomUtility.showError(requireContext(), "Network slow, try again", "Failed")
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["UserId"] = User.user!!.userId!!
                params["LatValue"] = MainActivity.presentLat!!
                params["LonValue"] = MainActivity.presentLon!!
                params["Accuracy"] = MainActivity.presentAcc!!
                if (photoFlag) {
                    params["ImageData"] = imageString
                }
                if(binding.remark.text.toString() != "")
                {
                    params["Remarks"] = binding.remark.text.toString()
                }
                params["TaskId"] = selectedTask!!.id

                return params
            }
        }
        stringRequest.retryPolicy =
            DefaultRetryPolicy(5000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        MySingleton.getInstance(requireContext())?.addToRequestQue(stringRequest)
    }


    private fun getTaskList() {

        pDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.progressHelper.barColor = Color.parseColor("#08839b")
        pDialog!!.titleText = "Loading"
        pDialog!!.setCancelable(false)
        pDialog!!.show()
        val retrofit = Retrofit.Builder()
            .baseUrl(StaticTags.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ActivityApiInterface::class.java)
        val call = service.getTaskList(User.user!!.userId!!)

        call.enqueue(object : Callback<GetTaskListApiResponse> {
            override fun onResponse(
                call: Call<GetTaskListApiResponse>?,
                response: retrofit2.Response<GetTaskListApiResponse>?
            ) {
                pDialog?.dismiss()
                Log.d("response", response?.body().toString())
                if (response != null) {
                    if (response.code() == 200) {
                        val getTaskListApiResponse = response.body()!!
                        if (getTaskListApiResponse.success) {
                            taskList.clear()
                            val list = getTaskListApiResponse.resultList
                            list.forEachIndexed { index, result ->
                                taskList.add(result.name)
                                taskIdMap[index] = result
                            }

                            binding.spinnerActivityType.adapter =
                                ArrayAdapter(requireContext(), R.layout.spinner_item, taskList)

                        }
                    }
                }
            }

            override fun onFailure(call: Call<GetTaskListApiResponse>?, t: Throwable?) {
                pDialog?.dismiss()
                Log.e("res", t.toString())
                CustomUtility.showError(
                    requireContext(),
                    "Network Error, try again!",
                    "Failed"
                )
            }
        })

    }
}