package com.example.bucketlisttravel.activities

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bucketlisttravel.R
import com.example.bucketlisttravel.models.PlaceModel
import kotlinx.android.synthetic.main.activity_place_detail.*

class PlaceDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_detail)

        var placeDetailModel: PlaceModel? = null
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            placeDetailModel =
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }

        placeDetailModel?.let {
            setSupportActionBar(toolbar_place_detail)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = placeDetailModel.title
            toolbar_place_detail.setNavigationOnClickListener {
                onBackPressed()
            }

            iv_place_image.setImageURI(Uri.parse(placeDetailModel.image))
            tv_description.text = placeDetailModel.description
            tv_location.text = placeDetailModel.location
        }
    }
}