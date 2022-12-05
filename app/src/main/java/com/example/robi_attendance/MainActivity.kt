package com.example.robi_attendance

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.robi_attendance.login.LoginActivity
import com.example.robi_attendance.model.User
import com.example.robi_attendance.model.User.Companion.user
import com.example.robi_attendance.utils.CustomUtility
import com.example.robi_attendance.utils.GPSLocation
import com.example.robi_attendance.utils.StaticTags.Companion.GPS_REQUEST
import com.google.android.material.navigation.NavigationView
import com.savvi.rangedatepicker.CalendarPickerView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {


    var button: Button? = null
    private var isGPSEnabled: Boolean = false
    private lateinit var appBarConfiguration: AppBarConfiguration

    private var gpsLocation: GPSLocation? = null
    private lateinit var navController: NavController
    var doubleBackToExitPressedOnce = false
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        //supportActionBar!!.title = "Outlet Registration"

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val appBarConfiguration = AppBarConfiguration(topLevelDestinationIds = setOf(
            R.id.attendanceFragment,
            R.id.attendanceSummaryFragment,
            R.id.logoutFragment,
            R.id.leaveFragment,
            R.id.activityFragment,

        ),drawerLayout)
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)
        val navigationView =
            findViewById<NavigationView>(R.id.nav_view)
        navigationView?.setupWithNavController(navController)

        menuNav = navigationView.menu


        user = User.instance
        if (user == null) {
            val sweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            sweetAlertDialog.setTitle("No user found please login")
            sweetAlertDialog.setConfirmButton("Ok") {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        val navHeaderMainView = navigationView.getHeaderView(0)
        navHeaderMainView.findViewById<TextView>(R.id.name).text = user?.name
        navHeaderMainView.findViewById<TextView>(R.id.location).text = user?.zone



        //ask for gps
        CustomUtility.haveGpsEnabled(this)




        gpsLocation = GPSLocation(this)
        gpsLocation!!.GPS_Start()
        gpsLocation!!.setLocationChangedListener(object : GPSLocation.LocationChangedListener {
            override fun locationChangeCallback(lat: String?, lon: String?, acc: String?) {
                presentLat = lat
                presentLon = lon
                presentAcc = acc
            }

        })

    }

    companion object {
        var presentLat: String? = null; var presentLon: String? = null; var presentAcc:String? = null
        var retailUserName: String? = null; var retailPassword: String? = null
        var menuNav: Menu? = null
        var isInActivity = false
    }


    override fun onBackPressed() {
        if (Objects.requireNonNull(navController.currentDestination)!!.id  == R.id.attendanceFragment) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to go outlet registration", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        } else if (Objects.requireNonNull(navController.currentDestination)?.id == R.id.attendanceFragment) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to return back", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
        else if(Objects.requireNonNull(navController.currentDestination)!!.id == R.id.leaveFragment) {
            finish()
            startActivity(intent)
        }
        else {
            super.onBackPressed()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GPS_REQUEST) {
                isGPSEnabled = true
                //invokeLocationAction()
            }
        }
    }

//    private ArrayList<SubTitle> getSubTitles()
//    {
//        final ArrayList < SubTitle > subTitles = new ArrayList<>();
//        final Calendar tmrw = Calendar.getInstance();
//        tmrw.add(Calendar.DAY_OF_MONTH, 1);
//        subTitles.add(new SubTitle (tmrw.getTime(), "₹1000"));
//        return subTitles;
//    }
}