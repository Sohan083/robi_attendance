package com.example.robi_attendance.leave

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.robi_attendance.R
import com.example.robi_attendance.databinding.FragmentLeaveBinding
import com.example.robi_attendance.model.User.Companion.user
import com.example.robi_attendance.utils.CustomUtility
import com.example.robi_attendance.utils.StaticTags
import com.google.gson.Gson
import com.savvi.rangedatepicker.CalendarPickerView
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class LeaveFragment: Fragment() {
    var calendar: CalendarPickerView? = null
    lateinit var binding: FragmentLeaveBinding
    var leaveType: String? = null;
    var leavTypeId = 0
    var sweetAlertDialog: SweetAlertDialog? = null
    val machineBrandIdMap = mutableMapOf<Int, String?>()
    var selectedDate = arrayListOf<Date>()
    val showDateFormater = DateTimeFormatter.ofPattern(StaticTags.dateFormatShow)
    val dateFormatter = DateTimeFormatter.ofPattern(StaticTags.dateFormat)

    var leaveTypeList : ArrayList<String> = arrayListOf("Station Leave", "Casual Leave","Sick Leave")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLeaveBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 10);

        val lastYear = Calendar.getInstance();
        lastYear.add(Calendar.YEAR, -10);

        calendar = binding.calendarView;
        //button = findViewById(R.id.get_selected_dates);
        // for deactiviting date
//        var list =  ArrayList<Int>();
//        list.add(2);
//
//        calendar!!.deactivateDates(list);
        var arrayList = ArrayList<Date>();
        try {
            var dateformat =  SimpleDateFormat("dd-MM-yyyy");

            var strdate = "22-4-2019";
            var strdate2 = "26-4-2019";

            var newdate = dateformat . parse (strdate);
            var newdate2 = dateformat . parse (strdate2);
            arrayList.add(newdate);
            arrayList.add(newdate2);
        } catch (e: ParseException) {
            e.printStackTrace();
        }

        calendar!!.init(
            lastYear.getTime(),
            nextYear.getTime(),
            SimpleDateFormat (StaticTags.dateFormatShow,
                Locale.getDefault()
            )
        ) //
            .inMode(CalendarPickerView.SelectionMode.MULTIPLE) //
            //.withDeactivateDates(list)
            .withHighlightedDates(arrayList);

        calendar!!.scrollToDate( Date ());
        binding.spinnerLeaveType.adapter =
            ArrayAdapter(requireContext(), R.layout.spinner_item, leaveTypeList)
        binding.spinnerLeaveType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                leaveType = leaveTypeList[position]
                leavTypeId = position
                calendar!!.clearSelectedDates()
//                if(position == 0)
//                {
//                    binding.leaveLayout.visibility = View.GONE
//                    calendar!!.clearSelectedDates()
//
//
//                }
//                else
//                {
//                    binding.leaveLayout.visibility = View.VISIBLE
//                }

                //getZoneList(cityId!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        binding.submitbtn.setOnClickListener {

            if((calendar!!.selectedDates!=null || calendar!!.selectedDates.size > 0))
            {
                if(CustomUtility.haveNetworkConnection(requireContext()))
                {
                    sweetAlertDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                    sweetAlertDialog!!.setTitle("Are you sure")
                    sweetAlertDialog!!.setCancelButton("No") {
                        sweetAlertDialog!!.dismiss()
                    }
                    sweetAlertDialog!!.setConfirmButton("Yes", SweetAlertDialog.OnSweetClickListener {
                        sweetAlertDialog!!.dismiss()
                        submitLeave()
                    })
                    sweetAlertDialog!!.show()
                }
                else
                {
                    CustomUtility.showError(requireContext(),"No internet connection","Error")
                }
            }
            else
            {
                CustomUtility.showError(requireContext(),"Please select leave dates","Error")
            }
        }
//        button!!.setOnClickListener(View . OnClickListener () {
//            Toast.makeText(
//                this,
//                "list " + calendar!!.getSelectedDates().toString(),
//                Toast.LENGTH_LONG
//            ).show();
//        });

    }

    private fun submitLeave() {

        val sweetAlertDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
        sweetAlertDialog.titleText = "Loading"
        sweetAlertDialog.setCancelable(false)
        sweetAlertDialog.show()
        val queue = Volley.newRequestQueue(requireContext())
        var url = ""
        if(leavTypeId == 0)
        {
            url = StaticTags.BASE_URL + "leave/station_leave_submit.php"

        }
        else{
            url = StaticTags.BASE_URL + "leave/leave_submit.php"

        }

        val sr: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                sweetAlertDialog.dismiss()
                Log.d("response:", it)
                try {

                    val jsonObject = JSONObject(it)
                    if (jsonObject.getBoolean("success")) {
                        CustomUtility.showSuccess(
                            requireContext(),
                            "","Success"
                        )
                        calendar!!.clearSelectedDates()

                    } else {
                        CustomUtility.showError(
                            requireContext(),
                            "Failed!",
                            jsonObject.getString("message")
                        )
                    }
                } catch (e: JSONException) {
                    CustomUtility.showError(requireContext(), e.message, "Failed")
                }

            },
            Response.ErrorListener {
                sweetAlertDialog.dismiss()
                CustomUtility.showError(requireContext(), "Network problem, try again", "Failed")
            }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()

                params["UserId"] = user!!.userId!!
                params["AppVersion"] = getString(R.string.version)
                params["EmployeeId"] = user!!.employeeId!!
                val d= calendar!!.selectedDates
                val a = LeaveDates()
                for(v in d){
                    a.add(LeaveDatesItem(SimpleDateFormat(StaticTags.dateFormat).format(v)))
                }
                Log.d("dates",Gson().toJson(a))
                params["LeaveDates"] = Gson().toJson(a)

                params["LeaveType"] = leaveType!!

                return params
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/x-www-form-urlencoded"
                return params
            }
        }
        queue.add(sr)
    }

}