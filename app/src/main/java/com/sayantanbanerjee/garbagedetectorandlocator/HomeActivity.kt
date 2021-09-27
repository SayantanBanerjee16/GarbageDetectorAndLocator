package com.sayantanbanerjee.garbagedetectorandlocator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val routeToNewLocationActivity: Button = findViewById(R.id.routeToNewLocationActivity)
        val routeToMapsActivity: Button = findViewById(R.id.routeToMapsActivity)

        routeToNewLocationActivity.setOnClickListener {
            val intent : Intent = Intent(this, NewLocationActivity::class.java)
            startActivity(intent)
        }

        routeToMapsActivity.setOnClickListener {
            val intent : Intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }
}
