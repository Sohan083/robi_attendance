package com.example.robi_attendance.attendancesummary

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.robi_attendance.R
import com.example.robi_attendance.attendancesummary.attendancesummaryresponse.AttendanceData
import com.example.robi_attendance.attendancesummary.attendancesummaryresponse.AttendanceSummaryResponse
import com.example.robi_attendance.databinding.FragmentAttendanceSummaryBinding
import com.example.robi_attendance.model.User
import com.example.robi_attendance.utils.CustomUtility
import com.example.robi_attendance.utils.GPSLocation
import com.example.robi_attendance.utils.StaticTags
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.math.abs

class AttendanceSummaryFragment: Fragment() {
    lateinit var binding: FragmentAttendanceSummaryBinding
    var attendanceInFlag = false
    var attendanceOutFlag = false
    var network = false
    var pDialog: SweetAlertDialog? = null
    private var gpsLocation: GPSLocation? = null
    var leaveType: String? = null;
    var leavTypeId: String? = null
    var leaveTypeList : ArrayList<String> = arrayListOf("Present", "Absent", "Station Leave", "Casual Leave","Sick Leave")
    var leaveIdMap = mutableMapOf<String, String>("Present" to "1", "Casual Leave" to "3",
            "Sick Leave" to "4", "Station Leave" to "5", "Absent" to "6"
        )

    var recyclerView: RecyclerView? = null
    lateinit var mAdapter: AttendanceListDataAdapter
    private val dataList: java.util.ArrayList<AttendanceData> = java.util.ArrayList<AttendanceData>()
    private val presentList: java.util.ArrayList<AttendanceData> = java.util.ArrayList<AttendanceData>()
    private val casualLeaveList: java.util.ArrayList<AttendanceData> = java.util.ArrayList<AttendanceData>()
    private val sickLeaveList: java.util.ArrayList<AttendanceData> = java.util.ArrayList<AttendanceData>()
    private val stationLeaveList: java.util.ArrayList<AttendanceData> = java.util.ArrayList<AttendanceData>()
    private val absentList: java.util.ArrayList<AttendanceData> = java.util.ArrayList<AttendanceData>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAttendanceSummaryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
    }

    fun initialize() {
        User.user = User.instance

        binding.spinnerLeaveType.adapter =
            ArrayAdapter(requireContext(), R.layout.spinner_item, leaveTypeList)
        binding.spinnerLeaveType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                dataList.clear()
                leaveType = leaveTypeList[position]
                leavTypeId = leaveIdMap[leaveType]

                if(leavTypeId == "1")
                {
                    dataList.addAll(presentList)
                }
                else if(leavTypeId == "3")
                {
                   dataList.addAll(casualLeaveList)
                }
                else if(leavTypeId == "4")
                {
                    dataList.addAll(sickLeaveList)
                }
                else if(leavTypeId == "5")
                {
                    dataList.addAll(stationLeaveList)
                }
                else if(leavTypeId == "6")
                {
                    dataList.addAll(absentList)
                }
                mAdapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        getAttendanceSummary()
        dataList.clear()
        recyclerView = binding.attendanceListRecycler
        recyclerView!!.setHasFixedSize(true)

        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView!!.layoutManager = linearLayoutManager

        mAdapter = AttendanceListDataAdapter(dataList,requireContext())
        recyclerView!!.adapter = mAdapter

    }


    private fun getAttendanceSummary() {

        pDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.progressHelper.barColor = Color.parseColor("#08839b")
        pDialog!!.titleText = "Loading"
        pDialog!!.setCancelable(false)
        pDialog!!.show()
        val retrofit = Retrofit.Builder()
            .baseUrl(StaticTags.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(AttendanceSummaryApiInterface::class.java)
        val call = service.getAttendanceSummary(User.user!!.userId!!, User.user!!.employeeId!!)
        //Log.d("id:",User.user!!.userId!! +User.user!!.employeeId!!)
        call.enqueue(object : Callback<AttendanceSummaryResponse> {
            override fun onResponse(
                call: Call<AttendanceSummaryResponse>?,
                response: retrofit2.Response<AttendanceSummaryResponse>?
            ) {
                pDialog?.dismiss()
                Log.d("response", response?.body().toString())
                if (response != null) {
                    if (response.code() == 200) {
                        val attendanceSummaryResponse = response.body()!!
                        if (attendanceSummaryResponse.success) {

                            binding.present.text = "Present: ${attendanceSummaryResponse.summeryReport.present}"
                            binding.absent.text = "Absent: ${attendanceSummaryResponse.summeryReport.absent}"
                            binding.casualLeave.text = "Casual Leave: ${attendanceSummaryResponse.summeryReport.casual_leave}"
                            binding.sickLeave.text = "Sick Leave: ${attendanceSummaryResponse.summeryReport.sick_leave}"
                            binding.stationLeave.text = "Station Leave: ${attendanceSummaryResponse.summeryReport.station_leave}"
                            for(d in attendanceSummaryResponse.attendanceData)
                            {
                                if(d.attendance_status_id == "1")
                                {
                                    presentList.add(d)
                                }
                                else if(d.attendance_status_id == "3")
                                {
                                    casualLeaveList.add(d)
                                }
                                else if(d.attendance_status_id == "4")
                                {
                                    sickLeaveList.add(d)
                                }
                                else if(d.attendance_status_id == "6")
                                {
                                    absentList.add(d)
                                }
                            }
                            for(s in attendanceSummaryResponse.stationLeaveData)
                            {
                                stationLeaveList.add(AttendanceData(s.attendance_date,s.attendance_status_id,s.attendance_status_name,s.employee_id,s.employee_name,"",""))
                            }
                            //stationLeaveList.addAll(attendanceSummaryResponse.stationLeaveData)
                            dataList.addAll(presentList)
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<AttendanceSummaryResponse>?, t: Throwable?) {
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
    inner class AttendanceListDataAdapter(dataList: java.util.ArrayList<AttendanceData>, context: Context) :
        RecyclerView.Adapter<AttendanceListDataAdapter.MyViewHolder>() {
        var mc = context

        private val dataList: java.util.ArrayList<AttendanceData> = dataList
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.attendance_row_layout, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val data = dataList[position]
            holder.attendanceDate.text = data.attendance_date
            if(data.in_time != null && data.in_time != "")
            {
                holder.inTime.visibility = View.VISIBLE
                holder.inTime.text = data.in_time
            }
            else
            {
                holder.inTime.visibility = View.GONE
            }
            if(data.out_time != null && data.out_time != "")
            {
                holder.outTime.visibility = View.VISIBLE
                holder.outTime.text = data.out_time
            }
            else
            {
                holder.outTime.visibility = View.GONE
            }
            if(data.attendance_status_id == "1" )
            {
                holder.status.background = ContextCompat.getDrawable(requireContext(), R.color.green)
            }
            else if(data.attendance_status_id == "6")
            {
                holder.status.background = ContextCompat.getDrawable(requireContext(), R.color.coffe_red)
            }
            else
            {
                holder.status.background = ContextCompat.getDrawable(requireContext(), R.color.light_orange)
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        inner class MyViewHolder(convertView: View) : RecyclerView.ViewHolder(convertView) {
            var attendanceDate: TextView = convertView.findViewById(R.id.attendanceDate)
            var inTime: TextView = convertView.findViewById(R.id.inTime)
            var outTime: TextView = convertView.findViewById(R.id.outTime)
            var status: AppCompatTextView = convertView.findViewById(R.id.outletStatusLayout)
        }

    }
    private fun crossfadeVisible(view: View) {

        view.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            duration = 1000
            start()
        }
    }


    companion object {
        const val REQUEST_IMAGE_CAPTURE = 6
    }
}