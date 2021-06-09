package com.example.bucketlisttravel.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bucketlisttravel.R
import com.example.bucketlisttravel.models.DatabaseHandler
import com.example.bucketlisttravel.models.PlaceModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabAddPlace.setOnClickListener {
            val intent = Intent(this, AddPlaceActivity::class.java)
            startActivity(intent)
        }
        getPlacesListFromLocalDB()
    }

    private fun getPlacesListFromLocalDB() {
        val dbHandler = DatabaseHandler(this)
        val getPlaceList: ArrayList<PlaceModel> = dbHandler.getPlacesList()
        if (getPlaceList.size > 0) {
            for (i in getPlaceList) {
                Log.e("Title", "${i.title}")
                Log.e("Description", "${i.description}")
            }
        }
    }
}