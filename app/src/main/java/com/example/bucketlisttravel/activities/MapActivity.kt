package com.example.bucketlisttravel.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bucketlisttravel.R
import com.example.bucketlisttravel.models.PlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.activity_place_detail.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mPlaceDetailDetail: PlaceModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mPlaceDetailDetail = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }

        mPlaceDetailDetail?.let {
            setSupportActionBar(toolbar_place_map)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = it.title
            toolbar_place_map.setNavigationOnClickListener {
                onBackPressed()
            }
        }

        val supportMapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mPlaceDetailDetail?.let {
            val position = LatLng(it.latitude, it.longitude)
            googleMap.addMarker(MarkerOptions().position(position).title(it.location))
            val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15F)
            googleMap.animateCamera(newLatLngZoom)
        }
    }
}