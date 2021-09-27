package com.sayantanbanerjee.garbagedetectorandlocator

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


const val PICK_IMAGE = 1

@Suppress("DEPRECATION")
class NewLocationActivity : AppCompatActivity() {

    val PERMISSION_ID = 44

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private lateinit var photoHolder: ImageView
    private val mInputSize = 255
    private val mModelPath = "converted_model.tflite"
    private val mLabelPath = "label.txt"
    private lateinit var classifier: Classifier
    private lateinit var fabButton: FloatingActionButton

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_location)

        initClassifier()

        val galleryButton: Button = findViewById(R.id.galleryButton)
        photoHolder = findViewById(R.id.picView)
        fabButton = findViewById(R.id.fabToUpload)
        databaseReference = Firebase.database.reference

        fabButton.visibility = View.GONE
        fabButton.setOnClickListener {
            getLastLocation()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        galleryButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        if (requestCode == PICK_IMAGE) {
            val imageBitmap: Bitmap =
                MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)
            photoHolder.setImageBitmap(imageBitmap)

            val result = classifier.recognizeImage(imageBitmap)
            runOnUiThread { Toast.makeText(this, result[0].toString(), Toast.LENGTH_LONG).show() }

            if (result[0].title == "garbage") {
                fabButton.visibility = View.VISIBLE
            }
        }
    }

    private fun initClassifier() {
        classifier = Classifier(assets, mModelPath, mLabelPath, mInputSize)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        Log.i("#########", "GET LAST LOCATION ELSE")
                        val locationData = LocationData(
                            location.latitude.toString(),
                            location.longitude.toString()
                        )
                        databaseReference.child(getString(R.string.location)).push()
                            .setValue(locationData)
                        fabButton.visibility = View.GONE
                        runOnUiThread {
                            Toast.makeText(
                                baseContext,
                                "Updated To Cloud",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        Log.i("#########", "REQUEST NEW LOCATION DATA")
        // Initializing LocationRequest
        // object with appropriate methods
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            Log.i("#########", "M LOCATION CALLBACK")

            val mLastLocation: Location = locationResult.lastLocation
            val locationData =
                LocationData(mLastLocation.latitude.toString(), mLastLocation.longitude.toString())
            databaseReference.child(getString(R.string.location)).push().setValue(locationData)
            fabButton.visibility = View.GONE
            runOnUiThread {
                Toast.makeText(
                    baseContext,
                    "Updated To Cloud",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    // method to check for permissions
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSION_ID
        )
    }

    // method to check
    // if location is enabled
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // If everything is alright then
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }

}
