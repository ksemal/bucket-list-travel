package com.example.bucketlisttravel.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bucketlisttravel.R
import com.example.bucketlisttravel.adapters.PlaceAdapter
import com.example.bucketlisttravel.models.DatabaseHandler
import com.example.bucketlisttravel.models.PlaceModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabAddPlace.setOnClickListener {
            val intent = Intent(this, AddPlaceActivity::class.java)
            addPlaceActivity.launch(intent)
        }
        getPlacesListFromLocalDB()
    }

    private fun setPlacesRecyclerView(list: ArrayList<PlaceModel>) {
        rv_places_list.run {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = PlaceAdapter(context, list)
            setHasFixedSize(true)
        }
    }

    private fun getPlacesListFromLocalDB() {
        val dbHandler = DatabaseHandler(this)
        val getPlaceList: ArrayList<PlaceModel> = dbHandler.getPlacesList()
        if (getPlaceList.size > 0) {
            rv_places_list.visibility = View.VISIBLE
            tv_no_records_available.visibility = View.GONE
            setPlacesRecyclerView(getPlaceList)
        } else {
            rv_places_list.visibility = View.GONE
            tv_no_records_available.visibility = View.VISIBLE
        }
    }

    private val addPlaceActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    getPlacesListFromLocalDB()
                }
            }
        }

}