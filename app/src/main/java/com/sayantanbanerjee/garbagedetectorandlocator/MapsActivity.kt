package com.sayantanbanerjee.garbagedetectorandlocator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.clear()
        val firebaseReference =
            FirebaseDatabase.getInstance().reference
        val databaseReference = firebaseReference.child(getString(R.string.location))
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val curLocationData: LocationData? =
                            snapshot.getValue(LocationData::class.java)
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(
                                    LatLng(
                                        curLocationData?.latitude!!.toDouble(),
                                        curLocationData.longitude!!.toDouble()
                                    )
                                )
                        )
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }
}
