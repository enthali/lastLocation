package de.drachenfels.lastlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import de.drachenfels.lastlocation.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    // UI Binding
    private lateinit var mainBinding: ActivityMainBinding

    // Location handling
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    /*
     * onCreate
     * bind UI
     * bind Button
     * get location service
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // UI binding
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        mainBinding.buttonGetLocation.setOnClickListener {
            getLocation()
        }
        // get location service
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /*
     * run task to get lastLocation and call setLocation() to perform what
     * ever the app want's to do with the location
     */
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        // check if location access is permitted by the user
        if (checkPermissions()) {
            // check if the location service is enabled on the device
            if (isLocationEnabled()) {
                // get the last known location and execute setLocation()
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    //   val lastLoc: Location = task.result
                    setLocation(task.result)
                }
            } else {
                // request the user to turn on the location service on his device
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            // ask the user to allow permission
            requestPermissions()
        }
    }

    /*
    * here's what we do when we get a location from the fused location client task
    * in this app we simply show the current location in the UI
    * but feel free to do what ever you desire here
    */
    @SuppressLint("SetTextI18n")
    private fun setLocation(lastLoc: Location) {

        // get the object for the default Geocoder
        val geocoder = Geocoder(this, Locale.getDefault())

        // translate lan / lat to street address - call is depreciated !!
        @Suppress("DEPRECATION")
        val list = geocoder.getFromLocation(lastLoc.latitude, lastLoc.longitude, 1)

        // present the result on the screen
        mainBinding.apply {
            lastLatitude.text = "Latitude\n${list?.get(0)?.latitude}"
            lastLongitude.text = "Longitude\n${list?.get(0)?.longitude}"
            lastCountry.text = "Country Name\n${list?.get(0)?.countryName}"
            lastLocality.text = "Locality\n${list?.get(0)?.locality}"
            lastAddress.text = "Address\n${list?.get(0)?.getAddressLine(0)}"
        }
    }

    /*
     * check if the location services are enabled on the device
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    /*
     * check if the user granted the app permission to the location services
     */
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    /*
     * ask the user to grant permissions for the app
     */
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            ), permissionId
        )
    }

    /*
     * if the permissions are given, get the latest location now
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}